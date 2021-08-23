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
import com.google.android.libraries.maps.model.CircleOptions
import com.google.android.libraries.maps.model.Marker
import com.google.android.libraries.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*


class MapScreenViewModel(
    private val database: AlarmsDao
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


    val areaRadius = Transformations.map(sliderPosition) {
        sliderPosition.value?.times(1000)?.toInt()
    }

    val alarms = database.getAllAlarmsLive()

    private var googleMapMarkers = mutableListOf<Marker>()
    private var googleMapCircles = mutableListOf<Circle>()

    private var isMapInitialized = false


    fun onMoveMarker(marker: Marker?, circle: Circle?) {
        lastMarker.value?.remove()
        lastCircle.value?.remove()
        _lastMarker.value = marker
        _lastCircle.value = circle
    }

    fun onChangeSlider(num: Float) {
        _sliderPosition.value = num
        (areaRadius.value?.toDouble() ?: 0.0).also { lastCircle.value?.radius = it }
    }

    fun onAlarmNameChange(name: String) {
        _alarmName.value = name
    }

    fun onChangeAlarmType(type: AlarmType) {
        _alarmType.value = type
    }

    fun addAlarm(
        is_active: Boolean,
    ): Alarm? {

        lastMarker.value?.let {

            val alarm = Alarm(
                name = alarmName.value!!,
                location = it.position,
                radius = areaRadius.value ?: 1,
                type = alarmType.value!!,
                is_active = is_active,
                created_at = Date()
            )
            runBlocking {
                database.insert(alarm)
            }

            return runBlocking { database.get(alarm.location) }
        }

        return null
    }


    @SuppressLint("LongLogTag", "MissingPermission")
    fun mapUpdate(googleMap: GoogleMap) {

        // Update google maps with the creation and deletion of markers

        viewModelScope.launch {

            var isPresent: Boolean
            var index: Int
            val changedMarkers = mutableListOf<Marker>()
            var markerOptions: MarkerOptions
            var circleOptions: CircleOptions
            // LiveData alarms does not update value properly, use the suspend function inside a coroutine to get the list instead.
            val activeAlarms = database.getActiveAlarms()


            if (!isMapInitialized) {

                googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                googleMap.isMyLocationEnabled = true

                googleMap.clear()

                isMapInitialized = true
            }

            if (alarms.value == null)
                return@launch


            Log.i("mapUpdate", "Updating Map")
            Log.i(
                "mapUpdate",
                "activeAlarms:${activeAlarms.size} googleMapMarkers:${googleMapMarkers.size} LiveData:${alarms.value?.filter { it.is_active }?.size}"
            )

            // An alarm has been deactivated or deleted
            if (googleMapMarkers.size > activeAlarms.size) {

                for (marker in googleMapMarkers) {

                    isPresent = false

                    for (alarm in activeAlarms) {
                        if (marker.position == alarm.location)
                            isPresent = true
                    }

                    if (!isPresent)
                        changedMarkers.add(marker)
                }

                for (marker in changedMarkers) {
                    index = googleMapMarkers.lastIndexOf(marker)
                    googleMapMarkers.removeAt(index)
                    googleMapCircles.removeAt(index)
                }

            }
            // An alarm has been activated or created
            else if (googleMapMarkers.size < activeAlarms.size) {

                for (alarm in activeAlarms) {
                    isPresent = false

                    for (marker in googleMapMarkers) {
                        if (marker.position == alarm.location)
                            isPresent = true
                    }

                    if (!isPresent) {


                        if (alarm.type == AlarmType.ON_ENTRY) {
                            markerOptions = ENTER_MARKER_OPTIONS
                            circleOptions = ENTER_CIRCLE_OPTIONS
                        } else {
                            markerOptions = EXIT_MARKER_OPTIONS
                            circleOptions = EXIT_CIRCLE_OPTIONS
                        }

                        googleMapMarkers.add(
                            googleMap.addMarker(
                                markerOptions
                                    .position(alarm.location)
                                    .title(alarm.name)
                                    .snippet(
                                        ("Lat: %.4f Long: %.4f").format(
                                            alarm.location.latitude,
                                            alarm.location.longitude
                                        )
                                    )
                            )
                        )

                        googleMapCircles.add(
                            googleMap.addCircle(
                                circleOptions
                                    .center(alarm.location)
                                    .radius(alarm.radius.toDouble())
                            )
                        )
                    }

                }
            }
        }

    }

    fun onMapDestroyed() {
        isMapInitialized = false
        googleMapMarkers.clear()
        googleMapCircles.clear()
    }

}