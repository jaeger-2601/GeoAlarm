package com.example.geoalarm.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geoalarm.data.room.AlarmsDao
import kotlinx.coroutines.launch

class AlarmsScreenViewModel(
    val database: AlarmsDao,
) : ViewModel() {

    val alarms = database.getAllAlarms()

    fun toggleAlarm(alarm: Alarm) {

        val updated_alarm = alarm

        updated_alarm.is_active = !alarm.is_active

        viewModelScope.launch {
            database.update(updated_alarm)
        }

    }


}