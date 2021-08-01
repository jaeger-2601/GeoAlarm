package com.example.geoalarm.data

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.geoalarm.AlarmsScreenViewModel
import com.example.geoalarm.data.room.AlarmsDao

class MapScreenViewModelFactory(
    private val dataSource: AlarmsDao
    ) : ViewModelProvider.Factory {


    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapScreenViewModel::class.java)) {
            return MapScreenViewModel(dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}