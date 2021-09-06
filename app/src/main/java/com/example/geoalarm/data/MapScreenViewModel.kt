package com.example.geoalarm.data

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.geoalarm.navigation.Directions
import com.example.geoalarm.navigation.NavigationManager
import com.example.geoalarm.repository.AlarmsRepository
import com.example.geoalarm.utils.*
import com.google.android.gms.location.GeofencingClient
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.model.Circle
import com.google.android.libraries.maps.model.CircleOptions
import com.google.android.libraries.maps.model.Marker
import com.google.android.libraries.maps.model.MarkerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MapScreenViewModel @Inject constructor(
    private val repository: AlarmsRepository,
    private val navigationManager: NavigationManager,
    private val geofencingClient: GeofencingClient,
    private val geofencePendingIntent: PendingIntent
) : ViewModel() {

    private val _lastMarker = MutableLiveData<Marker?>(null)
    val lastMarker: LiveData<Marker?>
        get() = _lastMarker

    private val _lastCircle = MutableLiveData<Circle?>(null)
    private val lastCircle: LiveData<Circle?>
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

    val alarms = repository.getAlarmsLive()

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

    fun addAlarm(is_active: Boolean, context: Context){

        viewModelScope.launch {

            lastMarker.value?.let {


                val alarm = Alarm(
                    name = alarmName.value!!,
                    location = it.position,
                    radius = areaRadius.value ?: 1,
                    type = alarmType.value!!,
                    is_active = is_active,
                    created_at = Date()
                )

                repository.addAlarm(alarm)

                if (is_active) {

                    // Alarm is again retrieved from database using location since
                    // alarm id is auto generated upon insertion and the geofencing request id
                    // is the same as the id of the alarm.

                    repository.getAlarmByLocation(alarm.location)?.let { alarm1 ->
                        addGeofence(
                            geofencingClient,
                            geofencePendingIntent,
                            alarm1,
                            context,
                            success = { Log.i("addAlarm", "Geofence successfully added") },
                            failure = { error, _ -> Log.e("addAlarm", error) }
                            )
                    }
                }


            }

        }
    }


    @SuppressLint("LongLogTag", "MissingPermission")
    fun mapUpdate(googleMap: GoogleMap) {

        // Update google maps with the creation and deletion of markers

        Log.i("mapUpdate", "Updating Map")

        viewModelScope.launch {

            var isPresent: Boolean
            var index: Int
            val changedMarkers = mutableListOf<Marker>()
            var markerOptions: MarkerOptions
            var circleOptions: CircleOptions


            alarms.value?.filter { it.is_active }?.let {
                if (!isMapInitialized) {

                    googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                    googleMap.isMyLocationEnabled = true

                    isMapInitialized = true

                    Log.i("mapUpdate", "Initializing map")
                }

                if (alarms.value == null)
                    return@launch


                // An alarm has been deactivated or deleted
                if (googleMapMarkers.size > it.size) {

                    for (marker in googleMapMarkers) {

                        isPresent = false

                        for (alarm in it) {
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
                else if (googleMapMarkers.size < it.size) {

                    for (alarm in it) {
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

    }

    fun goToAlarmsScreen() {
        navigationManager.navigate(Directions.Alarms)
    }

    fun onMapDestroyed() {

        isMapInitialized = false

        for (marker in googleMapMarkers) {
            marker.remove()
        }

        for (circle in googleMapCircles) {
            circle.remove()
        }

        googleMapMarkers.clear()
        googleMapCircles.clear()
    }

}