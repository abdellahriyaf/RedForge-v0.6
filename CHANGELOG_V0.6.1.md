# RedForge v0.6.1

v0.6.1 is the optimization, reliability, and engineering-hardening pass on top of v0.6.

## Performance & efficiency

- Added Room indexes for high-frequency session, set, split, exercise, photo, and measurement queries.
- Replaced several N+1/read-heavy paths with SQL aggregates, projections, and targeted queries.
- Reduced Room transaction/invalidation overhead for split activation/deletion and day-exercise replacement.
- Reduced memory allocations in progress metrics and strength-formula hot paths.
- Moved progress-photo filesystem work to `Dispatchers.IO`.
- Cached the bounded completed-history window used by active-workout PR detection so repeated set logging does not repeatedly reload the same 1,000 historical rows.
- Enabled Gradle build caching, configuration cache, and Kotlin incremental compilation.
- Kept R8/resource shrinking enabled for release builds.
- Added Room migration 3 -> 4 for performance indexes.

## Reliability & data safety

- Preserved real additive Room migrations with no destructive migration fallback.
- Added regression tests covering the migration chain and every migration's intended SQL operations.
- Hardened backup/restore with archive allowlisting, size limits, SQLite header validation, staging, rollback, and explicit restore confirmation.
- Fixed the v0.6.1 `DataBackupUtil.exportBackup()` compile error caused by using `return` inside an expression-body function.
- Kept completed-workout data persistent before set logging and preserved the active-workout mutex against rapid double taps.

## Engineering quality

- Added `.gitignore` for Android build output, `local.properties`, IDE metadata, local secrets, and temporary files.
- Added GitHub Actions CI for build, unit tests, and Android lint on pushes and pull requests.
- Added unit coverage for strength/volume formulas and migration regressions.
- Configured Room schema export into `app/schemas` so schema snapshots can be generated and reviewed with the migration code.
- Kept the settings screen scrollable and Compose state collection lifecycle-aware.
- Updated README and release documentation to match the actual v0.6.1 branch.

## Compatibility / scope

- Application ID: `com.redforge.app.v06`.
- Version: `0.6.1`, versionCode `7`.
- No Ember or exercise demonstration media added; those remain scheduled for v0.7.
- No Gemini/network backend added; Gemini remains scheduled for v0.8.
- Internationalization is intentionally deferred until the release direction is finalized rather than beginning a large string-resource migration during this optimization pass.
