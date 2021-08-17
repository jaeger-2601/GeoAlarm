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

    private var googleMapMarkers = mutableListOf<Marker>()
    private var googleMapCircles = mutableListOf<Circle>()


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

    @SuppressLint("MissingPermission", "LongLogTag")
    fun MapInitializer(googleMap: GoogleMap, permissionsGranted: Boolean) {

        var is_present: Boolean
        var markerOptions: MarkerOptions
        var circleOptions: CircleOptions

        Log.i("getMapLifecycleObserver-MapInitializer", "Entered MapInitializer")

        googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
        googleMap.isMyLocationEnabled = permissionsGranted


        //make sure alarms is not null
        alarms.value?.let {
            Log.i("getMapLifecycleObserver-MapInitializer", "Map Initialized")
            this.MapUpdate(googleMap)
            _isMapInitialized.value = true
        }

    }

    @SuppressLint("LongLogTag")
    fun MapUpdate(googleMap: GoogleMap){

        // Update google maps with the creation and deletion of markers

        var is_present: Boolean
        var index: Int
        var changed_markers = mutableListOf<Marker>()
        var markerOptions: MarkerOptions
        var circleOptions: CircleOptions
        val activeAlarms = alarms.value!!.filter { it.is_active }

        Log.i("getMapLifecycleObserver-before", activeAlarms.size.toString())
        Log.i("getMapLifecycleObserver-before", googleMapMarkers.size.toString())

        // An alarm has been deactivated or deleted
        if (googleMapMarkers.size > activeAlarms.size){

            for (marker in googleMapMarkers) {

                is_present = false

                for(alarm in activeAlarms){
                    if (marker.position == alarm.location)
                        is_present = true
                }

                if (!is_present)
                    changed_markers.add(marker)
            }

            for (marker in changed_markers){
                index = googleMapMarkers.lastIndexOf(marker)
                googleMapMarkers.removeAt(index)
                googleMapCircles.removeAt(index)
            }

        }
        // An alarm has been activated or created
        else if (googleMapMarkers.size < alarms.value!!.size){

            for (alarm in activeAlarms){
                is_present = false

                for(marker in googleMapMarkers){
                    if (marker.position == alarm.location)
                            is_present = true
                }

                if (!is_present){


                    if (alarm.type == AlarmType.ON_ENTRY){
                        markerOptions = ENTER_MARKER_OPTIONS
                        circleOptions = ENTER_CIRCLE_OPTIONS
                    }
                    else{
                        markerOptions = EXIT_MARKER_OPTIONS
                        circleOptions = EXIT_CIRCLE_OPTIONS
                    }

                    googleMapMarkers.add(
                        googleMap.addMarker(
                            markerOptions
                                .position(alarm.location)
                                .title(alarm.name)
                                .snippet(
                                    "Lat: %.4f Long: %.4f".format(
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

        Log.i("getMapLifecycleObserver-after", activeAlarms.size.toString())
        Log.i("getMapLifecycleObserver-after", googleMapMarkers.size.toString())

    }

    fun onMapDestroyed(){
        _isMapInitialized.value = false
        googleMapMarkers.clear()
        googleMapCircles.clear()
    }

}