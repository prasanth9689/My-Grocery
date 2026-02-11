package com.skyblue.mygrocery.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.skyblue.mygrocery.model.UserLocation
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: UserLocation)

    @Query("SELECT * FROM user_locations ORDER BY timestamp DESC")
    fun getAllLocations(): Flow<List<UserLocation>>

    @Query("SELECT * FROM user_locations WHERE isCurrentLocation = 1 LIMIT 1")
    suspend fun getCurrentLocation(): UserLocation?

    @Query("UPDATE user_locations SET isCurrentLocation = 0")
    suspend fun clearCurrentLocation()

    @Delete
    suspend fun deleteLocation(location: UserLocation)
}