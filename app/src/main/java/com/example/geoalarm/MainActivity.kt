package com.example.geoalarm

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.geoalarm.data.room.GeoAlarmDatabase


class MainActivity : ComponentActivity() {

    @ExperimentalAnimationApi
    @Composable
    fun navScreens(permissionGranted: Boolean){
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "map") {
            composable("map") { MainMapScreen(navController, permissionGranted) }
            composable("alarms") { AlarmScreen() }

        }
    }

    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        var permissionsGranted = false

        val application = this.application
        val dataSource = GeoAlarmDatabase.getInstance(application).alarmsDao()
        val viewModelFactory = AlarmsScreenViewModelFactory(dataSource, application)

        val alarmsScreenViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(AlarmsScreenViewModel::class.java)


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
            navScreens(permissionsGranted)
        }
    }


}

