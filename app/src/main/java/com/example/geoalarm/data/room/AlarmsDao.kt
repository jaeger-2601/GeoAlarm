package com.example.geoalarm.data.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.geoalarm.data.Alarm

@Dao
abstract class AlarmsDao {

    @Query("SELECT * FROM alarms WHERE id = :id")
    abstract fun get(id: Int): Alarm?

    @Query("SELECT * FROM alarms")
    abstract fun getAllAlarms(): LiveData<List<Alarm>>

    @Insert
    abstract fun insert(alarm: Alarm)

    @Delete
    abstract fun delete(alarm: Alarm)
}