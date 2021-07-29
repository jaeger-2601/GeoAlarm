package com.example.geoalarm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.geoalarm.data.room.AlarmsDao

class AlarmsScreenViewModel(
    val database: AlarmsDao,
    application: Application
) : AndroidViewModel(application) {

}