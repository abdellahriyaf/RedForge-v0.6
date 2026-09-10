# RedForge v0.6.1

A local-first bodybuilding and workout progress tracker for Android. Built with Kotlin, Jetpack Compose, Room, and DataStore.

## Current release

**RedForge v0.6.1** is the optimization and hardening release on top of v0.6.

- Application ID: `com.redforge.app.v06`
- Version: `0.6.1`
- versionCode: `7`
- v0.6.1 keeps the local-first model and does not add a network/backend layer.

## v0.6 foundation

v0.6 expands the exercise system into a curated catalog with searchable aliases, muscle/equipment/movement metadata, technique instructions, key cues, common mistakes, difficulty, rep ranges, and source metadata. Custom exercises remain supported.

History and progress screens use the same persisted Room session/set data as the workout flow. Backup and restore remain explicit user actions.

Strong Ember demonstrations are intentionally deferred to v0.7.
Gemini integration is intentionally deferred to v0.8.

## v0.6.1 optimization and hardening

### Performance
- Added database indexes for the high-frequency session, set, split, exercise, photo, and measurement queries.
- Replaced several read-heavy paths with SQL aggregates, projections, and targeted queries.
- Reduced repeated work in history, progress, sharing, and home statistics.
- Cached completed exercise-history lookups used by active-workout PR detection so the 1,000-row history window is not re-read from Room on every set tap.
- Reduced avoidable allocations in strength/volume calculations.
- Moved progress-photo filesystem copying to an IO dispatcher.
- Enabled Gradle build caching, configuration cache, and Kotlin incremental compilation.

### Data safety
- Kept real additive Room migrations rather than destructive fallback migration.
- Added migration regression tests for the v1 -> v2 -> v3 -> v4 chain.
- Backup/restore validation keeps archive allowlisting, size limits, SQLite header validation, staging, and rollback.
- Restore still requires an explicit confirmation before replacing live data.

### Engineering quality
- Added `.gitignore` for Android build outputs, local properties, IDE files, and local secrets.
- Added GitHub Actions CI running build, unit tests, and Android lint on pushes and pull requests.
- Added unit coverage for core strength/volume formulas and migration statements.
- Kept settings screen scrolling and lifecycle-aware Compose state collection.

## Architecture

```text
UI / Compose
   ↓
ViewModel
   ↓
Repository
   ↓
Room / DataStore
```

The repository layer intentionally stays thin; business rules remain in ViewModels/domain code while persistence stays in Room/DataStore.

## Privacy

RedForge remains local-first in v0.6.1. Workout data, settings, measurements, and progress photos stay on the device unless the user explicitly exports a backup. There is no Gemini/network layer in this release.

## Release roadmap

- v0.7 — Cute Ember + Strong Ember / exercise demonstration system
- v0.8 — Gemini integration
- Then bug fixes, polish, and final improvements toward v1

## Development

Open the repository in Android Studio and work from the release branch you intend to test. The `main` branch is kept as the stable baseline; release work can be tested on its corresponding version branch.

GitHub Actions provides the automated build/test/lint gate for changes pushed to the repository.
