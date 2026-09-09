package com.redforge.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Category tags used to line photos up consistently over time. */
enum class PhotoAngle { FRONT, SIDE, BACK, OTHER }

/**
 * A physique tracking photo. [filePath] points at a file under the app's
 * private files dir (see FileProvider "progress_photos" path) — never a
 * shared/public gallery location — to keep it local and sandboxed.
 */
@Entity(tableName = "progress_photos")
data class ProgressPhoto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filePath: String,
    val angle: PhotoAngle = PhotoAngle.FRONT,
    val takenAt: Long = System.currentTimeMillis(),
    val bodyWeightAtTime: Double? = null,
    val note: String = ""
)

/**
 * A body-metrics checkpoint for scientific tracking beyond the scale:
 * bodyweight plus optional circumference measurements and body fat %.
 * All fields besides date/weight are optional so users can log as much or
 * as little as they want.
 */
@Entity(tableName = "body_measurements")
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
