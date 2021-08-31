package com.example.geoalarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.example.geoalarm.navigation.Directions
import com.example.geoalarm.navigation.NavigationManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val CHANNEL_ID = "GeoAlarm"

    @Inject
    lateinit var navigationManager : NavigationManager

    @RequiresApi(Build.VERSION_CODES.M)
    @ExperimentalMaterialApi
    @ExperimentalPermissionsApi
    @ExperimentalAnimationApi
    @Composable
    fun NavScreens() {

        val navController = rememberNavController()

        navigationManager.command.observe(LocalLifecycleOwner.current) {
            Log.i("NavigationManager", "Navigation to ${it.destination}")
            navController.navigate(it.destination)
        }

        NavHost(navController = navController, startDestination = Directions.Default.destination) {

            composable(Directions.Map.destination) {
                MainMapScreen()
            }

            composable(Directions.Alarms.destination) {
                AlarmScreen()
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


            val channel = NotificationChannel(
                CHANNEL_ID,
                "GeoAlarm",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
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

    @RequiresApi(Build.VERSION_CODES.M)
    @ExperimentalMaterialApi
    @ExperimentalPermissionsApi
    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        createNotificationChannel()

        setContent {
            NavScreens()
        }
    }
}

