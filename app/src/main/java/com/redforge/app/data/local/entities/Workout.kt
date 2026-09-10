package com.redforge.app.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One workout instance — a specific date the user trained a given split day.
 * Created the moment the user opens "Start Workout" and immediately written
 * to Room, so it exists on disk before a single set is even logged. This is
 * the anchor that lets us survive process death: on relaunch we just look
 * for a session with [completed] == false and resume it.
 */
@Entity(
    tableName = "workout_sessions",
    indices = [
        Index(value = ["completed", "startedAt"]),
        Index(value = ["splitDayId", "startedAt"])
    ]
)
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val splitDayId: Long?,
    val splitDayNameSnapshot: String,
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val completed: Boolean = false,
    val notes: String = ""
)

@Entity(
    tableName = "set_entries",
    indices = [
        Index(value = ["workoutSessionId", "exerciseId", "setIndex"]),
        Index(value = ["exerciseId", "loggedAt"])
    ]
)
data class SetEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutSessionId: Long,
    val exerciseId: Long,
    val setIndex: Int,
    val weight: Double,
    val reps: Int,
    val isWarmup: Boolean = false,
    val rpe: Float? = null,
    val completed: Boolean = true,
    val isPersonalRecord: Boolean = false,
    val loggedAt: Long = System.currentTimeMillis()
)
