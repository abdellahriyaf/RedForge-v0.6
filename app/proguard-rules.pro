# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**

# Keep entities for reflection-based Room mapping
-keep class com.redforge.app.data.local.entities.** { *; }
