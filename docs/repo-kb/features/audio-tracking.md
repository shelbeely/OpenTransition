# Feature: Audio Tracking (Voice)

> ⚠️ **Critical correctness note**: As of this pass, the per-recording metrics
> are **real measurements** of the recorded audio for everything labelled below as
> "Computed". The **formant fields (`f1Mean`..`f4Mean`) are persisted as `0f`** —
> no LPC/cepstral formant analysis runs ([`util/AudioAnalysisUtil.kt:29, 117`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/AudioAnalysisUtil.kt)).
> This contradicts the historical audit text in [`audit-report/07-issues-and-bugs.md`](../../../audit-report/07-issues-and-bugs.md) ISSUE-005,
> which has been partially superseded by [`audit-report/18-refresh-2026-04-29.md`](../../../audit-report/18-refresh-2026-04-29.md).

## Summary

The user records a short audio clip (phone or watch). The clip is decoded to PCM, an autocorrelation-based pitch tracker runs over overlapping frames, and a row of `AudioAnalysisEntity` is written. Progress over time is plotted in `VoiceProgressFragment`. Per-recording details are shown in `VoiceSessionDetailFragment`. Users define their own goals (no presets, no gendered defaults — see [`VoiceGoalEntity.kt:30-34`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/entities/VoiceGoalEntity.kt)).

## Entry points

| Surface | Component | Path |
|---|---|---|
| Record on phone | `RecordAudioFragment` + `RecordAudioScreen` | [`ui/recordaudio/`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/recordaudio/) |
| Record on watch | `wear/AudioRecordActivity` (channel-streamed to phone) | [`wear/AudioRecordActivity.kt`](../../../wear/src/main/java/com/shelbeely/opentransition/wear/AudioRecordActivity.kt) |
| Progress over time | `VoiceProgressFragment` + `VoiceProgressScreen` | [`ui/voiceprogress/`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/voiceprogress/) |
| Per-session detail | `VoiceSessionDetailFragment` + `VoiceSessionDetailScreen` | [`ui/voicesession/`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/voicesession/) |
| Goal creation | `GoalCreationSheet` | [`ui/voicegoal/GoalCreationSheet.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/voicegoal/GoalCreationSheet.kt) |

Custom drawing views: `PitchProgressionView`, `FormantChartView`, `SpectrogramView`, `WaveformView` ([`ui/widget/`](../../../mobile/src/main/java/com/shelbeely/opentransition/ui/widget/)).

## State / domain

- `VoiceGoalRepository` (per-call, not in `DomainManager`) — [`domain/VoiceGoalRepository.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/domain/VoiceGoalRepository.kt).
- No `ViewModel`. Fragments use `lifecycleScope.launch` + DAO `Flow`s.

## Computed vs stubbed voice metrics

Source of truth: [`util/AudioAnalysisUtil.kt:48-118`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/AudioAnalysisUtil.kt).

| Column on `AudioAnalysisEntity` | Status | How computed |
|---|---|---|
| `f0Mean` | **Computed** | Mean of voiced-frame `PitchTracker` outputs |
| `f0Min`, `f0Max` | **Computed** | Min/max over voiced frames |
| `f0StdDev` | **Computed** | sqrt of variance over voiced-frame F0s |
| `pitchConfidenceMean` | **Computed** | Mean autocorrelation confidence |
| `voicedRatio` | **Computed** | `voicedFrames / totalFrames` |
| `intensityMeanDb`, `intensityMaxDb` | **Computed** | RMS dB across all frames |
| `pitchRangeHz` | **Computed** | `f0Max − f0Min` |
| `pitchStabilityScore` | **Computed** | `1 − (stdDev / mean)`, clamped to `[0, 1]` |
| `intonationMovement` | **Computed** | Mean `\|Δf0\|` between consecutive voiced frames |
| `durationSeconds` | **Computed** | `samples.size / sampleRate` |
| `analysisTimestamp` | **Computed** | `System.currentTimeMillis()` at analysis |
| `f1Mean`..`f4Mean` | **Stubbed (zeroed)** | LPC/cepstrum formant extraction is not implemented; the fields are left at `0f` and the doc-comment marks them as zero ([`AudioAnalysisUtil.kt:117`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/AudioAnalysisUtil.kt)) |
| `sessionSummaryText` | **Computed elsewhere** | Built by [`util/SessionSummaryBuilder.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/SessionSummaryBuilder.kt) and persisted by callers |
| `transcript` (Realm only; not in `AudioAnalysisEntity`) | Optional via [`util/SpeechTranscriptionManager.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/SpeechTranscriptionManager.kt) | May be empty |

DSP units: [`util/AudioDecoder.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/AudioDecoder.kt) decodes to PCM; [`util/PitchTracker.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/PitchTracker.kt) runs autocorrelation pitch detection.

## Goals (target ranges)

`VoiceGoalEntity` ([`database/room/entities/VoiceGoalEntity.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/entities/VoiceGoalEntity.kt)) stores `{ id, name, metricKey, targetMin, targetMax, createdAt }`. Allowed `metricKey` values mirror [`util/VoiceMetric.kt`](../../../mobile/src/main/java/com/shelbeely/opentransition/util/VoiceMetric.kt): `f0Mean`, `pitchRangeHz`, `voicedRatio`, `pitchStabilityScore`, `intonationMovement`.

## Persistence touched

| Data | Sink |
|---|---|
| `AudioAnalysisEntity` | Room `audio_analysis` via [`AudioAnalysisDao`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/dao/AudioAnalysisDao.kt) |
| `VoiceGoalEntity` | Room `voice_goals` via [`VoiceGoalDao`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/dao/VoiceGoalDao.kt) |
| Audio file bytes | App-private under `filesDir/audio/` (mobile) or sent via Wear `ChannelClient` (watch) |
| Phone-recorded clip ↔ photo row | Each clip becomes a `Photo` row with `type = TYPE_AUDIO = 2` so it appears in Gallery alongside images ([`Photo.kt:69`](../../../mobile/src/main/java/com/shelbeely/opentransition/data/Photo.kt), [`MobileWearableListenerService.saveAudioToRealm`](../../../mobile/src/main/java/com/shelbeely/opentransition/wear/MobileWearableListenerService.kt)) |

## External APIs

- **Wearable Data Layer** `ChannelClient` for streaming AMR/3GP bytes from watch to phone (`PATH_AUDIO_DATA + "/<filename>"`) — fixes the audit-report ISSUE-010 100 KB cap. See [`apis/wearable-data-layer.md`](../apis/wearable-data-layer.md).
- **Android `MediaRecorder`** on the watch.
- **Android `SpeechRecognizer`** optional, via `SpeechTranscriptionManager`.

## Known issues (audit cross-reference)

- 🔴 ISSUE-005 — historical claim that all formant numbers were faked. **Partially fixed.** F0 metrics and intensity/voiced-ratio are now real measurements; **formants are still zero** by design.
- 🟡 ISSUE-009 — watch `AudioRecordActivity` recursive `postDelayed` leak window.
- 🟠 ISSUE-010 — historical 100 KB DataItem cap. **Fixed** by ChannelClient streaming ([`MobileWearableListenerService.kt:113-160`](../../../mobile/src/main/java/com/shelbeely/opentransition/wear/MobileWearableListenerService.kt)).

## Invariants

- **No gendered defaults**: goal targets are user-defined ([`VoiceGoalEntity.kt:18`](../../../mobile/src/main/java/com/shelbeely/opentransition/database/room/entities/VoiceGoalEntity.kt)). Any rewrite must preserve this.
- `metricKey` strings exactly match column names on `AudioAnalysisEntity`. Changing one without the other silently zeroes a chart.

## Tests

None as of this pass (`grep -rln "AudioAnalysisUtil\|PitchTracker" mobile/src/test` returns no results).
