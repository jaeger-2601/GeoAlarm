package com.example.geoalarm.data

import android.app.PendingIntent
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.geoalarm.data.room.AlarmsDao
import com.example.geoalarm.utils.addGeofence
import com.example.geoalarm.utils.removeGeoFence
import com.google.android.gms.location.GeofencingClient
import kotlinx.coroutines.launch

class AlarmsScreenViewModel(
    val database: AlarmsDao,
    val geofencingClient: GeofencingClient,
    val geofencePendingIntent: PendingIntent
) : ViewModel() {

    private val TAG = "AlarmsScreenViewModel"

    val alarms = database.getAllAlarmsLive()

    private val _selectedAlarm: MutableLiveData<Alarm?> = MutableLiveData(null)
    val selectedAlarm: LiveData<Alarm?>
        get() = _selectedAlarm

    private val _sliderPosition: MutableLiveData<Float?> = MutableLiveData(null)
    val sliderPosition: LiveData<Float?>
        get() = _sliderPosition

    private val _alarmName: MutableLiveData<String?> = MutableLiveData(null)
    val alarmName: LiveData<String?>
        get() = _alarmName

    private val _alarmType: MutableLiveData<AlarmType?> = MutableLiveData(null)
    val alarmType: LiveData<AlarmType?>
        get() = _alarmType

    val areaRadius = Transformations.map(sliderPosition) {
        sliderPosition.value?.times(1000)?.toInt()
    }

    fun changeSelectedAlarm(alarm: Alarm?) {
        _selectedAlarm.value = alarm
        _sliderPosition.value = alarm?.radius?.toFloat()?.div(1000)
        _alarmName.value = alarm?.name
        _alarmType.value = alarm?.type
    }

    fun onChangeSlider(num: Float) {
        _sliderPosition.value = num
    }

    fun onAlarmNameChange(name: String) {
        _alarmName.value = name
    }

    fun onChangeAlarmType(type: AlarmType) {
        _alarmType.value = type
    }

    fun toggleAlarm(alarm: Alarm, context: Context) {

        if (alarm.is_active) {
            //Remove geofence
            removeGeoFence(
                geofencingClient,
                alarm,
                context,
                success = { Log.i(TAG, "Geofence toggled off successfully") },
                failure = { error_str, _ -> Log.i(TAG, error_str) }
            )

        } else {
            //Add geofence
            addGeofence(
                geofencingClient,
                geofencePendingIntent,
                alarm,
                context,
                success = { Log.i(TAG, "Geofence toggled on successfully") },
                failure = { error_str, _ -> Log.i(TAG, error_str) }
            )
        }

        alarm.is_active = !alarm.is_active

        viewModelScope.launch {
            database.update(alarm)
        }

    }

    fun menuClose() {
        changeSelectedAlarm(null)
    }

    fun modifyAlarm() {
        selectedAlarm.value?.let {
            it.type = alarmType.value ?: it.type
            it.name = alarmName.value ?: it.name
            it.radius = areaRadius.value ?: it.radius

            viewModelScope.launch {
                database.update(it)
            }
        }

        menuClose()
    }

    fun deleteAlarm() {

        selectedAlarm.value?.let {
            viewModelScope.launch {
                database.delete(it)
            }
        }

        menuClose()
    }


}