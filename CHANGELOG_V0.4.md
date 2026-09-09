# RedForge v0.4 — Changelog

This release is based on the user's RedForge v0.3 source tree. The original v0.3
build remains untouched in the source archive so it can stay installed side-by-side
on Android.

## Reliability
- Removed destructive Room migration fallback.
- Serialized workout writes.
- Atomic-ish transactional session deletion + set reindexing.
- Prevented empty workouts from polluting stats.
- Stops rest timer when leaving workout.

## Scheduling / streaks
- Rest days are real calendar slots.
- Missed days advance the planned cycle.
- Streaks deduplicate same-day sessions and use local calendar dates.
- Derived streak values no longer write back through Home's combined state flow.

## Workout
- Real deload target reduction (40%).
- Optional RPE.
- Fresh set-index allocation.
- Improved PR baseline.
- Default rest fallback.
- 1.25 lb plate support.

## Home / media
- New History tab and weekly summary.
- Spotify action moved from Settings to Home's Media card.
- Android MediaSession controls remain optional.
- Immediate widget refresh after key split/workout changes.

## Settings / privacy
- Dark theme, timer sound, and vibration settings are wired.
- Notification permission is contextual to starting a rest timer.
- Unused CAMERA permission removed.
- kg/lb switching converts existing logged set weights.

## Backup / restore
- Live Room instance remains open during export.
- Staged restore with path/size validation and SQLite header validation.
- Best-effort rollback and clean restart after successful restore.
- v0.3 legacy backup markers are accepted.

## v0.4 FIX3 follow-up
- Added real in-app camera capture for progress photos with runtime camera permission.
- Kept gallery import available alongside camera capture.
- Kept the bottom navigation visible on Photo Tracking and Body Measurements routes, with Progress remaining selected.
- Reworked backup import to use the system document picker, validate off the UI thread, validate SQLite integrity, and restore through a staged copy with rollback support.
- Fixed temporary camera-file cleanup so the captured image is not deleted before the asynchronous photo save finishes.

## v0.4 FIX4

- Fixed invalid outlined-delete icon references across split/progress screens.
- Fixed the active-workout `mapNotNull` compile issue.
- Added mid-day split-switch confirmation with **Start today** / **Start tomorrow**.
- Added a persisted schedule anchor so a new split can begin on the next calendar day without repeating today's workout.
- Home now clearly shows **Starts tomorrow** when a newly activated split is anchored to the next day.

## v0.4 FIX4 — stabilization pass

- Fixed remaining `Delete` icon references that could fail compilation on the project’s current Compose icon set.
- Fixed the `ActiveWorkoutViewModel` `mapNotNull` control-flow compile error.
- Added safe mid-day split switching: if a workout was already completed today, RedForge asks whether the new split should start today or tomorrow.
- Persisted the split schedule anchor locally so “Start tomorrow” does not repeat the current day.
- Home and the widget now respect the same schedule anchor and show “Starts tomorrow” before the new split begins.
- Clarified the Backup & Restore controls and made the dark-theme behavior explicit in Settings.
