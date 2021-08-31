package com.example.geoalarm

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.geoalarm.data.Alarm
import com.example.geoalarm.data.room.AlarmsDao
import com.example.geoalarm.data.room.GeoAlarmDatabase
import com.example.geoalarm.repository.AlarmsRepository
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


const val TAG = "GeoFenceBroadcastReceiver"

@AndroidEntryPoint
class GeoFenceBroadcastReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: AlarmsRepository

    @SuppressLint("LongLogTag")
    override fun onReceive(context: Context, intent: Intent?) {


        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes
                .getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, errorMessage)
            return
        }

        val fullScreenIntent = Intent(context, AlarmActivity::class.java)

        fullScreenIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        fullScreenIntent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val fullScreenPendingIntent = PendingIntent.getActivity(context, 0,
            fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
        geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            //build and send notification
            buildNotification(context, geofencingEvent, fullScreenPendingIntent)?.let {

                with(NotificationManagerCompat.from(context)){

                    Log.i(TAG, "Notification sent")
                    this.notify(0, it)
                    val notification =
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    val r = RingtoneManager.getRingtone(context, notification)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        r.isLooping = false
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(fullScreenIntent)
                }
            }

            Log.i(TAG, "Broadcast received successfully")
        } else {
            // Log the error.
            Log.e(TAG, "Error while processing geo fence broadcast")
        }
    }



    @SuppressLint("LongLogTag")
    fun buildNotification(context: Context?, geofencingEvent: GeofencingEvent, fullScreenPendingIntent:PendingIntent): Notification? {

        val geofence = geofencingEvent.triggeringGeofences[0]
        val alarm =  runBlocking { repository.getAlarmById(geofence.requestId.toInt()) }

        val alarmSound: Uri =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        if (alarm == null)
            return null

        val notificationBuilder =
            context?.let {
                NotificationCompat.Builder(it, "GeoAlarm")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSound(alarmSound)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVibrate(longArrayOf(500, 500, 500, 500, 500, 500, 500, 500, 500))
                    .setSound(alarmSound)
                    .setContentIntent(fullScreenPendingIntent)
                    .setFullScreenIntent(fullScreenPendingIntent, true)
                    .setSmallIcon(R.drawable.maps_icon_direction)
                    .setContentTitle(alarm.name)
                    .setContentText("Lat: %.4f Long: %.4f".format(
                        alarm.location.latitude,
                        alarm.location.longitude
                    ))

                    // Use a full-screen intent only for the highest-priority alerts where you
                    // have an associated activity that you would like to launch after the user
                    // interacts with the notification. Also, if your app targets Android 10
                    // or higher, you need to request the USE_FULL_SCREEN_INTENT permission in
                    // order for the platform to invoke this notification.
            }

        return notificationBuilder?.build()
    }

}