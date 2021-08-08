package com.example.geoalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class GeoFenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        StringBuilder().apply {
            append("Action: ${intent.action}\n")
            append("URI: ${intent.toUri(Intent.URI_INTENT_SCHEME)}\n")
            toString().also { log ->
            }
        }
    }
}