package com.redforge.app.data.local.db

import androidx.room.TypeConverter
import com.redforge.app.data.local.entities.PhotoAngle

class Converters {
    @TypeConverter
    fun fromPhotoAngle(angle: PhotoAngle): String = angle.name

    @TypeConverter
    fun toPhotoAngle(value: String): PhotoAngle = PhotoAngle.valueOf(value)
}
