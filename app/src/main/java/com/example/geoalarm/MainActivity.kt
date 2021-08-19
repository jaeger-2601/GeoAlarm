package com.example.geoalarm

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
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
import android.media.AudioAttributes

import android.R
import android.net.Uri
import android.util.Log
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.runBlocking


class MainActivity : ComponentActivity() {

    private val CHANNEL_ID = "GeoAlarm"

    lateinit var geofencingClient: GeofencingClient
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeoFenceBroadcastReceiver::class.java)

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().

        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    @ExperimentalPermissionsApi
    @ExperimentalAnimationApi
    @Composable
    fun NavScreens(viewModels: Map<String, ViewModel>, geofencingClient: GeofencingClient, geofencePendingIntent: PendingIntent){

        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "map") {
            composable("map") {
                MainMapScreen(
                    navController,
                    geofencingClient,
                    geofencePendingIntent,
                    viewModels["MapScreen"] as MapScreenViewModel
                )
            }

            composable("alarms") {
                AlarmScreen(
                    navController,
                    viewModels["AlarmScreen"] as AlarmsScreenViewModel
                )
            }

        }
    }

    private fun createNotificationChannel() {

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val soundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()


            val channel = NotificationChannel(CHANNEL_ID, "GeoAlarm", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notification channel for GeoAlarm"
                this.setSound(soundUri, audioAttributes)
                this.enableVibration(true)
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


            notificationManager.createNotificationChannel(channel)
        }
    }

            @ExperimentalPermissionsApi
            @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)


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

        geofencingClient = LocationServices.getGeofencingClient(this)

        createNotificationChannel()

        setContent {
            NavScreens(
                viewModels,
                geofencingClient,
                geofencePendingIntent
            )
        }
    }


}

