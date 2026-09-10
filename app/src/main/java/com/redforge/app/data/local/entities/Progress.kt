package com.redforge.app.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class PhotoAngle { FRONT, SIDE, BACK, OTHER }

@Entity(
    tableName = "progress_photos",
    indices = [Index(value = ["takenAt"])]
)
data class ProgressPhoto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filePath: String,
    val angle: PhotoAngle = PhotoAngle.FRONT,
    val takenAt: Long = System.currentTimeMillis(),
    val bodyWeightAtTime: Double? = null,
    val note: String = ""
)

@Entity(
    tableName = "body_measurements",
    indices = [Index(value = ["date"])]
)
data class BodyMeasurement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val bodyWeight: Double,
    val bodyFatPercent: Float? = null,
    val chestCm: Float? = null,
    val waistCm: Float? = null,
    val hipsCm: Float? = null,
    val armCm: Float? = null,
    val thighCm: Float? = null
)
