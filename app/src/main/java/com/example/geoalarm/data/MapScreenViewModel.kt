package com.example.geoalarm.data

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.*
import com.example.geoalarm.ENTER_CIRCLE_OPTIONS
import com.example.geoalarm.ENTER_MARKER_OPTIONS
import com.example.geoalarm.EXIT_CIRCLE_OPTIONS
import com.example.geoalarm.EXIT_MARKER_OPTIONS
import com.example.geoalarm.data.room.AlarmsDao
import com.google.android.libraries.maps.GoogleMap
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

    private val _alarmName = MutableLiveData("")
    val alarmName: LiveData<String>
        get() = _alarmName

    private val _sliderPosition = MutableLiveData(0f)
    val sliderPosition: LiveData<Float>
        get() = _sliderPosition

    private val _alarmType = MutableLiveData(AlarmType.ON_ENTRY)
    val alarmType: LiveData<AlarmType>
        get() = _alarmType

    private val _isMapInitialized = MutableLiveData(false)
    val isMapInitialized: LiveData<Boolean>
        get() = _isMapInitialized

    val areaRadius = Transformations.map(sliderPosition) {
        sliderPosition.value?.times(1000)?.toInt()
    }


    val alarms = database.getAllAlarms()


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
                        radius = areaRadius.value ?: 1,
                        type = alarmType.value!!,
                        is_active = is_active,
                        created_at = Date()
                    )
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun MapInitializer(googleMap: GoogleMap, permissionsGranted: Boolean) {

        Log.i("MapInitializer", "Entered MapInitializer")

        googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
        googleMap.isMyLocationEnabled = permissionsGranted

        //make sure alarms is not null
        alarms.value?.let {
            Log.i("MapInitializer", "Map Initialized")

            _isMapInitialized.value = true

            // place marker on active alarms in the map
            for (alarm in it) {

                if (alarm.is_active) {
                    if (alarm.type == AlarmType.ON_ENTRY) {

                        googleMap.addMarker(
                            ENTER_MARKER_OPTIONS
                                .position(alarm.location)
                                .title(alarm.name)
                                .snippet(
                                    "Lat: %.4f Long: %.4f".format(
                                        alarm.location.latitude,
                                        alarm.location.longitude
                                    )
                                )
                        )

                        googleMap.addCircle(
                            ENTER_CIRCLE_OPTIONS
                                .center(alarm.location)
                                .radius(alarm.radius.toDouble())
                        )

                    } else {
                        googleMap.addMarker(
                            EXIT_MARKER_OPTIONS
                                .position(alarm.location)
                                .title(alarm.name)
                                .snippet(
                                    "Lat: %.4f Long: %.4f".format(
                                        alarm.location.latitude,
                                        alarm.location.longitude
                                    )
                                )
                        )

                        googleMap.addCircle(
                            EXIT_CIRCLE_OPTIONS
                                .center(alarm.location)
                                .radius(alarm.radius.toDouble())
                        )
                    }
                }

            }

            _isMapInitialized.value = true
        }

    }

    fun onMapDestroyed(){
        _isMapInitialized.value = false
    }


}