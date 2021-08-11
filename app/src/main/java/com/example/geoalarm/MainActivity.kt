package com.example.geoalarm

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.geoalarm.data.AlarmsScreenViewModel
import com.example.geoalarm.data.AlarmsScreenViewModelFactory
import com.example.geoalarm.data.MapScreenViewModel
import com.example.geoalarm.data.MapScreenViewModelFactory
import com.example.geoalarm.data.room.GeoAlarmDatabase
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices


class MainActivity : ComponentActivity() {

    lateinit var geofencingClient: GeofencingClient
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeoFenceBroadcastReceiver::class.java)

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().

        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    @ExperimentalAnimationApi
    @Composable
    fun NavScreens(permissionGranted: Boolean, viewModels: Map<String, ViewModel>, geofencingClient: GeofencingClient, geofencePendingIntent: PendingIntent){

        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "map") {
            composable("map") {
                MainMapScreen(
                    navController,
                    permissionGranted,
                    geofencingClient,
                    geofencePendingIntent,
                    viewModels["MapScreen"] as MapScreenViewModel
                )
            }

            composable("alarms") {
                AlarmScreen(
                    viewModels["AlarmScreen"] as AlarmsScreenViewModel
                )
            }

        }
    }

    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        var permissionsGranted = false

        val dataSource = GeoAlarmDatabase.getInstance(this.application).alarmsDao()

        val alarmsScreenViewModel =
            ViewModelProvider(
                this, AlarmsScreenViewModelFactory(dataSource)
            ).get(AlarmsScreenViewModel::class.java)
        val mapScreenViewModel = ViewModelProvider(
            this, MapScreenViewModelFactory(dataSource)).get(MapScreenViewModel::class.java)

        val viewModels = mapOf(
            "AlarmScreen" to alarmsScreenViewModel,
            "MapScreen" to mapScreenViewModel
        )

        //Check if permissions are granted
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            val permissions= arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            ActivityCompat.requestPermissions(this, permissions, 4564564)

            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED) {
                permissionsGranted = true
            }
        }
        else {
            permissionsGranted = true
        }

        geofencingClient = LocationServices.getGeofencingClient(this)

        setContent {
            NavScreens(
                permissionsGranted,
                viewModels,
                geofencingClient,
                geofencePendingIntent
            )
        }
    }


}

