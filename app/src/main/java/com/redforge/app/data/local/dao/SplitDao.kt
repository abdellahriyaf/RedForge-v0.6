package com.redforge.app.data.local.dao

import androidx.room.*
import com.redforge.app.data.local.entities.Split
import com.redforge.app.data.local.entities.SplitDay
import com.redforge.app.data.local.entities.SplitDayExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface SplitDao {

    @Query("SELECT * FROM splits ORDER BY updatedAt DESC")
    fun observeAllSplits(): Flow<List<Split>>

    @Query("SELECT * FROM splits WHERE isActive = 1 LIMIT 1")
    fun observeActiveSplit(): Flow<Split?>

    @Query("SELECT * FROM splits WHERE id = :id")
    fun observeSplit(id: Long): Flow<Split?>

    @Query("SELECT * FROM splits WHERE id = :id")
    suspend fun getSplit(id: Long): Split?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSplit(split: Split): Long

    @Delete
    suspend fun deleteSplit(split: Split)

    @Query("UPDATE splits SET isActive = 0")
    suspend fun clearActiveFlag()

    @Query("UPDATE splits SET isActive = 1, updatedAt = :now WHERE id = :splitId")
    suspend fun markActive(splitId: Long, now: Long = System.currentTimeMillis())

    @Transaction
    suspend fun activateSplit(splitId: Long, now: Long = System.currentTimeMillis()) {
        clearActiveFlag()
        markActive(splitId, now)
    }

    @Query("SELECT * FROM split_days WHERE splitId = :splitId ORDER BY dayOrder ASC")
    fun observeDaysForSplit(splitId: Long): Flow<List<SplitDay>>

    @Query("SELECT * FROM split_days WHERE splitId = :splitId ORDER BY dayOrder ASC")
    suspend fun getDaysOnce(splitId: Long): List<SplitDay>

    @Query("SELECT * FROM split_days WHERE id = :id")
    fun observeDay(id: Long): Flow<SplitDay?>

    @Query("SELECT * FROM split_days WHERE id = :id")
    suspend fun getDay(id: Long): SplitDay?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDay(day: SplitDay): Long

    @Delete
    suspend fun deleteDay(day: SplitDay)

    @Query("SELECT * FROM split_day_exercises WHERE splitDayId = :dayId ORDER BY orderIndex ASC")
    fun observeExercisesForDay(dayId: Long): Flow<List<SplitDayExercise>>

    @Query("SELECT * FROM split_day_exercises WHERE splitDayId = :dayId ORDER BY orderIndex ASC")
    suspend fun getExercisesForDayOnce(dayId: Long): List<SplitDayExercise>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDayExercise(entry: SplitDayExercise): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDayExercises(entries: List<SplitDayExercise>): List<Long>

    @Delete
    suspend fun deleteDayExercise(entry: SplitDayExercise)

    @Query("DELETE FROM split_day_exercises WHERE splitDayId = :dayId")
    suspend fun clearExercisesForDay(dayId: Long)

    @Query("DELETE FROM split_day_exercises WHERE splitDayId IN (SELECT id FROM split_days WHERE splitId = :splitId)")
    suspend fun clearExercisesForSplit(splitId: Long)

    @Query("DELETE FROM split_days WHERE splitId = :splitId")
    suspend fun clearDaysForSplit(splitId: Long)

    @Transaction
    suspend fun replaceDayExercises(dayId: Long, entries: List<SplitDayExercise>) {
        clearExercisesForDay(dayId)
        if (entries.isNotEmpty()) upsertDayExercises(entries)
    }

    @Transaction
    suspend fun deleteDayWithExercises(day: SplitDay) {
        clearExercisesForDay(day.id)
        deleteDay(day)
    }

    @Transaction
    suspend fun deleteSplitWithChildren(split: Split) {
        clearExercisesForSplit(split.id)
        clearDaysForSplit(split.id)
        deleteSplit(split)
    }

    @Query("SELECT COUNT(*) FROM split_days WHERE splitId = :splitId")
    suspend fun countDays(splitId: Long): Int
}
