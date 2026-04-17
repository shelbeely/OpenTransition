#!/usr/bin/env python3
"""
OpenTransition – Comprehensive CLI feature test
Drives the app end-to-end via adb + uiautomator.
All resource-IDs and content-descs verified from live UI dumps.

Usage:
    python3 test_opentransition.py [--screenshots-dir <path>]

Exit code: 0 if all checks pass, 1 if any fail.
"""
import subprocess, re, sys, time, argparse, os
import xml.etree.ElementTree as ET

# ─────────────────────────── config ────────────────────────────────────────
DEVICE  = "emulator-5554"
PKG     = "com.shelbeely.opentransition"
DEFAULT_SS_DIR = os.path.join(os.path.dirname(__file__), "screenshots")

parser = argparse.ArgumentParser()
parser.add_argument("--screenshots-dir", default=DEFAULT_SS_DIR)
ARGS = parser.parse_args()
SS_DIR = ARGS.screenshots_dir
os.makedirs(SS_DIR, exist_ok=True)

# ─────────────────────────── primitives ────────────────────────────────────

def adb(*args, timeout=30):
    r = subprocess.run(["adb", "-s", DEVICE] + list(args),
                       capture_output=True, text=True, timeout=timeout)
    return r.stdout.strip()

def dump():
    adb("shell", "uiautomator", "dump", "/sdcard/ui.xml")
    adb("pull", "/sdcard/ui.xml", "/tmp/ui_cur.xml")
    try:
        return ET.parse("/tmp/ui_cur.xml")
    except Exception:
        return None

def all_texts(tree):
    if tree is None: return set()
    return {n.get("text","") for n in tree.iter("node")}

def find_node(tree, *, text=None, desc=None, res=None):
    if tree is None: return None
    for n in tree.iter("node"):
        if text is not None and n.get("text","")         == text: return n
        if desc is not None and n.get("content-desc","") == desc: return n
        if res  is not None and res in n.get("resource-id",""):   return n
    return None

def find_nodes(tree, *, res):
    if tree is None: return []
    return [n for n in tree.iter("node") if res in n.get("resource-id","")]

def xy(node):
    if node is None: return None, None
    m = re.findall(r'\d+', node.get("bounds",""))
    if len(m) == 4:
        return (int(m[0])+int(m[2]))//2, (int(m[1])+int(m[3]))//2
    return None, None

def tap(node, wait=2):
    x, y = xy(node)
    if x is None: return False
    adb("shell", "input", "tap", str(x), str(y))
    time.sleep(wait)
    return True

def tap_text(text, wait=2):
    return tap(find_node(dump(), text=text), wait=wait)

def tap_desc(desc, wait=2):
    return tap(find_node(dump(), desc=desc), wait=wait)

def tap_res(res, wait=2):
    return tap(find_node(dump(), res=res), wait=wait)

def has_text(text):
    return find_node(dump(), text=text) is not None

def has_res(res):
    return find_node(dump(), res=res) is not None

def back(wait=2):
    adb("shell", "input", "keyevent", "4")
    time.sleep(wait)

def screenshot(name):
    path = os.path.join(SS_DIR, f"{name}.png")
    adb("shell", "screencap", "-p", "/sdcard/ss.png")
    adb("pull", "/sdcard/ss.png", path)
    print(f"  📸  {path}")
    return path

def dismiss_dialogs(wait=1):
    """Dismiss any welcome / permission / system dialogs."""
    tree = dump()
    for lbl in ("Looks Good", "Don't allow", "AGREE", "OK", "Dismiss", "Allow"):
        n = find_node(tree, text=lbl)
        if n:
            tap(n, wait)
            return True
    return False

def on_home():
    return find_node(dump(), res="home_day_title") is not None

def go_home(max_tries=10):
    """Navigate back to Home, dismissing any dialogs along the way."""
    for _ in range(max_tries):
        if on_home(): return True
        if dismiss_dialogs(): time.sleep(1)
        if on_home(): return True
        back(1)
    # Last resort: re-launch
    adb("shell", "am", "start", "-n", f"{PKG}/.ui.MainActivity")
    time.sleep(3)
    dismiss_dialogs()
    time.sleep(1)
    return on_home()

def scroll_down(steps=5):
    for _ in range(steps):
        adb("shell", "input", "swipe", "540", "1800", "540", "400", "500")
        time.sleep(0.6)

def type_text(text):
    # Escape special shell chars
    safe = text.replace(" ", "%s")
    adb("shell", "input", "text", safe)
    time.sleep(0.5)

# ─────────────────────────── results ───────────────────────────────────────
results = []

def check(label, cond, note=""):
    status = "✅ PASS" if cond else "❌ FAIL"
    results.append((status, label, note))
    line = f"  {status}: {label}"
    if note: line += f"  [{note}]"
    print(line)
    return bool(cond)

# ═══════════════════════════════════════════════════════════════════════════
print("=" * 62)
print("  OpenTransition — Automated Feature Test")
print("=" * 62)

# ── Setup ──────────────────────────────────────────────────────────────────
adb("logcat", "-c")
adb("shell", "am", "start", "-n", f"{PKG}/.ui.MainActivity")
time.sleep(4)
dismiss_dialogs()
time.sleep(1)

# ╔══════════════════════════════════════════════════════════════════════════╗
# ║  FEATURE 1 — Home Screen                                                ║
# ╚══════════════════════════════════════════════════════════════════════════╝
print("\n── FEATURE 1: Home Screen " + "─" * 35)
tree = dump()
check("Home loads (START DAY title)",        find_node(tree, res="home_day_title")          is not None)
check("Start date label present",            find_node(tree, res="home_start_date")         is not None)
check("Current date label present",          find_node(tree, res="home_current_date")       is not None)
check("Face Gallery button present",         find_node(tree, res="home_face_gallery")       is not None)
check("Body Gallery button present",         find_node(tree, res="home_body_gallery")       is not None)
check("Audio Gallery button present",        find_node(tree, res="home_audio_gallery")      is not None)
check("Take Photo shortcut present",         find_node(tree, desc="Take Photo")             is not None)
check("Edit Settings shortcut present",      find_node(tree, desc="Edit Settings")          is not None)
check("Milestones shortcut present",         find_node(tree, desc="Milestones")             is not None)
add_btns = find_nodes(tree, res="home_adapter_add_button")
check("3× add-photo row buttons present",    len(add_btns) >= 3)
screenshot("01_home")

# ╔══════════════════════════════════════════════════════════════════════════╗
# ║  FEATURE 2 — Settings                                                   ║
# ╚══════════════════════════════════════════════════════════════════════════╝
print("\n── FEATURE 2: Settings Screen " + "─" * 31)
go_home()
# Use resource-id tap for reliability
ok = tap_res("home_settings", wait=3)
check("Settings opens via gear button", ok)
tree = dump()
texts = all_texts(tree)
check("Start Date setting visible",           any("Start Date" in t for t in texts))
check("Lock Mode setting visible",            any("Lock Mode" in t for t in texts))
check("Theme setting visible",                any("Theme" in t for t in texts))
check("Security section visible",             any("Security" in t for t in texts))
check("Export / Import visible",              any("Export" in t for t in texts))
check("OpenTransition Account shown",         any("OpenTransition Account" in t for t in texts))
check("Decoy Vault option visible",           any("Decoy Vault" in t for t in texts))
check("Encrypted Database option visible",    any("Encrypted Database" in t for t in texts))
screenshot("02_settings_top")

# Scroll down to find Ads setting
scroll_down(4)
time.sleep(0.5)
screenshot("02b_settings_scrolled")
tree2 = dump()
texts2 = all_texts(tree2)
all_s = texts | texts2
check("Ads setting visible (after scroll)",   any("Ads" in t or "Ad " in t or "AdMob" in t
                                                   or "ad_free" in t.lower() or "Show Ads" in t
                                                   or "Hide Ads" in t or "ads" in t.lower()
                                                   for t in all_s))
back(2)

# ╔══════════════════════════════════════════════════════════════════════════╗
# ║  FEATURE 3 — Milestones                                                 ║
# ╚══════════════════════════════════════════════════════════════════════════╝
print("\n── FEATURE 3: Milestones " + "─" * 37)
go_home()
ok = tap_res("home_milestones", wait=3)
check("Milestones screen opens", ok)
tree = dump()
check("Milestones toolbar shown",             find_node(tree, res="milestones_toolbar")      is not None)
check("Milestones empty-state message shown", find_node(tree, res="milestones_empty_message") is not None)
screenshot("03a_milestones_empty")

# Add a milestone via the top-right '+' menu button
ok = tap(find_node(dump(), res="milestones_menu_add"), wait=2)
check("Add-Milestone form opens (menu '+')",  ok)
tree = dump()
texts = all_texts(tree)
print(f"  Form texts: {sorted(t for t in texts if t.strip())}")
title_node = find_node(tree, res="milestoneTitle")
check("Title field present in Add-Milestone form", title_node is not None)
desc_node  = find_node(tree, res="milestoneDescription")
check("Description field present",           desc_node is not None)
date_text  = any(re.match(r'\d{2}/\d{2}/\d{4}', t) for t in texts)
check("Date pre-filled in form",              date_text)
screenshot("03b_add_milestone_form")

if title_node is not None:
    tap(title_node, 0.5)
    type_text("Six_Month_Mark")
    time.sleep(0.5)
    ok_save = tap_text("Save", wait=3)
    if not ok_save:
        ok_save = tap_res("save_button", wait=3)
    check("Save tapped successfully",         ok_save)
    tree = dump()
    check("Saved milestone appears in list",
          any("Six_Month_Mark" in n.get("text","") for n in tree.iter("node")))
    screenshot("03c_milestone_saved")
else:
    back(2)

go_home()

# ╔══════════════════════════════════════════════════════════════════════════╗
# ║  FEATURE 4 — Full Gallery Screens (Face / Body / Audio)                 ║
# ╚══════════════════════════════════════════════════════════════════════════╝
print("\n── FEATURE 4: Gallery Screens " + "─" * 31)
go_home()

ok = tap_res("home_face_gallery", wait=3)
check("Face Gallery opens", ok)
tree = dump()
check("Face Gallery screen content shown",
      find_node(tree, text="Face Gallery") is not None or
      any("photo" in n.get("text","").lower() or "yet" in n.get("text","").lower()
          for n in tree.iter("node")))
screenshot("04a_gallery_face")
back(2); go_home()

ok = tap_res("home_body_gallery", wait=3)
check("Body Gallery opens", ok)
tree = dump()
check("Body Gallery screen content shown",
      find_node(tree, text="Body Gallery") is not None or
      any("photo" in n.get("text","").lower() or "yet" in n.get("text","").lower()
          for n in tree.iter("node")))
screenshot("04b_gallery_body")
back(2); go_home()

ok = tap_res("home_audio_gallery", wait=3)
check("Audio Gallery opens", ok)
tree = dump()
check("Audio Gallery screen content shown",
      find_node(tree, text="Audio Gallery") is not None or
      any("audio" in n.get("text","").lower() or "yet" in n.get("text","").lower()
          for n in tree.iter("node")))
screenshot("04c_gallery_audio")
back(2); go_home()

# ╔══════════════════════════════════════════════════════════════════════════╗
# ║  FEATURE 5 — Add Photo Source Picker                                    ║
# ╚══════════════════════════════════════════════════════════════════════════╝
print("\n── FEATURE 5: Add Photo Source Picker " + "─" * 23)
go_home()
tree = dump()
add_btns = find_nodes(tree, res="home_adapter_add_button")
check("Add-photo row buttons present on home", len(add_btns) >= 1)

if add_btns:
    tap(add_btns[0], wait=3)   # face row
    tree = dump()
    has_camera  = find_node(tree, text="Camera")  is not None
    has_gallery = find_node(tree, text="Gallery") is not None
    check("Source picker: 'Camera' option shown",  has_camera)
    check("Source picker: 'Gallery' option shown", has_gallery)
    screenshot("05_source_picker")
    # Dismiss picker cleanly – press back
    back(2)
    go_home()

# ╔══════════════════════════════════════════════════════════════════════════╗
# ║  FEATURE 6 — Camera Screen                                              ║
# ╚══════════════════════════════════════════════════════════════════════════╝
print("\n── FEATURE 6: Camera Screen " + "─" * 33)
go_home()
# Open the source picker for face row fresh
tree = dump()
add_btns = find_nodes(tree, res="home_adapter_add_button")
if add_btns:
    tap(add_btns[0], wait=3)
    # Select 'Camera'
    cam_node = find_node(dump(), text="Camera")
    if cam_node:
        ok = tap(cam_node, wait=4)
        check("Camera selected from source picker", ok)
        tree = dump()
        check("Camera preview_view loaded",         find_node(tree, res="preview_view")    is not None)
        check("Capture button present (btn_capture)",
              find_node(tree, res="btn_capture") is not None or
              find_node(tree, desc="Take Photo") is not None)
        check("Toggle-overlay button present",      find_node(tree, desc="Toggle overlay") is not None)
        check("Back button present on camera",
              find_node(tree, res="btn_back") is not None or
              find_node(tree, desc="Back") is not None)
        screenshot("06_camera")
        back(2)
    else:
        check("Camera option found in source picker", False)
        back(2)
else:
    check("Add button available for camera test", False)
go_home()

# ╔══════════════════════════════════════════════════════════════════════════╗
# ║  FEATURE 7 — Gallery / Photo Picker                                     ║
# ╚══════════════════════════════════════════════════════════════════════════╝
print("\n── FEATURE 7: Photo Picker (Gallery source) " + "─" * 17)
go_home()
tree = dump()
add_btns = find_nodes(tree, res="home_adapter_add_button")
if add_btns:
    tap(add_btns[0], wait=3)
    gal_node = find_node(dump(), text="Gallery")
    if gal_node:
        ok = tap(gal_node, wait=4)
        check("Gallery selected from source picker", ok)
        tree = dump()
        texts = all_texts(tree)
        print(f"  Picker texts: {sorted(t for t in texts if t.strip())[:15]}")
        on_picker = any("Photos" in t or "Images" in t or "Gallery" in t
                        or "Select" in t or "Media" in t or "Album" in t
                        for t in texts)
        check("Photo picker / gallery screen shown", on_picker)
        screenshot("07_photo_picker")
        back(2)
    else:
        check("Gallery option found in source picker", False)
        back(2)
else:
    check("Add button available for gallery-picker test", False)
go_home()

# ╔══════════════════════════════════════════════════════════════════════════╗
# ║  FEATURE 8 — Record Audio                                               ║
# ╚══════════════════════════════════════════════════════════════════════════╝
print("\n── FEATURE 8: Record Audio " + "─" * 35)
go_home()
tree = dump()
add_btns = find_nodes(tree, res="home_adapter_add_button")
check("Audio add button present (3rd row)", len(add_btns) >= 3)
if len(add_btns) >= 3:
    tap(add_btns[2], wait=3)     # 3rd = audio row
    dismiss_dialogs(1)
    tree = dump()
    texts = all_texts(tree)
    print(f"  Record Audio texts: {sorted(t for t in texts if t.strip())}")
    check("Record Audio screen title shown",    any("Record Audio" in t for t in texts))
    check("Countdown timer '00:00' shown",      any("00:00" in t for t in texts))
    check("Save Audio button present",          any("Save Audio" in t for t in texts))
    check("Cancel button present",              any("Cancel" in t for t in texts))
    check("Date pre-filled on record screen",
          any(re.match(r'\d{2}/\d{2}/\d{4}', t) for t in texts))
    screenshot("08_record_audio")
    back(2)
go_home()

# ╔══════════════════════════════════════════════════════════════════════════╗
# ║  FEATURE 9 — Back-Stack / Navigation Integrity                          ║
# ╚══════════════════════════════════════════════════════════════════════════╝
print("\n── FEATURE 9: Back-Stack Integrity " + "─" * 26)
go_home()

# Settings → back → home
# Use resource-id for reliable tap
ok_settings = tap_res("home_settings", wait=3)
check("Settings opened for back-stack test",  ok_settings and (has_text("Start Date") or has_text("Lock Mode")))
back(2)
check("Back from Settings → Home",            on_home())

# Milestones → back → home
tap_res("home_milestones", wait=3)
check("Milestones opened for back-stack test", has_text("Milestones"))
back(2)
check("Back from Milestones → Home",          on_home())

# Camera → back → home
tree = dump()
add_btns = find_nodes(tree, res="home_adapter_add_button")
if add_btns:
    tap(add_btns[0], wait=2)
    if has_text("Camera"):
        tap_text("Camera", wait=3)
        back(2)
    else:
        back(2)
check("Back from Camera → Home",              on_home())
screenshot("09_back_stack")

# ╔══════════════════════════════════════════════════════════════════════════╗
# ║  FEATURE 10 — Screen Rotation (Config Change)                           ║
# ╚══════════════════════════════════════════════════════════════════════════╝
print("\n── FEATURE 10: Screen Rotation " + "─" * 30)
go_home()
adb("shell", "settings", "put", "system", "accelerometer_rotation", "0")
adb("shell", "settings", "put", "system", "user_rotation", "1")   # landscape
time.sleep(3)
check("App survives rotation to landscape", on_home())
screenshot("10a_landscape")
adb("shell", "settings", "put", "system", "user_rotation", "0")   # portrait
time.sleep(3)
check("App survives rotation back to portrait", on_home())
screenshot("10b_portrait")

# ╔══════════════════════════════════════════════════════════════════════════╗
# ║  FEATURE 11 — Empty Gallery States                                      ║
# ╚══════════════════════════════════════════════════════════════════════════╝
print("\n── FEATURE 11: Empty Gallery States " + "─" * 25)
go_home()

tap_res("home_face_gallery", wait=3)
tree = dump()
check("Face Gallery: empty state, no crash",
      find_node(tree, text="Face Gallery") is not None or
      any("yet" in n.get("text","").lower() or "photo" in n.get("text","").lower()
          for n in tree.iter("node")))
screenshot("11a_gallery_face_empty")
back(2); go_home()

tap_res("home_body_gallery", wait=3)
tree = dump()
check("Body Gallery: empty state, no crash",
      find_node(tree, text="Body Gallery") is not None or
      any("yet" in n.get("text","").lower() or "photo" in n.get("text","").lower()
          for n in tree.iter("node")))
screenshot("11b_gallery_body_empty")
back(2); go_home()

tap_res("home_audio_gallery", wait=3)
tree = dump()
check("Audio Gallery: empty state, no crash",
      find_node(tree, text="Audio Gallery") is not None or
      any("yet" in n.get("text","").lower() or "audio" in n.get("text","").lower()
          for n in tree.iter("node")))
screenshot("11c_gallery_audio_empty")
back(2)

# ╔══════════════════════════════════════════════════════════════════════════╗
# ║  CRASH / ANR / FATAL CHECK                                              ║
# ╚══════════════════════════════════════════════════════════════════════════╝
print("\n── Crash / ANR / Fatal Check " + "─" * 33)
log  = adb("logcat", "-d")
fatal = [l for l in log.splitlines() if "FATAL EXCEPTION" in l]
anr   = [l for l in log.splitlines() if "ANR"             in l and PKG in l]
pid   = adb("shell", "pidof", PKG)
check("No FATAL EXCEPTIONs in logcat",  len(fatal) == 0, f"{len(fatal)} found")
check("No ANRs for app in logcat",       len(anr)   == 0, f"{len(anr)} found")
check("App process alive at test end",   bool(pid.strip()))
if fatal:
    for l in fatal[:3]: print(f"    FATAL: {l}")

# ═══════════════════════════════════════════════════════════════════════════
# SUMMARY
# ═══════════════════════════════════════════════════════════════════════════
print("\n" + "=" * 62)
print("  FINAL TEST SUMMARY")
print("=" * 62)
passed = [r for r in results if "✅" in r[0]]
failed = [r for r in results if "❌" in r[0]]
for s, l, n in results:
    print(f"{s}  {l}" + (f"  [{n}]" if n else ""))
print(f"\n  ✅  Passed : {len(passed)} / {len(results)}")
print(f"  ❌  Failed : {len(failed)} / {len(results)}")
if failed:
    print("\n  Failed checks:")
    for _, l, n in failed:
        print(f"    • {l}" + (f" [{n}]" if n else ""))

sys.exit(0 if not failed else 1)
