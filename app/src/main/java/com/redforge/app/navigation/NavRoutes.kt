package com.redforge.app.navigation

/** Central list of navigation destinations, kept as plain route strings for readability/upgradability. */
object NavRoutes {
    const val SPLASH = "splash"
    const val PRIVACY_POLICY = "privacy_policy"
    const val ONBOARDING_TUTORIAL = "onboarding_tutorial"

    const val HOME = "home"
    const val HISTORY = "history"
    const val HISTORY_DETAIL = "history_detail/{sessionId}"
    const val SPLIT_LIST = "split_list"
    const val SPLIT_EDITOR = "split_editor/{splitId}"
    const val SPLIT_DAY_EDITOR = "split_day_editor/{dayId}"
    const val EXERCISE_LIBRARY = "exercise_library/{pickerMode}"
    const val EXERCISE_EDITOR = "exercise_editor/{exerciseId}"
    const val EXERCISE_DETAIL = "exercise_detail/{exerciseId}"
    const val EXERCISE_PROGRESS = "exercise_progress/{exerciseId}"

    const val ACTIVE_WORKOUT = "active_workout"

    const val PROGRESS_DASHBOARD = "progress_dashboard"
    const val PHOTO_TRACKING = "photo_tracking"
    const val BODY_MEASUREMENTS = "body_measurements"

    const val SETTINGS = "settings"
    const val SHARE_RESULT = "share_result/{scope}"

    fun historyDetail(sessionId: Long) = "history_detail/$sessionId"
    fun exerciseProgress(exerciseId: Long) = "exercise_progress/$exerciseId"
    fun splitEditor(splitId: Long) = "split_editor/$splitId"
    fun splitDayEditor(dayId: Long) = "split_day_editor/$dayId"
    fun exerciseLibrary(pickerMode: Boolean) = "exercise_library/$pickerMode"
    fun exerciseEditor(exerciseId: Long) = "exercise_editor/$exerciseId"
    fun exerciseDetail(exerciseId: Long) = "exercise_detail/$exerciseId"
    fun shareResult(scope: String) = "share_result/$scope"

    const val NEW_ID = "0" // sentinel meaning "create new" for editor routes
}
