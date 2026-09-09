# RedForge v0.6.1

## Performance & efficiency

- Added Room indexes for high-frequency session, set, split, exercise, photo, and measurement queries.
- Replaced several N+1 queries with SQL aggregates, projections, and targeted queries.
- Reduced Room transaction/invalidation overhead for split activation/deletion and day exercise replacement.
- Reduced memory allocations in progress metrics and strength-formula hot paths.
- Moved progress-photo filesystem work to `Dispatchers.IO`.
- Made Compose state collection lifecycle-aware and reduced eager ViewModel subscriptions.
- Cached active-workout PR reference data to avoid database reads on every set.
- Replaced calendar day walking with constant-time civil-date arithmetic.
- Preserved R8/resource shrinking and added Gradle caching/configuration-cache settings.
- Added Room migration 3 -> 4 for performance indexes.

## Compatibility

- Same application ID as v0.6: `com.redforge.app.v06`.
- Patch version: `0.6.1`, versionCode `7`.
- No Ember or exercise demonstration media added; those remain scheduled for v0.7.
