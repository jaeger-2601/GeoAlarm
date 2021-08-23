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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    alarms: LiveData<List<Alarm>>,
    mapUpdate: (GoogleMap) -> Unit,
    currentCircleSize: () -> Double,
    moveMarker: (Marker?, Circle?) -> Unit,
    isLastMarker: (Marker) -> Boolean
) {
    val coroutineScope = rememberCoroutineScope()
    val gAlarms by alarms.observeAsState()

    Log.i("Screen", "MapViewContainer")

    AndroidView({ map }) { mapView ->

        coroutineScope.launch {
            val googleMap = mapView.awaitMap()

            Log.i("MapViewContainer", "coroutineScope : Map rendered")

            Log.i("mapUpdate", "Launching mapUpdate : ${gAlarms?.size}")
            mapUpdate(googleMap)

            googleMap.setOnMarkerClickListener {

                if (isLastMarker(it))
                    moveMarker(null, null)
                else
                    it.showInfoWindow()

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

                moveMarker(marker, circle)
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

                moveMarker(poiMarker, circle)
                poiMarker.showInfoWindow()
            }
        }

    }
}

@ExperimentalPermissionsApi
@Composable
fun MarkerSaveMenu(
    mapViewModel: MapScreenViewModel,
    geofencingClient: GeofencingClient,
    geofencePendingIntent: PendingIntent
) {

    val alarmName by mapViewModel.alarmName.observeAsState("")
    val areaRadius by mapViewModel.areaRadius.observeAsState()
    val sliderValue by mapViewModel.sliderPosition.observeAsState(0f)
    val alarmType by mapViewModel.alarmType.observeAsState(AlarmType.ON_ENTRY)

    val context = LocalContext.current

    val bgLocationPermissionState =
        rememberPermissionState(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    Log.i("Screen", "MarkerSaveMenu")

    PermissionRequired(
        permissionState = bgLocationPermissionState,
        permissionNotGrantedContent = {

            Column(
                modifier = Modifier
                    .background(Color(0xFF131E37))
                    .padding(20.dp, 20.dp)
            ) {
                Text(
                    "To set up GeoAlarms, we need access to your location at all times. Please grant the permission.",
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp, 20.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { bgLocationPermissionState.launchPermissionRequest() },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF3EB38A)),
                    ) {
                        Text("Ok!")
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
                    val intent =
                        Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
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
                .background(Color(0xFF131E37))
                .wrapContentHeight()
        ) {

            OutlinedTextField(
                value = alarmName,
                onValueChange = { mapViewModel.onAlarmNameChange(it) },
                label = { Text("Alarm Name") },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    cursorColor = Color.White,
                    unfocusedBorderColor = Color.LightGray,
                    unfocusedLabelColor = Color.DarkGray,
                    focusedBorderColor = Color.LightGray,
                    focusedLabelColor = Color.LightGray
                ),
                modifier = Modifier
                    .padding(20.dp, 10.dp)
                    .fillMaxWidth()
            )

            Text(
                buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Radius : ")
                    }
                    append("$areaRadius m")

                },
                color = Color.White,
                modifier = Modifier.padding(23.dp, 10.dp, 10.dp, 0.dp)
            )

            Slider(
                value = sliderValue,
                onValueChange = { mapViewModel.onChangeSlider(it) },
                modifier = Modifier.padding(15.dp, 0.dp, 15.dp, 5.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xffbd93f9),
                    activeTrackColor = Color(
                        0xffc296ff
                    ),
                    inactiveTrackColor = Color(0xFF8b6ab8)
                )
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
                            if (alarmType == AlarmType.ON_ENTRY) Color(0xFF3EB38A) else Color(
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
                            if (alarmType == AlarmType.ON_EXIT) Color(0xFF3EB38A) else Color(
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
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = Color(0xFFFF5F6E)
                    )
                }

                TextButton(onClick = {
                    mapViewModel.addAlarm(false)
                    mapViewModel.onMoveMarker(null, null)
                }) {
                    Text("Save", color = Color(0xffc296ff))
                }

                TextButton(onClick = {

                    mapViewModel.addAlarm(true)?.let {
                        addGeofence(
                            geofencingClient,
                            geofencePendingIntent,
                            it,
                            context,
                            success = { Log.i("MarkerSaveMenu", "Geofence successfully added") },
                            failure = { error, _ -> Log.e("MarkerSaveMenu", error) }
                        )
                    }


                    mapViewModel.onMoveMarker(null, null)
                }) {
                    Text("Start", color = Color(0xFF3EB38A))
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

    val locationPermissionState =
        rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)

    Log.i("Screen", "MainMapScreen")

    PermissionRequired(
        permissionState = locationPermissionState,
        permissionNotGrantedContent = {

            Column(
                modifier = Modifier
                    .background(Color.White)
                    .fillMaxSize()
            ) {

                Image(
                    painter = painterResource(R.drawable.map_logo),
                    contentDescription = "Map Logo",
                    alignment = Alignment.Center,
                    modifier = Modifier.padding(100.dp, 80.dp, 100.dp, 60.dp)
                )

                Text(
                    "Please give us access to your GPS location",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp, 20.dp, 15.dp, 60.dp)
                )

                Text(
                    "This will us to notify you when you enter a area with a GeoAlarm. This is needed for the core functionality of the app",
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp, 10.dp, 15.dp, 70.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = { locationPermissionState.launchPermissionRequest() },
                        contentPadding = PaddingValues(40.dp, 30.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF3EB38A),
                            contentColor = Color.White
                        ),
                    ) {
                        Text(text = "Ok!", fontSize = 30.sp)
                    }
                }
            }

        },
        permissionNotAvailableContent = {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .fillMaxSize()
            ) {
                Image(
                    painter = painterResource(R.drawable.map_logo),
                    contentDescription = "Red cross",
                    alignment = Alignment.Center,
                    modifier = Modifier.padding(100.dp, 80.dp, 100.dp, 60.dp)
                )
                Text(
                    "Location permission denied too many times. Please, grant us access on the Settings screen.",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp, 20.dp, 15.dp, 90.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))


                OutlinedButton(
                    onClick = {
                        //Navigate to app permissions settings
                        val intent =
                            Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri: Uri = Uri.fromParts("package", context.packageName, null)
                        intent.data = uri
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(context, intent, null)

                    },
                    contentPadding = PaddingValues(30.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF3EB38A),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(text = "Open Settings", fontSize = 20.sp)
                }
            }
        }
    ) {


        Scaffold(
            topBar = {
                TopAppBar(backgroundColor = Color(0xFF141F38)) {
                    IconButton(onClick = {
                        navController.navigate("alarms")
                        mapViewModel.onMoveMarker(null, null)
                    }) {
                        Icon(Icons.Default.Menu, "Menu", tint = Color.White)
                    }
                    Text(text = "Geo Alarm", color = Color.White)


                }
            }
        ) {

            Box {

                Box {
                    MapViewContainer(
                        map = rememberMapViewWithLifecycle { mapViewModel.onMapDestroyed() },
                        alarms = mapViewModel.alarms,
                        currentCircleSize = { (areaRadius ?: 0).toDouble() },
                        mapUpdate = {map -> mapViewModel.mapUpdate(map) },
                        moveMarker = { m: Marker?, c: Circle? -> mapViewModel.onMoveMarker(m, c) },
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

// TODO : make notification vibrate and emit a sound with a full screen activity
// TODO : alarm edit screen

