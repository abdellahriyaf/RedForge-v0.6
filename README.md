# RedForge v0.5

A local-first bodybuilding progress tracker for Android. Built with Kotlin,
Jetpack Compose, and Room. No account, no server, and no network permission in
v0.5.

## v0.5 identity and side-by-side testing

**v0.4 is intentionally preserved as a separate app.** v0.4 uses application
ID `com.redforge.app.v04`; v0.5 uses `com.redforge.app.v05` and version `0.5.0`.
Android therefore treats them as two independent installs, so you can keep both
for direct comparison. v0.5 has its own local database and settings on a fresh
install. Nothing in v0.5 modifies the data stored by v0.4.

To compare with the same workout history, export a backup from v0.4 and import
it into v0.5. The existing backup/restore path remains local and explicit.

## What changed in v0.5

### History
- History rows are tappable and open a full workout detail screen.
- Completed sessions show exercise-by-exercise set history.
- Past sets can be edited for weight, reps, and RPE.
- Past sets can be deleted with confirmation and remaining set numbers are renumbered.
- Warm-up sets remain visible but are excluded from working-set volume summaries.

### Progress
- Each lift progress card opens a dedicated trend screen.
- Users can switch between estimated 1RM and per-session volume.
- The chart and recent-session list are derived from the local Room history.
- Warm-ups are excluded from e1RM and working-volume metrics.

### Sharing
- Added Summary, Streak, and Volume share-card templates.
- Shared image filenames include template + time range.
- Share totals focus on actual working sets and working volume.

### Widget
- Today's state now distinguishes training complete, rest day, and a startable session.
- Added a one-tap START action for a startable training day.
- OPEN remains available even when today's action is unavailable.

### Existing v0.4 foundation retained
- Local Room persistence and data-loss protection
- Calendar-based scheduling and distinct-day streaks
- Deload and superset behavior
- Camera/photo tracking and bottom navigation on progress subpages
- Hardened backup/export/import
- Home media controls and Spotify launcher
- Dark theme and timer settings

## Architecture

The domain/data/UI separation remains intentionally manual and approachable:

```text
UI / Compose
   ↓
ViewModel
   ↓
Repository
   ↓
Room / DataStore
```

History and progress detail screens read the same persisted set/session rows as
the rest of the app, so there is no secondary history database.

## Privacy

v0.5 remains local-first. Workout data, settings, measurements, and progress
photos stay on the device unless the user explicitly exports a backup. There
is no Gemini/network layer in v0.5.

## Known limitations / next steps

- The Android SDK/Gradle wrapper binary is not available in this preparation
environment, so the final compile and device test must still be done in
Android Studio.
- The exercise library still contains the small seeded set; v0.6 is the
planned expansion to a much richer catalog with structured metadata and
licensed/original demonstrations.
- Final Cute Ember animation work is planned for v0.7.
- Gemini is planned for v0.8.

## App identity

| Build | Application ID | Version |
|---|---|---|
| RedForge v0.3 | `com.redforge.app` | legacy baseline |
| RedForge v0.4 | `com.redforge.app.v04` | 0.4.0 |
| **RedForge v0.5** | **`com.redforge.app.v05`** | **0.5.0** |
```

## v0.6 — Exercise Library Expansion

v0.6 expands RedForge's exercise system to a curated 100+ exercise catalog with searchable aliases, muscle/equipment/movement metadata, technique instructions, key cues, common mistakes, difficulty and typical rep/time ranges. The library and custom exercise editor are designed to remain offline-first.

Exercise demonstration media is intentionally deferred to v0.7, where Strong Ember will become the visual trainer. The v0.6 schema already reserves a demonstration asset field so the later system can attach media without redesigning the core exercise model.
