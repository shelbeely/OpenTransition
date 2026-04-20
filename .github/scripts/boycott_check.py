#!/usr/bin/env python3
"""
boycott_check.py — StandWithUkraine dependency/action scanner.

Scans the repository for Gradle dependencies, Gradle plugin IDs, and GitHub
Actions `uses:` references that match the project's boycott denylist.

Exit codes:
  0 — no matches found
  1 — one or more blocked items detected
  2 — configuration/usage error

Usage:
  python .github/scripts/boycott_check.py [--repo-root PATH]
"""

import argparse
import os
import re
import sys
from pathlib import Path
from typing import Iterator

try:
    import yaml
except ImportError:
    print("ERROR: PyYAML is required. Install it with: pip install pyyaml", file=sys.stderr)
    sys.exit(2)


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _load_yaml(path: Path) -> dict:
    with open(path, "r", encoding="utf-8") as fh:
        return yaml.safe_load(fh) or {}


def _iter_files(root: Path, suffix: str) -> Iterator[Path]:
    for p in root.rglob(f"*{suffix}"):
        # Skip build output directories
        parts = p.parts
        if any(part in ("build", ".gradle", ".git", "node_modules") for part in parts):
            continue
        yield p


# ---------------------------------------------------------------------------
# Extraction helpers
# ---------------------------------------------------------------------------

# Matches Gradle dependency strings like:
#   implementation 'com.foo:bar:1.0'
#   implementation "com.foo:bar:1.0"
#   classpath 'com.foo:bar:1.0'
#   api "com.foo:bar:1.0"
_GRADLE_DEP_RE = re.compile(
    r"""(?:implementation|api|classpath|testImplementation|androidTestImplementation|
         compileOnly|runtimeOnly|annotationProcessor|kapt|ksp|
         debugImplementation|releaseImplementation|coreLibraryDesugaring)
        \s+['"]([^'"]+)['"]""",
    re.VERBOSE | re.IGNORECASE,
)

# Matches plugin IDs in Gradle `plugins { id '...' }` blocks and
# `apply plugin: '...'` style, as well as `id '...' version '...'`
_GRADLE_PLUGIN_RE = re.compile(
    r"""(?:id\s+['"]([^'"]+)['"]|apply\s+plugin:\s*['"]([^'"]+)['"])""",
    re.IGNORECASE,
)

# Matches `uses: org/repo@ref` and `- uses: org/repo@ref` in YAML workflow files
_ACTION_USES_RE = re.compile(
    r"""^\s+(?:-\s+)?uses:\s+['"]?([A-Za-z0-9_.\-]+)/([A-Za-z0-9_.\-/]+)@""",
    re.MULTILINE,
)

# Matches URLs in `run:` blocks (curl/wget targets)
_URL_RE = re.compile(r"https?://([A-Za-z0-9.\-]+)/")


def extract_gradle_deps(text: str) -> list[str]:
    """Return a list of 'group:artifact' strings found in a Gradle file."""
    coords = []
    for m in _GRADLE_DEP_RE.finditer(text):
        coord = m.group(1).strip()
        # Strip version — keep group:artifact
        parts = coord.split(":")
        if len(parts) >= 2:
            coords.append(f"{parts[0]}:{parts[1]}")
    return coords


def extract_gradle_plugins(text: str) -> list[str]:
    """Return plugin IDs found in a Gradle file."""
    plugins = []
    for m in _GRADLE_PLUGIN_RE.finditer(text):
        pid = m.group(1) or m.group(2)
        if pid:
            plugins.append(pid.strip())
    return plugins


def extract_action_orgs(text: str) -> list[str]:
    """Return GitHub org names from `uses:` entries in a workflow YAML."""
    return [m.group(1) for m in _ACTION_USES_RE.finditer(text)]


def extract_domains(text: str) -> list[str]:
    """Return hostnames found in http(s) URLs inside a text block."""
    return [m.group(1).lower() for m in _URL_RE.finditer(text)]


# ---------------------------------------------------------------------------
# Denylist / allowlist loading
# ---------------------------------------------------------------------------

def build_denied_sets(denylist: dict) -> dict:
    """
    Return a dict with compiled lists of blocked items.

    Keys:
      tld_patterns  - list of TLD suffix strings (e.g. '.ru')
      gradle_groups - list of (prefix, vendor_name) pairs
      gradle_plugins - list of (prefix, vendor_name) pairs
      action_orgs  - list of (org_lower, vendor_name) pairs
      domains      - list of (substring, vendor_name) pairs
    """
    result: dict = {
        "tld_patterns": denylist.get("domain_tld_patterns", []),
        "gradle_groups": [],
        "gradle_plugins": [],
        "action_orgs": [],
        "domains": [],
    }
    for vendor in denylist.get("vendors", []):
        name = vendor.get("name", "unknown")
        for g in vendor.get("gradle_groups", []):
            result["gradle_groups"].append((g.lower(), name))
        for p in vendor.get("gradle_plugins", []):
            result["gradle_plugins"].append((p.lower(), name))
        for o in vendor.get("action_orgs", []):
            result["action_orgs"].append((o.lower(), name))
        for d in vendor.get("domains", []):
            result["domains"].append((d.lower(), name))
    return result


def build_allowed_sets(allowlist: dict) -> dict:
    result: dict = {
        "gradle_groups": set(),
        "gradle_plugins": set(),
        "action_orgs": set(),
        "domains": set(),
    }
    for ex in allowlist.get("exemptions", []) or []:
        for g in ex.get("gradle_groups", []):
            result["gradle_groups"].add(g.lower())
        for p in ex.get("gradle_plugins", []):
            result["gradle_plugins"].add(p.lower())
        for o in ex.get("action_orgs", []):
            result["action_orgs"].add(o.lower())
        for d in ex.get("domains", []):
            result["domains"].add(d.lower())
    return result


# ---------------------------------------------------------------------------
# Matching
# ---------------------------------------------------------------------------

def _is_blocked_by_tld(value: str, tld_patterns: list[str]) -> bool:
    """Return True if `value` ends with a blocked TLD pattern (domain form) or
    starts with a blocked reversed-domain prefix (Gradle group form).

    Examples:
      domain form:   'kaspersky.ru' ends with '.ru'  → True
      group form:    'ru.kaspersky' starts with 'ru.' → True (reversed .ru)
    """
    v = value.lower()
    for tld in tld_patterns:
        # Direct domain suffix check (e.g. 'kaspersky.ru' ends with '.ru')
        if v.endswith(tld):
            return True
        # Reversed-domain prefix check for Gradle group IDs
        # '.ru' → reversed prefix 'ru.'
        stripped = tld.lstrip(".")
        if v.startswith(f"{stripped}."):
            return True
    return False


def _matches_prefix_list(value: str, prefix_list: list[tuple[str, str]]) -> tuple[bool, str]:
    """Return (True, vendor_name) if value starts with any blocked prefix."""
    v = value.lower()
    for prefix, vendor in prefix_list:
        if v.startswith(prefix.lower()):
            return True, vendor
    return False, ""


def _is_in_allowed(value: str, allowed_set: set) -> bool:
    v = value.lower()
    return any(v.startswith(a.lower()) for a in allowed_set)


# ---------------------------------------------------------------------------
# Scan routines
# ---------------------------------------------------------------------------

Finding = tuple[str, str, str, str]  # (file, kind, value, vendor)


def scan_gradle_file(path: Path, denied: dict, allowed: dict) -> list[Finding]:
    text = path.read_text(encoding="utf-8", errors="replace")
    findings: list[Finding] = []

    # Dependencies
    for coord in extract_gradle_deps(text):
        group = coord.split(":")[0]
        if _is_in_allowed(group, allowed["gradle_groups"]):
            continue
        if _is_blocked_by_tld(group, denied["tld_patterns"]):
            findings.append((str(path), "gradle_dep", coord, "TLD rule"))
            continue
        matched, vendor = _matches_prefix_list(group, denied["gradle_groups"])
        if matched:
            findings.append((str(path), "gradle_dep", coord, vendor))

    # Plugins
    for pid in extract_gradle_plugins(text):
        if _is_in_allowed(pid, allowed["gradle_plugins"]):
            continue
        matched, vendor = _matches_prefix_list(pid, denied["gradle_plugins"])
        if matched:
            findings.append((str(path), "gradle_plugin", pid, vendor))

    return findings


def scan_workflow_file(path: Path, denied: dict, allowed: dict) -> list[Finding]:
    text = path.read_text(encoding="utf-8", errors="replace")
    findings: list[Finding] = []

    # Action orgs
    for org in extract_action_orgs(text):
        if _is_in_allowed(org, allowed["action_orgs"]):
            continue
        matched, vendor = _matches_prefix_list(org, denied["action_orgs"])
        if matched:
            findings.append((str(path), "action_uses", org, vendor))

    # URLs in run: blocks
    for domain in extract_domains(text):
        if _is_in_allowed(domain, allowed["domains"]):
            continue
        # TLD check
        if _is_blocked_by_tld(domain, denied["tld_patterns"]):
            findings.append((str(path), "workflow_url", domain, "TLD rule"))
            continue
        matched, vendor = _matches_prefix_list(domain, denied["domains"])
        if matched:
            findings.append((str(path), "workflow_url", domain, vendor))

    return findings


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main() -> int:
    parser = argparse.ArgumentParser(description="StandWithUkraine boycott checker")
    parser.add_argument(
        "--repo-root",
        default=os.environ.get("GITHUB_WORKSPACE", "."),
        help="Root of the repository to scan (default: GITHUB_WORKSPACE or cwd)",
    )
    args = parser.parse_args()
    repo_root = Path(args.repo_root).resolve()

    boycott_dir = repo_root / ".github" / "boycott"
    denylist_path = boycott_dir / "denylist.yml"
    allowlist_path = boycott_dir / "allowlist.yml"

    if not denylist_path.exists():
        print(f"ERROR: Denylist not found at {denylist_path}", file=sys.stderr)
        return 2

    denylist = _load_yaml(denylist_path)
    allowlist = _load_yaml(allowlist_path) if allowlist_path.exists() else {}

    denied = build_denied_sets(denylist)
    allowed = build_allowed_sets(allowlist)

    all_findings: list[Finding] = []

    # Scan Gradle files
    for gradle_file in _iter_files(repo_root, ".gradle"):
        all_findings.extend(scan_gradle_file(gradle_file, denied, allowed))

    # Scan workflow YAML files
    workflows_dir = repo_root / ".github" / "workflows"
    if workflows_dir.exists():
        for yml_file in workflows_dir.glob("*.yml"):
            all_findings.extend(scan_workflow_file(yml_file, denied, allowed))

    # Report
    if not all_findings:
        print("✅  No blocked dependencies or actions found.")
        return 0

    print(f"🚫  Found {len(all_findings)} blocked item(s):\n")
    prev_file = None
    for file_path, kind, value, vendor in sorted(all_findings, key=lambda x: x[0]):
        rel = os.path.relpath(file_path, repo_root)
        if rel != prev_file:
            print(f"  {rel}")
            prev_file = rel
        print(f"    [{kind}]  {value}  →  {vendor}")

    print(
        "\n"
        "These packages/actions are associated with vendors on the\n"
        "StandWithUkraine boycott list:\n"
        "  https://github.com/vshymanskyy/StandWithUkraine/blob/main/docs/Boycott.md\n"
        "\n"
        "To resolve this:\n"
        "  1. Replace the dependency/action with an unblocked alternative, OR\n"
        "  2. If you believe this is a false positive, add an exemption to\n"
        "     .github/boycott/allowlist.yml with a documented reason.\n"
    )
    return 1


if __name__ == "__main__":
    sys.exit(main())
