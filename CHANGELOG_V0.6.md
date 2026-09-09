# RedForge v0.6 — FIX8

- Fixed finish-workout lifecycle: completion is persisted first, then navigation returns to the existing Home destination.
- Removed widget refresh from the critical finish/discard path so widget updates cannot crash or interrupt workout completion.
- Added guarded finish/discard state with a Saving indicator and safe error handling.
- Made the Settings screen vertically scrollable.
- Stacked Export Backup and Restore Backup as two clearly separate full-width actions.
- Kept v0.6 package/version unchanged so this is a drop-in source fix.

- Finish flow no longer triggers the widget broadcast before navigation; the navigation callback is executed from a Compose `LaunchedEffect` only after Room confirms completion.
