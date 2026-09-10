package com.redforge.app.data.local.dao

import androidx.room.*
import com.redforge.app.data.local.entities.SetEntry
import com.redforge.app.data.local.entities.WorkoutSession
import kotlinx.coroutines.flow.Flow

data class HistorySessionStats(
    val sessionId: Long,
    val splitDayNameSnapshot: String,
    val startedAt: Long,
    val endedAt: Long?,
    val setCount: Int,
    val workingSetCount: Int,
    val totalVolume: Double
)

data class SetStats(
    val setCount: Int,
    val totalVolume: Double
)

data class ExerciseProgressSet(
    val id: Long,
    val workoutSessionId: Long,
    val exerciseId: Long,
    val setIndex: Int,
    val weight: Double,
    val reps: Int,
    val isWarmup: Boolean,
    val rpe: Float?,
    val completed: Boolean,
    val isPersonalRecord: Boolean,
    val loggedAt: Long
)

@Dao
interface WorkoutDao {

    @Query("SELECT * FROM workout_sessions WHERE completed = 0 ORDER BY startedAt DESC LIMIT 1")
    suspend fun getInProgressSession(): WorkoutSession?

    @Query("SELECT * FROM workout_sessions WHERE completed = 0 ORDER BY startedAt DESC LIMIT 1")
    fun observeInProgressSession(): Flow<WorkoutSession?>

    @Query("SELECT * FROM workout_sessions ORDER BY startedAt DESC")
    fun observeAllSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE completed = 1 ORDER BY startedAt DESC")
    fun observeCompletedSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE startedAt BETWEEN :from AND :to ORDER BY startedAt ASC")
    suspend fun getSessionsBetween(from: Long, to: Long): List<WorkoutSession>

    @Query("SELECT * FROM workout_sessions WHERE completed = 1 AND startedAt BETWEEN :from AND :to ORDER BY startedAt ASC")
    suspend fun getCompletedSessionsBetween(from: Long, to: Long): List<WorkoutSession>

    @Query("SELECT EXISTS(SELECT 1 FROM workout_sessions WHERE completed = 1 AND startedAt BETWEEN :from AND :to LIMIT 1)")
    suspend fun hasCompletedSessionBetween(from: Long, to: Long): Boolean

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getSession(id: Long): WorkoutSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: WorkoutSession): Long

    @Query("UPDATE workout_sessions SET completed = 1, endedAt = :endedAt WHERE id = :id")
    suspend fun completeSession(id: Long, endedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteSession(session: WorkoutSession)

    @Transaction
    suspend fun deleteSessionAndSets(session: WorkoutSession) {
        clearSetsForSession(session.id)
        deleteSession(session)
    }

    @Query("SELECT * FROM set_entries WHERE workoutSessionId = :sessionId ORDER BY exerciseId, setIndex ASC")
    fun observeSetsForSession(sessionId: Long): Flow<List<SetEntry>>

    @Query("SELECT * FROM set_entries WHERE workoutSessionId = :sessionId ORDER BY exerciseId, setIndex ASC")
    suspend fun getSetsForSessionOnce(sessionId: Long): List<SetEntry>

    @Query("""
        SELECT se.* FROM set_entries se
        INNER JOIN workout_sessions ws ON ws.id = se.workoutSessionId
        WHERE se.exerciseId = :exerciseId AND ws.completed = 1
        ORDER BY se.loggedAt DESC LIMIT :limit
    """)
    suspend fun getRecentSetsForExercise(exerciseId: Long, limit: Int = 50): List<SetEntry>

    @Query("SELECT * FROM set_entries WHERE exerciseId = :exerciseId ORDER BY loggedAt DESC")
    fun observeAllSetsForExercise(exerciseId: Long): Flow<List<SetEntry>>

    @Query("""
        SELECT se.* FROM set_entries se
        INNER JOIN workout_sessions ws ON ws.id = se.workoutSessionId
        WHERE ws.completed = 1 AND se.completed = 1 AND se.isWarmup = 0
        ORDER BY se.loggedAt ASC
    """)
    fun observeAllWorkingSets(): Flow<List<SetEntry>>

    @Query("""
        SELECT se.id, se.workoutSessionId, se.exerciseId, se.setIndex, se.weight, se.reps,
               se.isWarmup, se.rpe, se.completed, se.isPersonalRecord, se.loggedAt
        FROM set_entries se
        INNER JOIN workout_sessions ws ON ws.id = se.workoutSessionId
        WHERE se.exerciseId = :exerciseId
          AND ws.completed = 1
          AND se.completed = 1
          AND se.isWarmup = 0
        ORDER BY se.loggedAt ASC
    """)
    suspend fun getCompletedSetsForExercise(exerciseId: Long): List<ExerciseProgressSet>

    /** Working-set count and volume for sessions whose start time is in the requested range. */
    @Query("""
        SELECT COUNT(se.id) AS setCount,
               COALESCE(SUM(se.weight * se.reps), 0.0) AS totalVolume
        FROM set_entries se
        INNER JOIN workout_sessions ws ON ws.id = se.workoutSessionId
        WHERE ws.completed = 1
          AND se.completed = 1
          AND se.isWarmup = 0
          AND ws.startedAt BETWEEN :from AND :to
    """)
    suspend fun getSetStatsBetween(from: Long, to: Long): SetStats

    @Query("""
        SELECT ws.id AS sessionId,
               ws.splitDayNameSnapshot AS splitDayNameSnapshot,
               ws.startedAt AS startedAt,
               ws.endedAt AS endedAt,
               COUNT(se.id) AS setCount,
               COUNT(CASE WHEN se.completed = 1 AND se.isWarmup = 0 THEN 1 END) AS workingSetCount,
               COALESCE(SUM(CASE WHEN se.completed = 1 AND se.isWarmup = 0 THEN se.weight * se.reps ELSE 0 END), 0.0) AS totalVolume
        FROM workout_sessions ws
        LEFT JOIN set_entries se ON se.workoutSessionId = ws.id
        WHERE ws.completed = 1
        GROUP BY ws.id
        ORDER BY ws.startedAt DESC
    """)
    fun observeCompletedHistoryStats(): Flow<List<HistorySessionStats>>

    @Query("""
        SELECT MAX(se.weight * (1.0 + se.reps / 30.0))
        FROM set_entries se
        INNER JOIN workout_sessions ws ON ws.id = se.workoutSessionId
        WHERE se.exerciseId = :exerciseId
          AND ws.completed = 1
          AND se.completed = 1
          AND se.isWarmup = 0
          AND se.reps > 0
          AND se.weight > 0
    """)
    suspend fun getBestEstimated1RMForExercise(exerciseId: Long): Double?

    @Query("""
        SELECT se.* FROM set_entries se
        INNER JOIN workout_sessions ws ON ws.id = se.workoutSessionId
        WHERE se.exerciseId = :exerciseId AND ws.completed = 1 AND se.completed = 1
        ORDER BY se.loggedAt DESC LIMIT 1
    """)
    suspend fun getLatestCompletedSetForExercise(exerciseId: Long): SetEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSet(set: SetEntry): Long

    @Query("SELECT COALESCE(MAX(setIndex), 0) FROM set_entries WHERE workoutSessionId = :sessionId AND exerciseId = :exerciseId")
    suspend fun getMaxSetIndex(sessionId: Long, exerciseId: Long): Int

    @Query("UPDATE set_entries SET setIndex = :setIndex WHERE id = :setId")
    suspend fun updateSetIndex(setId: Long, setIndex: Int)

    @Query("UPDATE set_entries SET weight = weight * :factor")
    suspend fun scaleAllWeights(factor: Double)

    @Update
    suspend fun updateSet(set: SetEntry)

    @Delete
    suspend fun deleteSet(set: SetEntry)

    @Query("DELETE FROM set_entries WHERE workoutSessionId = :sessionId")
    suspend fun clearSetsForSession(sessionId: Long)

    @Transaction
    suspend fun deleteSetAndReindex(set: SetEntry) {
        deleteSet(set)
        val remaining = getSetsForSessionOnce(set.workoutSessionId)
            .filter { it.exerciseId == set.exerciseId }
            .sortedBy { it.setIndex }
        remaining.forEachIndexed { index, entry ->
            val wantedIndex = index + 1
            if (entry.setIndex != wantedIndex) updateSetIndex(entry.id, wantedIndex)
        }
    }
}
