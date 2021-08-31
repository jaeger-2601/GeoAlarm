package com.example.geoalarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val CHANNEL_ID = "GeoAlarm"

    @RequiresApi(Build.VERSION_CODES.M)
    @ExperimentalMaterialApi
    @ExperimentalPermissionsApi
    @ExperimentalAnimationApi
    @Composable
    fun NavScreens() {

        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "map") {
            composable("map") {
                MainMapScreen(navController)
            }

            composable("alarms") {
                AlarmScreen(navController)
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

