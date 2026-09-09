# RedForge v0.5

The big non-AI upgrade built on the v0.4 foundation. v0.4 remains installable
separately as `com.redforge.app.v04`; v0.5 uses `com.redforge.app.v05`.

## History
- History rows are now tappable and open a full workout detail view.
- Completed sessions show exercise-by-exercise set history.
- Past sets can be corrected for weight, reps, and optional RPE.
- Past sets can be deleted with a confirmation and are automatically renumbered.
- History summaries count working sets and working-set volume rather than treating warm-ups as training volume.

## Progress
- Progress cards now open a per-exercise detail screen.
- Added selectable estimated-1RM and session-volume trend charts using the existing local workout data.
- Recent session trend rows show the date and the selected metric.
- Progress summaries now exclude warm-up sets from e1RM and working volume.

## Sharing
- Added three share-card templates: Summary, Streak, and Volume.
- Image filenames identify the selected template and time range.
- Share totals now focus on working sets and working-set volume.

## Widget
- Widget now distinguishes completed training days and rest days.
- Added a one-tap START action when today's scheduled session is startable.
- OPEN remains available for everything else.
- The same local Room schedule and streak sources continue to power the widget.

## App identity
| Build | Application ID | Version |
|---|---|---|
| RedForge v0.3 | `com.redforge.app` | legacy baseline |
| RedForge v0.4 | `com.redforge.app.v04` | 0.4.0 |
| **RedForge v0.5** | **`com.redforge.app.v05`** | **0.5.0** |

## Intentional scope
- No Gemini integration yet.
- No new network dependency.
- Exercise-library expansion and Strong Ember are reserved for v0.6.
- Cute Ember's final animation system is reserved for v0.7.
