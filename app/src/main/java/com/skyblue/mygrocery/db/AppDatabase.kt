package com.skyblue.mygrocery.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.skyblue.mygrocery.model.Product    // Crucial Import
import com.skyblue.mygrocery.model.CartItem   // Crucial Import
import com.skyblue.mygrocery.model.UserLocation // Crucial Import
import com.skyblue.mygrocery.utils.Converters

@Database(
    entities = [
        UserLocation::class,
        Product::class,
        CartItem::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun locationDao(): LocationDao
    abstract fun cartDao(): CartDao
}
