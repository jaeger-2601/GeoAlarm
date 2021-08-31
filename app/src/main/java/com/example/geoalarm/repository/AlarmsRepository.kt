package com.example.geoalarm.repository

import androidx.lifecycle.LiveData
import com.example.geoalarm.data.Alarm
import com.example.geoalarm.data.room.AlarmsDao
import com.google.android.libraries.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Singleton

class AlarmsRepository @Inject constructor(private val alarmsDao : AlarmsDao) {

    fun getAlarmsLive() : LiveData<List<Alarm>> {
        return alarmsDao.getAllAlarmsLive()
    }

    fun isAlarmActiveLive(id: Int) : LiveData<Boolean> {
        return alarmsDao.isAlarmActive(id)
    }

    suspend fun addAlarm(alarm: Alarm) {
        alarmsDao.insert(alarm)
    }

    suspend fun getAlarmByLocation(location: LatLng) : Alarm? {
        return alarmsDao.get(location)
    }

    suspend fun getAlarmById(id: Int) : Alarm? {
        return alarmsDao.get(id)
    }

    suspend fun getActiveAlarms() : List<Alarm> {
        return alarmsDao.getActiveAlarms()
    }

    suspend fun updateAlarm(alarm: Alarm) {
        alarmsDao.update(alarm)
    }

    suspend fun deleteAlarm(alarm: Alarm){
        alarmsDao.delete(alarm)
    }


}