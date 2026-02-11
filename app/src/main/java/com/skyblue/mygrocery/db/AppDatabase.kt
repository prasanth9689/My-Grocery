package com.skyblue.mygrocery.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.skyblue.mygrocery.model.UserLocation
import com.skyblue.mygrocery.utils.Converters

@Database(
    entities = [
        UserLocation::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun locationDao(): LocationDao
}
