package com.redforge.app.data.repository

import com.redforge.app.data.local.dao.SplitDao
import com.redforge.app.data.local.entities.Split
import com.redforge.app.data.local.entities.SplitDay
import com.redforge.app.data.local.entities.SplitDayExercise
import kotlinx.coroutines.flow.Flow

class SplitRepository(private val dao: SplitDao) {

    fun observeAllSplits(): Flow<List<Split>> = dao.observeAllSplits()
    fun observeActiveSplit(): Flow<Split?> = dao.observeActiveSplit()
    fun observeSplit(id: Long): Flow<Split?> = dao.observeSplit(id)
    suspend fun getSplit(id: Long) = dao.getSplit(id)

    suspend fun saveSplit(split: Split): Long =
        dao.upsertSplit(split.copy(updatedAt = System.currentTimeMillis()))

    suspend fun deleteSplit(split: Split) = dao.deleteSplitWithChildren(split)

    suspend fun setActiveSplit(splitId: Long) = dao.activateSplit(splitId)

    fun observeDays(splitId: Long): Flow<List<SplitDay>> = dao.observeDaysForSplit(splitId)
    fun observeDay(id: Long): Flow<SplitDay?> = dao.observeDay(id)
    suspend fun getDay(id: Long) = dao.getDay(id)
    suspend fun saveDay(day: SplitDay): Long = dao.upsertDay(day)

    suspend fun deleteDay(day: SplitDay) = dao.deleteDayWithExercises(day)

    fun observeDayExercises(dayId: Long): Flow<List<SplitDayExercise>> =
        dao.observeExercisesForDay(dayId)

    suspend fun getDayExercisesOnce(dayId: Long) = dao.getExercisesForDayOnce(dayId)
    suspend fun saveDayExercise(entry: SplitDayExercise): Long = dao.upsertDayExercise(entry)
    suspend fun deleteDayExercise(entry: SplitDayExercise) = dao.deleteDayExercise(entry)

    suspend fun replaceDayExercises(dayId: Long, exercises: List<SplitDayExercise>) {
        val normalized = exercises.mapIndexed { index, entry ->
            entry.copy(splitDayId = dayId, orderIndex = index)
        }
        dao.replaceDayExercises(dayId, normalized)
    }
}
