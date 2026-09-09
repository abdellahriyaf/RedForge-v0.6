package com.redforge.app

import android.app.Application
import com.redforge.app.data.datastore.SettingsDataStore
import com.redforge.app.data.local.db.RedForgeDatabase
import com.redforge.app.data.repository.ExerciseRepository
import com.redforge.app.data.repository.ProgressRepository
import com.redforge.app.data.repository.SplitRepository
import com.redforge.app.data.repository.WorkoutRepository

/**
 * Deliberately simple manual dependency graph (no Hilt/Koin) so the project
 * stays approachable to read and extend — swap in a DI framework later if
 * the codebase grows past what this comfortably supports.
 */
class RedForgeApplication : Application() {

    lateinit var database: RedForgeDatabase
        private set
    lateinit var settingsDataStore: SettingsDataStore
        private set
    lateinit var exerciseRepository: ExerciseRepository
        private set
    lateinit var splitRepository: SplitRepository
        private set
    lateinit var workoutRepository: WorkoutRepository
        private set
    lateinit var progressRepository: ProgressRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = RedForgeDatabase.getInstance(this)
        settingsDataStore = SettingsDataStore(this)
        exerciseRepository = ExerciseRepository(database.exerciseDao())
        splitRepository = SplitRepository(database.splitDao())
        workoutRepository = WorkoutRepository(database.workoutDao())
        progressRepository = ProgressRepository(database.progressDao())
    }
}
