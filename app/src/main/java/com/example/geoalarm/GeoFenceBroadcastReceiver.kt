package com.example.geoalarm

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.geoalarm.data.Alarm
import com.example.geoalarm.data.room.AlarmsDao
import com.example.geoalarm.data.room.GeoAlarmDatabase
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.runBlocking
import android.media.RingtoneManager
import android.net.Uri

import android.media.Ringtone
import android.os.Build


const val TAG = "GeoFenceBroadcastReceiver"

class GeoFenceBroadcastReceiver : BroadcastReceiver() {
    // ...
    @SuppressLint("LongLogTag")
    override fun onReceive(context: Context, intent: Intent?) {

        val dataSource = GeoAlarmDatabase.getInstance(context).alarmsDao()

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes
                .getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, errorMessage)
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
        geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            //build and send notification
            buildNotification(context, geofencingEvent, dataSource)?.let {

                with(NotificationManagerCompat.from(context)){

                    Log.i(TAG, "Notification sent")
                    this.notify(0, it)
                    val notification =
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    val r = RingtoneManager.getRingtone(context, notification)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        r.isLooping = false
                    }
                    r.play()

                }
            }

            Log.i(TAG, "Broadcast received successfully")
        } else {
            // Log the error.
            Log.e(TAG, "Error while processing geo fence broadcast")
        }
    }



    fun buildNotification(context: Context?, geofencingEvent: GeofencingEvent, dataSource: AlarmsDao): Notification? {

        //val fullScreenIntent = Intent(context, CallActivity::class.java)
        //val fullScreenPendingIntent = PendingIntent.getActivity(context, 0,
        //    fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val geofence = geofencingEvent.triggeringGeofences[0]
        val alarm =  get(dataSource, geofence.requestId.toInt())

        val fullScreenIntent = Intent(context, AlarmActivity::class.java)
        val fullScreenPendingIntent = PendingIntent.getActivity(context, 0,
            fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmSound: Uri =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val notificationBuilder =
            context?.let {
                NotificationCompat.Builder(it, "GeoAlarm")
                    .setSmallIcon(R.drawable.maps_icon_direction)
                    .setContentTitle(alarm?.name)
                    .setContentText("Lat: %.4f Long: %.4f".format(
                        alarm?.location?.latitude,
                        alarm?.location?.longitude
                    ))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSound(alarmSound)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVibrate(longArrayOf(500, 500, 500, 500, 500, 500, 500, 500, 500))
                    .setSound(alarmSound)
                    .setContentIntent(fullScreenPendingIntent)
                    .setFullScreenIntent(fullScreenPendingIntent, true)

                    // Use a full-screen intent only for the highest-priority alerts where you
                    // have an associated activity that you would like to launch after the user
                    // interacts with the notification. Also, if your app targets Android 10
                    // or higher, you need to request the USE_FULL_SCREEN_INTENT permission in
                    // order for the platform to invoke this notification.
                    //.setFullScreenIntent(fullScreenPendingIntent, true)
            }

        return notificationBuilder?.build()
    }

    fun get(dataSource: AlarmsDao, id: Int): Alarm? {
        return runBlocking { dataSource.get(id) }
    }
}