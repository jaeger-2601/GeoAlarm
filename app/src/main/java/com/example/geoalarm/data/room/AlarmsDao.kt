package com.example.geoalarm.data.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.geoalarm.data.Alarm
import com.google.android.libraries.maps.model.LatLng

@Dao
interface AlarmsDao {

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun get(id: Int): Alarm?

    @Query("SELECT * FROM alarms WHERE location = :location")
    suspend fun get(location: LatLng): Alarm?

    @Query("SELECT * FROM alarms")
    fun getAllAlarmsLive(): LiveData<List<Alarm>>

    @Query("SELECT * from alarms WHERE is_active = 1")
    suspend fun getActiveAlarms(): List<Alarm>

    @Query("SELECT is_active FROM alarms WHERE id = :id")
    fun isAlarmActive(id: Int): LiveData<Boolean>

    @Insert
    suspend fun insert(alarm: Alarm)

    @Update
    suspend fun update(alarm: Alarm)

    @Delete
    suspend fun delete(alarm: Alarm)

    @Query("DELETE FROM alarms")
    suspend fun deleteAll()
}