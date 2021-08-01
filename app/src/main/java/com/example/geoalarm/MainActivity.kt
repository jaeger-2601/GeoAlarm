package com.example.geoalarm

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.geoalarm.data.MapScreenViewModel
import com.example.geoalarm.data.MapScreenViewModelFactory
import com.example.geoalarm.data.room.GeoAlarmDatabase


class MainActivity : ComponentActivity() {

    @ExperimentalAnimationApi
    @Composable
    fun navScreens(permissionGranted: Boolean, viewModels: Map<String, ViewModel>){

        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "map") {
            composable("map") {
                MainMapScreen(
                    navController,
                    permissionGranted,
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
                this, AlarmsScreenViewModelFactory(dataSource)).get(AlarmsScreenViewModel::class.java)
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

        setContent {
            navScreens(
                permissionsGranted,
                viewModels
            )
        }
    }


}

