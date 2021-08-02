package com.example.geoalarm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.geoalarm.data.room.AlarmsDao

class AlarmsScreenViewModel(
    val database: AlarmsDao,
) : ViewModel() {

    val alarms = database.getAllAlarms()

}