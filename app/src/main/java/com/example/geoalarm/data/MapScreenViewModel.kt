package com.example.geoalarm.data

import android.app.Application
import androidx.lifecycle.*
import com.example.geoalarm.data.room.AlarmsDao
import com.google.android.libraries.maps.model.Circle
import com.google.android.libraries.maps.model.Marker


class MapScreenViewModel(
    val database: AlarmsDao
) : ViewModel() {

    private val _lastMarker = MutableLiveData<Marker?>(null)
    val lastMarker : LiveData<Marker?>
        get() = _lastMarker

    private val _lastCircle = MutableLiveData<Circle?>(null)
    val lastCircle : LiveData<Circle?>
        get() = _lastCircle

    private val _alarmName = MutableLiveData<String>(null)
    val alarmName : LiveData<String>
        get() = _alarmName

    private val _sliderPosition = MutableLiveData(0f)
    val sliderPosition : LiveData<Float>
        get() = _sliderPosition

    val areaRadius = Transformations.map(sliderPosition){
        sliderPosition.value?.times(1000)?.toInt()
    }

    val alarms = database.getAllAlarms()

    fun onChangeMarker(marker: Marker?, circle: Circle?){
        lastMarker.value?.remove()
        lastCircle.value?.remove()
        _lastMarker.value = marker
        _lastCircle.value = circle
    }

    fun onChangeSlider(num: Float){
        _sliderPosition.value = num
    }

    fun onAlarmNameChange(name: String){
        _alarmName.value = name
    }

}