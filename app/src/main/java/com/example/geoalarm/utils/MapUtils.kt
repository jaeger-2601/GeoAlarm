package com.example.geoalarm

import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.libraries.maps.MapView


/**
 * Remembers a MapView and gives it the lifecycle of the current LifecycleOwner
 */

@Composable
fun rememberMapViewWithLifecycle(onMapDestroy: () -> Unit): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            id = R.id.map
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        // Make MapView follow the current lifecycle
        val lifecycleObserver = getMapLifecycleObserver(mapView, onMapDestroy)
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return mapView
}

private fun getMapLifecycleObserver(mapView: MapView, onMapDestroy: () -> Unit): LifecycleEventObserver =
    LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                Log.i("getMapLifecycleObserver", "onCreate")
                mapView.onCreate(Bundle())
            }
            Lifecycle.Event.ON_START -> {
                Log.i("getMapLifecycleObserver", "onStart")
                mapView.onStart()
            }
            Lifecycle.Event.ON_RESUME -> {
                Log.i("getMapLifecycleObserver", "onResume")
                mapView.onResume()
            }
            Lifecycle.Event.ON_PAUSE -> {
                Log.i("getMapLifecycleObserver", "onPause")
                mapView.onPause()
            }
            Lifecycle.Event.ON_STOP -> {
                Log.i("getMapLifecycleObserver", "onStop")
                mapView.onStop()
            }
            Lifecycle.Event.ON_DESTROY -> {
                Log.i("getMapLifecycleObserver", "MapView destroyed")
                onMapDestroy()
                mapView.onDestroy()
            }
            else -> throw IllegalStateException()
        }
    }






