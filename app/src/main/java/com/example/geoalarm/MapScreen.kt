package com.example.geoalarm

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import com.example.geoalarm.data.Alarm
import com.example.geoalarm.data.AlarmType
import com.example.geoalarm.data.MapScreenViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.*
import com.google.maps.android.ktx.awaitMap
import kotlinx.coroutines.launch


object GeofenceErrorMessages {
    fun getErrorString(context: Context, e: Exception): String {
        return if (e is ApiException) {
            getErrorString(context, e.statusCode)
        } else {
            context.resources.getString(R.string.geofence_unknown_error)
        }
    }

    fun getErrorString(context: Context, errorCode: Int): String {

        val resources = context.resources

        val GEOFENCE_IMPROPER_PERMISSIONS = 13

        return when (errorCode) {

            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE ->
                resources.getString(R.string.geofence_not_available)

            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES ->
                resources.getString(R.string.geofence_too_many_geofences)

            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS ->
                resources.getString(R.string.geofence_too_many_pending_intents)

            GEOFENCE_IMPROPER_PERMISSIONS ->
                "Improper permissions"

            else -> resources.getString(R.string.geofence_unknown_error)
        }
    }
}


fun getGeofencingRequest(geofence: Geofence): GeofencingRequest {

    return GeofencingRequest.Builder().apply {

        setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
        addGeofence(geofence)

    }.build()
}

private fun buildGeofence(alarm: Alarm): Geofence? {


    return Geofence.Builder()
        .setRequestId(alarm.id.toString())
        .setCircularRegion(
            alarm.location.latitude,
            alarm.location.longitude,
            alarm.radius.toFloat()
        )
        .setTransitionTypes(if (alarm.type == AlarmType.ON_ENTRY) Geofence.GEOFENCE_TRANSITION_ENTER else Geofence.GEOFENCE_TRANSITION_EXIT )
        .setExpirationDuration(Geofence.NEVER_EXPIRE)
        .build()


}

private fun buildGeofencingRequest(geofence: Geofence): GeofencingRequest {
    return GeofencingRequest.Builder()
        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
        .addGeofences(listOf(geofence))
        .build()
}

@SuppressLint("MissingPermission")
fun addGeofence(
    geofencingClient: GeofencingClient,
    geofencePendingIntent: PendingIntent,
    alarm: Alarm,
    context: Context,
    success: () -> Unit,
    failure: (error: String, exception: Exception) -> Unit) {
    // 1
    val geofence = buildGeofence(alarm)
    if (geofence != null
        && ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        // 2
        geofencingClient
            .addGeofences(buildGeofencingRequest(geofence), geofencePendingIntent)
            // 3
            .addOnSuccessListener {
                //saveAll(getAll() + reminder)
                success()
            }
            // 4
            .addOnFailureListener {
                failure(GeofenceErrorMessages.getErrorString(context, it), it)
            }
    }
}


@SuppressLint("MissingPermission")
@Composable
fun MapViewContainer(
    map: MapView,
    alarms : LiveData<List<Alarm>>,
    MapInitializer: (GoogleMap, Boolean) -> Unit,
    MapUpdate: (GoogleMap) -> Unit,
    isMapInitialized: LiveData<Boolean>,
    permissionsGranted: Boolean,
    currentCircleSize: () -> Double,
    MoveMarker: (Marker?, Circle?) -> Unit,
    isLastMarker: (Marker) -> Boolean
) {

    val coroutineScope = rememberCoroutineScope()
    val isMapInit by isMapInitialized.observeAsState(false)
    val context = LocalContext.current

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
                MapInitializer(googleMap, permissionsGranted)

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

@Composable
fun MarkerSaveMenu(mapViewModel: MapScreenViewModel, geofencingClient: GeofencingClient, geofencePendingIntent: PendingIntent) {

    val alarmName by mapViewModel.alarmName.observeAsState("")
    val areaRadius by mapViewModel.areaRadius.observeAsState()
    val sliderValue by mapViewModel.sliderPosition.observeAsState(0f)
    val alarmType by mapViewModel.alarmType.observeAsState(AlarmType.ON_ENTRY)
    val lastMarker by mapViewModel.lastMarker.observeAsState()

    val context = LocalContext.current

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
        Slider(value = sliderValue, onValueChange = { mapViewModel.onChangeSlider(it) }, modifier = Modifier.padding(15.dp, 0.dp, 15.dp, 5.dp))

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 0.dp, 0.dp, 10.dp), horizontalArrangement = Arrangement.SpaceEvenly){
            TextButton(
                onClick = { mapViewModel.onChangeAlarmType(AlarmType.ON_ENTRY) },
                modifier = Modifier
                    .background(
                        if (alarmType == AlarmType.ON_ENTRY) Color(0xFF6200ee) else Color(
                            0xFFFFFFFF
                        )
                    )
                    .fillMaxWidth(0.35F)) {
                Text("Entry", color =  if (alarmType == AlarmType.ON_ENTRY) Color(0xFFFFFFFF) else Color(0xFF000000))

            }
            TextButton(
                onClick = { mapViewModel.onChangeAlarmType(AlarmType.ON_EXIT) },
                modifier = Modifier
                    .background(
                        if (alarmType == AlarmType.ON_EXIT) Color(0xFF6200ee) else Color(
                            0xFFFFFFFF
                        )
                    )
                    .fillMaxWidth(0.5F)) {
                Text("Exit", color = if (alarmType == AlarmType.ON_EXIT) Color(0xFFFFFFFF) else Color(0xFF000000))

            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {

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
                mapViewModel.addAlarm(true)?.let{
                    addGeofence(
                        geofencingClient,
                        geofencePendingIntent,
                        it,
                        context,
                        success = { Log.i("MarkerSaveMenu", "Geofence successfully added") },
                        failure = { error, exception -> Log.e("MarkerSaveMenu", error) }
                    )
                }


                mapViewModel.onMoveMarker(null, null)
            }) {
                Text("START")
            }

        }
    }
}

@ExperimentalAnimationApi
@Composable
fun MainMapScreen(
    navController: NavController,
    permissionsGranted: Boolean,
    geofencingClient: GeofencingClient,
    geofencePendingIntent: PendingIntent,
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
                    map = rememberMapViewWithLifecycle ({ mapViewModel.onMapDestroyed() }),
                    alarms = mapViewModel.alarms,
                    isMapInitialized = mapViewModel.isMapInitialized,
                    MapInitializer = { mMap, permissions -> mapViewModel.MapInitializer(mMap, permissions) },
                    MapUpdate = { mapViewModel.MapUpdate(it) },
                    permissionsGranted = permissionsGranted,
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

// TODO : display a notification, vibrate, emit sound a
// TODO : use geofencing API to add alarm features

