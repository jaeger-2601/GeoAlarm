package com.example.geoalarm

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.util.Log
import com.example.geoalarm.data.Alarm
import com.example.geoalarm.data.AlarmType
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest

object GeofenceErrorMessages {
    fun getErrorString(context: Context, e: Exception): String {
        return if (e is ApiException) {
            getErrorString(context, e.statusCode)
        } else {
            context.resources.getString(R.string.geofence_unknown_error)
        }
    }

    fun getErrorString(context: Context, errorCode: Int): String {

        val resources = context.resources

        val GEOFENCE_IMPROPER_PERMISSIONS = 13

        return when (errorCode) {

            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE ->
                resources.getString(R.string.geofence_not_available)

            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES ->
                resources.getString(R.string.geofence_too_many_geofences)

            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS ->
                resources.getString(R.string.geofence_too_many_pending_intents)

            GEOFENCE_IMPROPER_PERMISSIONS ->
                "Improper permissions"

            else -> resources.getString(R.string.geofence_unknown_error)
        }
    }
}



private fun buildGeofence(alarm: Alarm): Geofence? {

    Log.i("buildGeoFence", "building geofence for ${alarm.id}")

    return Geofence.Builder()
        .setRequestId(alarm.id.toString())
        .setCircularRegion(
            alarm.location.latitude,
            alarm.location.longitude,
            alarm.radius.toFloat()
        )
        .setTransitionTypes(if (alarm.type == AlarmType.ON_ENTRY) Geofence.GEOFENCE_TRANSITION_ENTER else Geofence.GEOFENCE_TRANSITION_EXIT )
        .setExpirationDuration(Geofence.NEVER_EXPIRE)
        .build()


}

private fun buildGeofencingRequest(geofence: Geofence, alarm: Alarm): GeofencingRequest {
    return GeofencingRequest.Builder()
        .setInitialTrigger(if (alarm.type == AlarmType.ON_ENTRY) GeofencingRequest.INITIAL_TRIGGER_ENTER else GeofencingRequest.INITIAL_TRIGGER_EXIT)
        .addGeofences(listOf(geofence))
        .build()
}

@SuppressLint("MissingPermission")
fun addGeofence(
    geofencingClient: GeofencingClient,
    geofencePendingIntent: PendingIntent,
    alarm: Alarm,
    context: Context,
    success: () -> Unit,
    failure: (error: String, exception: Exception) -> Unit) {
    // 1
    val geofence = buildGeofence(alarm)
    if (geofence != null) {
        // 2
        geofencingClient
            .addGeofences(buildGeofencingRequest(geofence, alarm), geofencePendingIntent)
            // 3
            .addOnSuccessListener {
                //saveAll(getAll() + reminder)
                success()
            }
            // 4
            .addOnFailureListener {
                failure(GeofenceErrorMessages.getErrorString(context, it), it)
            }
    }
    else {
        failure("Permissions not granted", java.lang.Exception("Permissions not granted"))
    }
}

fun removeGeoFence(
    geofencingClient: GeofencingClient,
    alarm: Alarm,
    context: Context,
    success: () -> Unit,
    failure: (error: String, exception: Exception) -> Unit
) {
    geofencingClient
        .removeGeofences(mutableListOf(alarm.id.toString(),))
        .addOnSuccessListener {
            //saveAll(getAll() + reminder)
            success()
        }
        // 4
        .addOnFailureListener {
            failure(GeofenceErrorMessages.getErrorString(context, it), it)
        }
}