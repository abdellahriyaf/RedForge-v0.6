package com.redforge.app.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A training split (e.g. "Push/Pull/Legs", "Upper/Lower", "Bro Split").
 * Exactly one split should have [isActive] = true at a time — that's the
 * one shown on the Home screen and used for streak calculation.
 */
@Entity(
    tableName = "splits",
    indices = [
        Index(value = ["isActive"]),
        Index(value = ["updatedAt"])
    ]
)
data class Split(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isActive: Boolean = false,
    val daysPerCycle: Int = 0,
    val isDeloadCycle: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "split_days",
    indices = [Index(value = ["splitId", "dayOrder"])]
)
data class SplitDay(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val splitId: Long,
    val name: String,
    val dayOrder: Int,
    val isRestDay: Boolean = false
)

@Entity(
    tableName = "split_day_exercises",
    indices = [Index(value = ["splitDayId", "orderIndex"])]
)
data class SplitDayExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val splitDayId: Long,
    val exerciseId: Long,
    val orderIndex: Int,
    val targetSets: Int = 3,
    val targetRepsLow: Int = 8,
    val targetRepsHigh: Int = 12,
    val targetRestSeconds: Int = 0,
    val supersetGroup: Int? = null
)
