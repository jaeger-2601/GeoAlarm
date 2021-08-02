package com.example.geoalarm.data

import androidx.lifecycle.*
import com.example.geoalarm.data.room.AlarmsDao
import com.google.android.libraries.maps.model.Circle
import com.google.android.libraries.maps.model.Marker
import kotlinx.coroutines.launch
import java.util.*


class MapScreenViewModel(
    val database: AlarmsDao
) : ViewModel() {

    private val _lastMarker = MutableLiveData<Marker?>(null)
    val lastMarker: LiveData<Marker?>
        get() = _lastMarker

    private val _lastCircle = MutableLiveData<Circle?>(null)
    val lastCircle: LiveData<Circle?>
        get() = _lastCircle

    private val _alarmName = MutableLiveData<String>("")
    val alarmName: LiveData<String>
        get() = _alarmName

    private val _sliderPosition = MutableLiveData(0f)
    val sliderPosition: LiveData<Float>
        get() = _sliderPosition

    private val _alarmType = MutableLiveData(AlarmType.ON_ENTRY)
    val alarmType: LiveData<AlarmType>
        get() = _alarmType

    val areaRadius = Transformations.map(sliderPosition) {
        sliderPosition.value?.times(1000)?.toInt()
    }


    val alarms = database.getAllAlarms()

    init {

    }

    fun onMoveMarker(marker: Marker?, circle: Circle?) {
        lastMarker.value?.remove()
        lastCircle.value?.remove()
        _lastMarker.value = marker
        _lastCircle.value = circle
    }

    fun onChangeSlider(num: Float) {
        _sliderPosition.value = num
        lastCircle.value?.radius = areaRadius.value?.toDouble() ?: 0.0
    }

    fun onAlarmNameChange(name: String) {
        _alarmName.value = name
    }

    fun onChangeAlarmType(type: AlarmType) {
        _alarmType.value = type
    }

    fun addAlarm(
        is_active: Boolean,
    ) {

        lastMarker.value?.let{
            viewModelScope.launch {
                database.insert(
                    Alarm(
                        name = alarmName.value!!,
                        location = it.position,
                        radius = areaRadius?.value ?: 1,
                        type = alarmType.value!!,
                        is_active = is_active,
                        created_at = Date()
                    )
                )
            }
        }
    }
}