package com.example.geoalarm.data

import android.app.PendingIntent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.geoalarm.data.room.AlarmsDao
import com.google.android.gms.location.GeofencingClient

class AlarmsScreenViewModelFactory(
    private val dataSource: AlarmsDao,
    private val geofencingClient: GeofencingClient,
    private val geofencePendingIntent: PendingIntent
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlarmsScreenViewModel::class.java)) {
            return AlarmsScreenViewModel(dataSource, geofencingClient, geofencePendingIntent) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
