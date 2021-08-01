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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.geoalarm.data.MapScreenViewModel
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.Circle
import com.google.android.libraries.maps.model.CircleOptions
import com.google.android.libraries.maps.model.Marker
import com.google.android.libraries.maps.model.MarkerOptions
import com.google.maps.android.ktx.awaitMap
import kotlinx.coroutines.launch


@SuppressLint("MissingPermission")
@Composable
fun MapViewContainer(
    map: MapView,
    permissionsGranted: Boolean,
    lastMarker: Marker?,
    lastCircle: Circle?,
    currentCircleSize: () -> Double,
    onChangeMarker:(Marker?, Circle?) -> Unit,
) {

    val coroutineScope = rememberCoroutineScope()


        AndroidView({ map }) { mapView ->

            coroutineScope.launch {
                val googleMap = mapView.awaitMap()

                Log.d("MapViewContainer", "couroutineScope : Map rendered")

                googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                googleMap.isMyLocationEnabled = permissionsGranted

                googleMap.setOnMarkerClickListener {
                    onChangeMarker(null, null)
                    true
                }

                googleMap.setOnMapClickListener {

                    Log.i("MapViewContainer", "coroutineScope : ${lastMarker?.position}")

                    val circle = googleMap.addCircle(
                        CircleOptions()
                            .center(it)
                            .radius(currentCircleSize())
                            .strokeWidth(2f)
                            .strokeColor(0x33DCD90D)
                            .fillColor(0x44DCD90D)
                            .visible(true)
                            .zIndex(100f)

                    )


                    val marker = googleMap.addMarker(
                        MarkerOptions()
                            .position(it)
                            .title("Location")
                            .snippet("Lat: %.4f Long: %.4f".format(it.latitude, it.longitude))
                    )

                    onChangeMarker(marker, circle)
                    marker.showInfoWindow()

                }

                googleMap.setOnPoiClickListener { poi ->

                    Log.i("MapViewContainer", "coroutineScope : ${lastMarker?.position}")

                    val circle = googleMap.addCircle(
                        CircleOptions()
                            .center(poi.latLng)
                            .radius(currentCircleSize())
                            .strokeWidth(2f)
                            .strokeColor(0x33DCD90D)
                            .fillColor(0x44DCD90D)
                            .visible(true)
                            .zIndex(100f)

                    )

                    val poiMarker = googleMap.addMarker(
                        MarkerOptions()
                            .position(poi.latLng)
                            .title(poi.name)
                            .snippet("Lat: %.4f Long: %.4f".format(
                                poi.latLng.latitude,
                                poi.latLng.longitude
                            ))
                    )

                    onChangeMarker(poiMarker, circle)
                    poiMarker.showInfoWindow()
                }
            }

        }
    }

@Composable
fun MarkerSaveMenu(sliderValue: Float, areaRadius: Int, onChangeSlider: (Float) -> Unit, alarmName: String, onNameChange: (String) -> Unit){

    Column (
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
    ){

        Row() {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Details")
            }

            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Default.Close, contentDescription = "Cancel")
            }

            TextButton(onClick = { /*TODO*/ }) {
                Text("SAVE")
            }

            TextButton(onClick = { /*TODO*/ }) {
                Text("START")
            }
        }

        Text("Radius : %.0f m".format())
        Slider(value = sliderValue, onValueChange = onChangeSlider)

        Row {
            IconButton(onClick = {}) {
                Icon(Icons.Default.ArrowForward, contentDescription = "On Entry")
            }

            IconButton(onClick = {}) {
                Icon(Icons.Default.ArrowBack, contentDescription = "On Exit")
            }
        }

        OutlinedTextField(
            value = alarmName,
            onValueChange = onNameChange,
            label =  { Text("Alarm Name") }
        )

    }
}

@ExperimentalAnimationApi
@Composable
fun MainMapScreen(navController: NavController, permissionsGranted: Boolean, mapViewModel: MapScreenViewModel){

    //Store context
    val context = LocalContext.current

    //Data
    val lastMarker: Marker? by mapViewModel.lastMarker.observeAsState()
    val lastCircle: Circle? by mapViewModel.lastCircle.observeAsState()
    val sliderPosition: Float by mapViewModel.sliderPosition.observeAsState(0f)
    val areaRadius: Int? by mapViewModel.areaRadius.observeAsState()
    val alarmName: String by mapViewModel.alarmName.observeAsState("")


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

                Box{
                    MapViewContainer(
                        map = rememberMapViewWithLifecycle(),
                        permissionsGranted = permissionsGranted,
                        lastMarker = lastMarker,
                        lastCircle = lastCircle,
                        currentCircleSize = { (areaRadius ?: 0).toDouble() },
                        onChangeMarker = {m: Marker?, c:Circle? -> mapViewModel.onChangeMarker(m, c) }
                    )
                }

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .align(Alignment.BottomCenter)) {

                    AnimatedVisibility(
                        visible = (lastMarker != null),
                        enter = slideInVertically(animationSpec = tween(durationMillis = 1000)),
                        exit = slideOutVertically(animationSpec = tween(durationMillis = 1000))
                    ) {

                        MarkerSaveMenu(
                            sliderPosition,
                            areaRadius ?: 0,
                            onChangeSlider = { mapViewModel.onChangeSlider(it) },
                            alarmName = alarmName,
                            onNameChange = { mapViewModel.onAlarmNameChange(it) }
                        )
                    }
                }
            }
        }
    }

// TODO : design UI to add alarm
// TODO : design UI to display alarms
// TODO : add alarm features
