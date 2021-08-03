package com.example.geoalarm.data.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.geoalarm.data.Alarm

@Dao
interface AlarmsDao {

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun get(id: Int): Alarm?

    @Query("SELECT * FROM alarms")
    fun getAllAlarms(): LiveData<List<Alarm>>

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