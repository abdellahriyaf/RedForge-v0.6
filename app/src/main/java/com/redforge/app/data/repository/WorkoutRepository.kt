package com.redforge.app.data.repository

import com.redforge.app.data.local.dao.HistorySessionStats
import com.redforge.app.data.local.dao.WorkoutDao
import com.redforge.app.data.local.entities.SetEntry
import com.redforge.app.data.local.entities.WorkoutSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class WorkoutRepository(private val dao: WorkoutDao) {

    /*
     * Completed-history lookups are read frequently while a workout is in
     * progress. Cache the bounded history window per exercise so logSet() does
     * not hit Room for the same 1,000-row history scan on every tap.
     *
     * The cache represents completed-session history only; the current active
     * session remains separate and is persisted by logSet(). Any operation
     * that can change completed history invalidates the cache.
     */
    private val recentSetsCacheMutex = Mutex()
    private val recentSetsCache = mutableMapOf<Long, List<SetEntry>>()

    fun observeInProgressSession(): Flow<WorkoutSession?> = dao.observeInProgressSession()
    suspend fun getInProgressSession(): WorkoutSession? = dao.getInProgressSession()

    fun observeAllSessions(): Flow<List<WorkoutSession>> = dao.observeAllSessions()
    suspend fun getSessionsBetween(from: Long, to: Long) = dao.getSessionsBetween(from, to)
    suspend fun getSession(id: Long) = dao.getSession(id)

    /** Creates and immediately persists a new session. */
    suspend fun startSession(splitDayId: Long?, splitDayName: String): Long =
        dao.upsertSession(WorkoutSession(splitDayId = splitDayId, splitDayNameSnapshot = splitDayName))

    suspend fun completeSession(id: Long) {
        dao.completeSession(id)
        clearRecentSetsCache()
    }

    suspend fun deleteSession(session: WorkoutSession) {
        dao.deleteSessionAndSets(session)
        clearRecentSetsCache()
    }

    fun observeSets(sessionId: Long): Flow<List<SetEntry>> = dao.observeSetsForSession(sessionId)
    suspend fun getSetsOnce(sessionId: Long) = dao.getSetsForSessionOnce(sessionId)

    /** One SQL projection drives the history list without per-session queries. */
    fun observeCompletedHistoryStats(): Flow<List<HistorySessionStats>> =
        dao.observeCompletedHistoryStats()

    /** Writes one set immediately; callers should never batch set logging in memory. */
    suspend fun logSet(set: SetEntry): Long = dao.upsertSet(set)

    suspend fun updateSet(set: SetEntry) {
        dao.updateSet(set)
        clearRecentSetsCache()
    }

    suspend fun deleteSet(set: SetEntry) {
        dao.deleteSet(set)
        clearRecentSetsCache()
    }

    suspend fun getMaxSetIndex(sessionId: Long, exerciseId: Long): Int =
        dao.getMaxSetIndex(sessionId, exerciseId)

    suspend fun convertAllSetWeights(factor: Double) {
        dao.scaleAllWeights(factor)
        clearRecentSetsCache()
    }

    suspend fun deleteSetAndReindex(set: SetEntry) {
        dao.deleteSetAndReindex(set)
        clearRecentSetsCache()
    }

    /**
     * Returns completed-history sets only. The first request loads the bounded
     * history window from Room; later requests for the same exercise are served
     * from memory until completed-history mutation invalidates the cache.
     */
    suspend fun getRecentSetsForExercise(exerciseId: Long, limit: Int = 50): List<SetEntry> =
        recentSetsCacheMutex.withLock {
            val cached = recentSetsCache[exerciseId]
            if (cached != null) {
                return@withLock cached.take(limit.coerceAtLeast(0))
            }

            val loaded = dao.getRecentSetsForExercise(exerciseId, 1000)
            recentSetsCache[exerciseId] = loaded
            loaded.take(limit.coerceAtLeast(0))
        }

    /** Targeted aggregate used by PR/history code that only needs the best e1RM. */
    suspend fun getBestEstimated1RMForExercise(exerciseId: Long): Double? =
        dao.getBestEstimated1RMForExercise(exerciseId)

    fun observeAllSetsForExercise(exerciseId: Long): Flow<List<SetEntry>> =
        dao.observeAllSetsForExercise(exerciseId)

    private suspend fun clearRecentSetsCache() {
        recentSetsCacheMutex.withLock {
            recentSetsCache.clear()
        }
    }
}
