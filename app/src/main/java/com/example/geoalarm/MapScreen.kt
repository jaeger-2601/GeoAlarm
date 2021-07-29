package com.example.geoalarm

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.Marker
import com.google.android.libraries.maps.model.MarkerOptions
import com.google.maps.android.ktx.awaitMap
import kotlinx.coroutines.launch
import kotlin.math.roundToLong


//@SuppressLint("MissingPermission")
@SuppressLint("MissingPermission")
@Composable
fun MapViewContainer(
    map: MapView,
    latitude: String,
    longitude: String,
    permissionsGranted: Boolean,
    lastMarker: Marker?,
    onChangeMarker: (Marker?) -> Unit
) {


    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()


        AndroidView({ map }) { mapView ->

            coroutineScope.launch {
                val googleMap = mapView.awaitMap()

                Log.d("MapViewContainer", "couroutineScope : Map rendered")

                googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                googleMap.isMyLocationEnabled = permissionsGranted

                googleMap.setOnMarkerClickListener {
                    it.remove()
                    onChangeMarker(null)
                    true
                }

                googleMap.setOnMapLongClickListener {

                    Log.i("MapViewContainer", "coroutineScope : ${lastMarker?.position}")
                    lastMarker?.remove()
                    val marker = googleMap.addMarker(
                            MarkerOptions()
                                .position(it)
                                .title("Location")
                                .snippet("Lat: %.4f Long: %.4f"
                                    .format(
                                        it.latitude, it.longitude
                                    )
                                )
                        )
                    onChangeMarker(marker)
                    marker.showInfoWindow()

                }
                googleMap.setOnPoiClickListener { poi ->
                    Log.i("MapViewContainer", "coroutineScope : ${lastMarker?.position}")
                    lastMarker?.remove()
                    val poiMarker = googleMap.addMarker(
                        MarkerOptions()
                            .position(poi.latLng)
                            .title(poi.name)
                            .snippet("Lat: %.4f Long: %.4f".format(
                                poi.latLng.latitude,
                                poi.latLng.longitude
                            ))
                    )
                    onChangeMarker(poiMarker)
                    poiMarker.showInfoWindow()
                }
            }

        }
    }


@ExperimentalAnimationApi
@Composable
fun MainMapScreen(navController: NavController, permissionsGranted: Boolean){

    var lastMarker: Marker? by rememberSaveable {
        mutableStateOf(null)
    }
    val context = LocalContext.current

    Scaffold(
    //    topBar = {
//            TopAppBar(backgroundColor = Color(51, 65, 145)) {
  //              IconButton(onClick = {
  //                  Toast.makeText(context, "Button clicked", Toast.LENGTH_SHORT).show()
   //                 navController.navigate("alarms")
   //             }) {
   //                 Icon(Icons.Default.Menu, "Menu", tint = Color.White)
    //            }
     //           Text(text = "Geo Alarm")
//
    //        }
   //     }
    ) {
        Row{

            Box(
                Modifier
                    .animateContentSize(
                        animationSpec = tween(
                            durationMillis = 1000,
                            easing = LinearOutSlowInEasing
                        )
                    )
                    .height(if (lastMarker == null) 600.dp else 300.dp)
            ){


                MapViewContainer(
                    map = rememberMapViewWithLifecycle(),
                    latitude = "1",
                    longitude = "1",
                    permissionsGranted = permissionsGranted,
                    lastMarker = lastMarker,
                    onChangeMarker = {
                        lastMarker = it
                    }
                )
            }
            AnimatedVisibility(visible = (lastMarker != null)) {

                TextButton(onClick = { /*TODO*/ }) {
                    Text("Save")
                }
            }
        }
    }
}