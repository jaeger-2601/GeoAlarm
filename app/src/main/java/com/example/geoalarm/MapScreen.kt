package com.example.geoalarm

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import com.example.geoalarm.data.Alarm
import com.example.geoalarm.data.AlarmType
import com.example.geoalarm.data.MapScreenViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.GeofencingClient
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.Circle
import com.google.android.libraries.maps.model.Marker
import com.google.android.libraries.maps.model.MarkerOptions
import com.google.maps.android.ktx.awaitMap
import kotlinx.coroutines.launch


@SuppressLint("MissingPermission")
@Composable
fun MapViewContainer(
    map: MapView,
    alarms : LiveData<List<Alarm>>,
    MapInitializer: (GoogleMap) -> Unit,
    MapUpdate: (GoogleMap) -> Unit,
    isMapInitialized: LiveData<Boolean>,
    currentCircleSize: () -> Double,
    MoveMarker: (Marker?, Circle?) -> Unit,
    isLastMarker: (Marker) -> Boolean
) {

    val coroutineScope = rememberCoroutineScope()
    val isMapInit by isMapInitialized.observeAsState(false)

    AndroidView({ map }) { mapView ->

        coroutineScope.launch {
            val googleMap = mapView.awaitMap()

            Log.d("MapViewContainer", "coroutineScope : Map rendered")

            if (!isMapInit) {

                // LiveData is an asynchronous query, you get the LiveData object but it might contain no data.
                // LiveData is to watch the data and distribute it to the observers. It won't calculate the value until an active observer is added.
                // i.e it won't run the query and initialize alarms until an active observer is added
                // Only call alarms.observeForever when alarms.value is equal to null

                if (alarms.value == null) {
                    Log.i("MapViewContainer", "Observing alarms")
                    alarms.observeForever {

                        Log.d("MapViewContainer", "observeForever : alarms list changed")
                        MapUpdate(googleMap)

                    }
                }
                MapInitializer(googleMap)

            }

            googleMap.setOnMarkerClickListener {

                if (isLastMarker(it)) {
                    MoveMarker(null, null)
                    true
                }
                else {
                    it.showInfoWindow()
                    true
                }
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

@ExperimentalPermissionsApi
@Composable
fun MarkerSaveMenu(mapViewModel: MapScreenViewModel, geofencingClient: GeofencingClient, geofencePendingIntent: PendingIntent) {

    val alarmName by mapViewModel.alarmName.observeAsState("")
    val areaRadius by mapViewModel.areaRadius.observeAsState()
    val sliderValue by mapViewModel.sliderPosition.observeAsState(0f)
    val alarmType by mapViewModel.alarmType.observeAsState(AlarmType.ON_ENTRY)

    val context = LocalContext.current

    var doNotShowRationale by rememberSaveable { mutableStateOf(false) }
    val bgLocationPermissionState = rememberPermissionState(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    PermissionRequired(
        permissionState = bgLocationPermissionState,
        permissionNotGrantedContent = {
            if (doNotShowRationale) {
                Text("Feature not available")
            } else {
                Column(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Color.White)) {
                    Text("To set up GeoAlarms, we need access to your location at all times. Please grant the permission.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Button(onClick = { bgLocationPermissionState.launchPermissionRequest() }) {
                            Text("Ok!")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { doNotShowRationale = true }) {
                            Text("Nope")
                        }
                    }
                }
            }
        },
        permissionNotAvailableContent = {
            Column {
                Text(
                    "Location permission denied. Please, grant us access on the Settings screen."
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {

                    //Navigate to app permissions settings
                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri: Uri = Uri.fromParts("package", context.packageName, null)
                    intent.data = uri
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(context, intent, null)

                }) {
                    Text("Open Settings")
                }
            }
        }
    ) {

        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .wrapContentHeight()
        ) {

            OutlinedTextField(
                value = alarmName,
                onValueChange = { mapViewModel.onAlarmNameChange(it) },
                label = { Text("Alarm Name") },
                modifier = Modifier
                    .padding(20.dp, 10.dp)
                    .fillMaxWidth()
            )


            Text("Radius : $areaRadius m", modifier = Modifier.padding(23.dp, 10.dp, 10.dp, 0.dp))
            Slider(
                value = sliderValue,
                onValueChange = { mapViewModel.onChangeSlider(it) },
                modifier = Modifier.padding(15.dp, 0.dp, 15.dp, 5.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 0.dp, 0.dp, 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(
                    onClick = { mapViewModel.onChangeAlarmType(AlarmType.ON_ENTRY) },
                    modifier = Modifier
                        .background(
                            if (alarmType == AlarmType.ON_ENTRY) Color(0xFF6200ee) else Color(
                                0xFFFFFFFF
                            )
                        )
                        .fillMaxWidth(0.35F)
                ) {
                    Text(
                        "Entry",
                        color = if (alarmType == AlarmType.ON_ENTRY) Color(0xFFFFFFFF) else Color(
                            0xFF000000
                        )
                    )

                }
                TextButton(
                    onClick = { mapViewModel.onChangeAlarmType(AlarmType.ON_EXIT) },
                    modifier = Modifier
                        .background(
                            if (alarmType == AlarmType.ON_EXIT) Color(0xFF6200ee) else Color(
                                0xFFFFFFFF
                            )
                        )
                        .fillMaxWidth(0.5F)
                ) {
                    Text(
                        "Exit",
                        color = if (alarmType == AlarmType.ON_EXIT) Color(0xFFFFFFFF) else Color(
                            0xFF000000
                        )
                    )

                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

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
                    mapViewModel.addAlarm(true)?.let {
                        addGeofence(
                            geofencingClient,
                            geofencePendingIntent,
                            it,
                            context,
                            success = { Log.i("MarkerSaveMenu", "Geofence successfully added") },
                            failure = { error, _-> Log.e("MarkerSaveMenu", error) }
                        )
                    }


                    mapViewModel.onMoveMarker(null, null)
                }) {
                    Text("START")
                }

            }
        }
    }
}

@ExperimentalPermissionsApi
@ExperimentalAnimationApi
@Composable
fun MainMapScreen(
    navController: NavController,
    geofencingClient: GeofencingClient,
    geofencePendingIntent: PendingIntent,
    mapViewModel: MapScreenViewModel
) {

    //Store context
    val context = LocalContext.current

    //Data
    val lastMarker: Marker? by mapViewModel.lastMarker.observeAsState()
    val areaRadius: Int? by mapViewModel.areaRadius.observeAsState()

    var doNotShowRationale by rememberSaveable { mutableStateOf(false) }
    val locationPermissionState = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)


    PermissionRequired(
        permissionState = locationPermissionState,
        permissionNotGrantedContent = {
            if (doNotShowRationale) {
                Text("Feature not available")
            } else {
                Column {
                    Text("To set up GeoAlarms, we need your location. Please grant the permission.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Button(onClick = { locationPermissionState.launchPermissionRequest() }) {
                            Text("Ok!")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { doNotShowRationale = true }) {
                            Text("Nope")
                        }
                    }
                }
            }
        },
        permissionNotAvailableContent = {
            Column {
                Text(
                    "Location permission denied. Please, grant us access on the Settings screen."
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {

                    //Navigate to app permissions settings
                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri: Uri = Uri.fromParts("package", context.packageName, null)
                    intent.data = uri
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(context, intent, null)

                }) {
                    Text("Open Settings")
                }
            }
        }
    ) {


        Scaffold(
            topBar = {
                TopAppBar(backgroundColor = Color(51, 65, 145)) {
                    IconButton(onClick = {
                        navController.navigate("alarms")
                        mapViewModel.onMoveMarker(null, null)
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
                        map = rememberMapViewWithLifecycle { mapViewModel.onMapDestroyed() },
                        alarms = mapViewModel.alarms,
                        isMapInitialized = mapViewModel.isMapInitialized,
                        MapInitializer = { mMap-> mapViewModel.mapInitializer(mMap) },
                        MapUpdate = { mapViewModel.mapUpdate(it) },
                        currentCircleSize = { (areaRadius ?: 0).toDouble() },
                        MoveMarker = { m: Marker?, c: Circle? -> mapViewModel.onMoveMarker(m, c) },
                        isLastMarker = { it.position == lastMarker?.position }
                    )
                }

                Box(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.BottomCenter)
                ) {

                    AnimatedVisibility(
                        visible = (lastMarker != null),
                        enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
                        exit = fadeOut(animationSpec = tween(durationMillis = 1000))
                    ) {

                        MarkerSaveMenu(mapViewModel, geofencingClient, geofencePendingIntent)
                    }
                }
            }
        }
    }

}

// TODO : display a notification, vibrate, emit sound a

