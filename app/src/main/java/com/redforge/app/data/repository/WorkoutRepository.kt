package com.redforge.app.data.repository

import com.redforge.app.data.local.dao.ExerciseProgressSet
import com.redforge.app.data.local.dao.HistorySessionStats
import com.redforge.app.data.local.dao.SetStats
import com.redforge.app.data.local.dao.WorkoutDao
import com.redforge.app.data.local.entities.SetEntry
import com.redforge.app.data.local.entities.WorkoutSession
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(private val dao: WorkoutDao) {

    fun observeInProgressSession(): Flow<WorkoutSession?> = dao.observeInProgressSession()
    suspend fun getInProgressSession(): WorkoutSession? = dao.getInProgressSession()

    fun observeAllSessions(): Flow<List<WorkoutSession>> = dao.observeAllSessions()
    fun observeCompletedSessions(): Flow<List<WorkoutSession>> = dao.observeCompletedSessions()
    fun observeCompletedHistoryStats(): Flow<List<HistorySessionStats>> = dao.observeCompletedHistoryStats()
    fun observeAllWorkingSets(): Flow<List<SetEntry>> = dao.observeAllWorkingSets()

    suspend fun getSessionsBetween(from: Long, to: Long) = dao.getSessionsBetween(from, to)
    suspend fun getCompletedSessionsBetween(from: Long, to: Long) = dao.getCompletedSessionsBetween(from, to)
    suspend fun hasCompletedSessionBetween(from: Long, to: Long) = dao.hasCompletedSessionBetween(from, to)
    suspend fun getSetStatsBetween(from: Long, to: Long): SetStats = dao.getSetStatsBetween(from, to)
    suspend fun getSession(id: Long) = dao.getSession(id)

    suspend fun startSession(splitDayId: Long?, splitDayName: String): Long =
        dao.upsertSession(WorkoutSession(splitDayId = splitDayId, splitDayNameSnapshot = splitDayName))

    suspend fun completeSession(id: Long) = dao.completeSession(id)
    suspend fun deleteSession(session: WorkoutSession) = dao.deleteSessionAndSets(session)

    fun observeSets(sessionId: Long): Flow<List<SetEntry>> = dao.observeSetsForSession(sessionId)
    suspend fun getSetsOnce(sessionId: Long) = dao.getSetsForSessionOnce(sessionId)

    suspend fun logSet(set: SetEntry): Long = dao.upsertSet(set)
    suspend fun updateSet(set: SetEntry) = dao.updateSet(set)
    suspend fun deleteSet(set: SetEntry) = dao.deleteSet(set)

    suspend fun getMaxSetIndex(sessionId: Long, exerciseId: Long): Int =
        dao.getMaxSetIndex(sessionId, exerciseId)

    suspend fun convertAllSetWeights(factor: Double) = dao.scaleAllWeights(factor)
    suspend fun deleteSetAndReindex(set: SetEntry) = dao.deleteSetAndReindex(set)

    suspend fun getRecentSetsForExercise(exerciseId: Long, limit: Int = 50) =
        dao.getRecentSetsForExercise(exerciseId, limit)

    fun observeAllSetsForExercise(exerciseId: Long): Flow<List<SetEntry>> =
        dao.observeAllSetsForExercise(exerciseId)

    suspend fun getCompletedSetsForExercise(exerciseId: Long): List<ExerciseProgressSet> =
        dao.getCompletedSetsForExercise(exerciseId)

    suspend fun getBestEstimated1RMForExercise(exerciseId: Long): Double? =
        dao.getBestEstimated1RMForExercise(exerciseId)

    suspend fun getLatestCompletedSetForExercise(exerciseId: Long): SetEntry? =
        dao.getLatestCompletedSetForExercise(exerciseId)
}
