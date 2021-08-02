package com.example.geoalarm

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import com.example.geoalarm.data.Alarm
import com.example.geoalarm.data.AlarmType
import com.example.geoalarm.data.MapScreenViewModel
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.Circle
import com.google.android.libraries.maps.model.CircleOptions
import com.google.android.libraries.maps.model.Marker
import com.google.android.libraries.maps.model.MarkerOptions
import com.google.maps.android.ktx.awaitMap
import kotlinx.coroutines.launch


val CURRENT_CIRCLE_OPTIONS =
    CircleOptions()
        .strokeWidth(2f)
        .strokeColor(0x33DCD90D)
        .fillColor(0x44DCD90D)
        .visible(true)
        .zIndex(100f)

val ENTER_CIRCLE_OPTIONS =
    CircleOptions()
        .strokeWidth(2f)
        .strokeColor(0x33FF2F09)
        .fillColor(0x44DCD90D)
        .visible(true)
        .zIndex(100f)

val EXIT_CIRCLE_OPTIONS =
    CircleOptions()
        .strokeWidth(2f)
        .strokeColor(0x33BEB7B5)
        .fillColor(0x44DCD90D)
        .visible(true)
        .zIndex(100f)

val CURRENT_MARKER_OPTIONS =
    MarkerOptions()
        .visible(true)

val ENTER_MARKER_OPTIONS =
    MarkerOptions()
        .visible(true)

val EXIT_MARKER_OPTIONS =
    MarkerOptions()
        .visible(true)

@SuppressLint("MissingPermission")
fun MapInitializer(googleMap: GoogleMap, alarms: List<Alarm>, permissionsGranted: Boolean){

    Log.i("MapInitializer", "Map Initialized")

    googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
    googleMap.isMyLocationEnabled = permissionsGranted


    for (alarm in alarms) {
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
}


@Composable
fun MapViewContainer(
    map: MapView,
    alarms : LiveData<List<Alarm>>,
    permissionsGranted: Boolean,
    currentCircleSize: () -> Double,
    MoveMarker: (Marker?, Circle?) -> Unit,
) {

    val coroutineScope = rememberCoroutineScope()
    var isMapInitialized by rememberSaveable{ mutableStateOf(false) }


    AndroidView({ map }) { mapView ->

        coroutineScope.launch {
            val googleMap = mapView.awaitMap()

            Log.d("MapViewContainer", "couroutineScope : Map rendered")

            if (!isMapInitialized) {

                alarms.value?.let {

                    // TODO: after exiting app and reopening, none of the markers are visible. Change lifecycle methods to toggle isMapInitialized to false when map is destroyed

                    MapInitializer(
                        googleMap = googleMap,
                        alarms = it,
                        permissionsGranted = permissionsGranted
                    )
                    isMapInitialized = true
                }

                if (!isMapInitialized) alarms.observeForever {
                    it.lastOrNull()?.let { alarm ->
                        Log.d("MapViewContainer", "observeForever : alarms list changed")
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
                }
            }

            googleMap.setOnMarkerClickListener {
                MoveMarker(null, null)
                true
            }
            googleMap.setOnMapClickListener {

                val circle = googleMap.addCircle(
                    CURRENT_CIRCLE_OPTIONS
                        .center(it)
                        .radius(currentCircleSize())
                )


                val marker = googleMap.addMarker(
                    CURRENT_MARKER_OPTIONS
                        .position(it)
                        .title("Location")
                        .snippet("Lat: %.4f Long: %.4f".format(it.latitude, it.longitude))
                )

                MoveMarker(marker, circle)
                marker.showInfoWindow()

            }
            googleMap.setOnPoiClickListener { poi ->


                val circle = googleMap.addCircle(
                    CURRENT_CIRCLE_OPTIONS
                        .center(poi.latLng)
                        .radius(currentCircleSize())

                )

                val poiMarker = googleMap.addMarker(
                    MarkerOptions()
                        .position(poi.latLng)
                        .title(poi.name)
                        .snippet(
                            "Lat: %.4f Long: %.4f".format(
                                poi.latLng.latitude,
                                poi.latLng.longitude
                            )
                        )
                )

                MoveMarker(poiMarker, circle)
                poiMarker.showInfoWindow()
            }
        }

    }
}

@Composable
fun MarkerSaveMenu(mapViewModel: MapScreenViewModel) {

    val alarmName by mapViewModel.alarmName.observeAsState("")
    val areaRadius by mapViewModel.areaRadius.observeAsState()
    val sliderValue by mapViewModel.sliderPosition.observeAsState(0f)
    val alarmType by mapViewModel.alarmType.observeAsState(AlarmType.ON_ENTRY)

    Column(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
    ) {

        Row() {

            IconButton(onClick = { mapViewModel.onMoveMarker(null, null) }) {
                Icon(Icons.Default.Close, contentDescription = "Cancel")
            }

            TextButton(onClick = {
                mapViewModel.addAlarm(false)
                mapViewModel.onMoveMarker(null, null)
            }) {
                Text("SAVE")
            }

            TextButton(onClick = {
                mapViewModel.addAlarm(true)
                mapViewModel.onMoveMarker(null, null)
            }) {
                Text("START")
            }
        }

        Text("Radius : ${areaRadius} m")
        Slider(value = sliderValue, onValueChange = { mapViewModel.onChangeSlider(it) })

        Row {
            IconButton(onClick = { mapViewModel.onChangeAlarmType(AlarmType.ON_ENTRY) }) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "On Entry",
                    tint =  if (alarmType == AlarmType.ON_ENTRY) Color(0xFFFF0000) else Color(0xFF000000)
                )
            }

            IconButton(onClick = { mapViewModel.onChangeAlarmType(AlarmType.ON_EXIT) }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "On Exit",
                    tint = if (alarmType == AlarmType.ON_EXIT) Color(0xFFFF0000) else Color(0xFF000000)
                )
            }
        }

        OutlinedTextField(
            value = alarmName,
            onValueChange = { mapViewModel.onAlarmNameChange(it) },
            label = { Text("Alarm Name") }
        )

    }
}

@ExperimentalAnimationApi
@Composable
fun MainMapScreen(
    navController: NavController,
    permissionsGranted: Boolean,
    mapViewModel: MapScreenViewModel
) {

    //Store context
    val context = LocalContext.current

    //Data
    val lastMarker: Marker? by mapViewModel.lastMarker.observeAsState()
    val lastCircle: Circle? by mapViewModel.lastCircle.observeAsState()
    val areaRadius: Int? by mapViewModel.areaRadius.observeAsState()


    Scaffold(
        topBar = {
            TopAppBar(backgroundColor = Color(51, 65, 145)) {
                IconButton(onClick = {
                    Toast.makeText(context, "Button clicked", Toast.LENGTH_SHORT).show()
                    navController.navigate("alarms")
                }) {
                    Icon(Icons.Default.Menu, "Menu", tint = Color.White)
                }
                Text(text = "Geo Alarm")

            }
        }
    ) {

        Box {

            Box {
                MapViewContainer(
                    map = rememberMapViewWithLifecycle(),
                    alarms = mapViewModel.alarms,
                    permissionsGranted = permissionsGranted,
                    currentCircleSize = { (areaRadius ?: 0).toDouble() },
                    MoveMarker = { m: Marker?, c: Circle? -> mapViewModel.onMoveMarker(m, c) }
                )
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .align(Alignment.BottomCenter)
            ) {

                AnimatedVisibility(
                    visible = (lastMarker != null),
                    enter = slideInVertically(animationSpec = tween(durationMillis = 1000)),
                    exit = slideOutVertically(animationSpec = tween(durationMillis = 1000))
                ) {

                    MarkerSaveMenu(mapViewModel)
                }
            }
        }
    }
}

// TODO : design UI to add alarm
// TODO : design UI to display alarms
// TODO : add alarm features
