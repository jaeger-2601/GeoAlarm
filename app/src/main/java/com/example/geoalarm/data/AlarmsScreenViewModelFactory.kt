package com.example.geoalarm.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.geoalarm.data.room.AlarmsDao

class AlarmsScreenViewModelFactory(
    private val dataSource: AlarmsDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlarmsScreenViewModel::class.java)) {
            return AlarmsScreenViewModel(dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
