package com.example.geoalarm.data

import android.app.PendingIntent
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geoalarm.GeofenceErrorMessages
import com.example.geoalarm.addGeofence
import com.example.geoalarm.data.room.AlarmsDao
import com.example.geoalarm.removeGeoFence
import com.google.android.gms.location.GeofencingClient
import kotlinx.coroutines.launch

class AlarmsScreenViewModel(
    val database: AlarmsDao,
    val geofencingClient: GeofencingClient,
    val geofencePendingIntent: PendingIntent
) : ViewModel() {

    private val TAG = "AlarmsScreenViewModel"

    val alarms = database.getAllAlarmsLive()

    fun toggleAlarm(alarm: Alarm, context: Context) {

        if (alarm.is_active){
            //Remove geofence
            removeGeoFence(
                geofencingClient,
                alarm,
                context,
                success = { Log.i(TAG, "Geofence toggled off successfully") },
                failure = { error_str, _ -> Log.i(TAG, error_str) }
            )

        }
        else {
            //Add geofence
            addGeofence(
                geofencingClient,
                geofencePendingIntent,
                alarm,
                context,
                success = { Log.i(TAG, "Geofence toggled on successfully") },
                failure = { error_str, _-> Log.i(TAG, error_str) }
            )
        }

        alarm.is_active = !alarm.is_active

        viewModelScope.launch {
            database.update(alarm)
        }

    }


}