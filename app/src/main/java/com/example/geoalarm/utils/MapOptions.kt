package com.example.geoalarm.utils

import com.google.android.libraries.maps.model.CircleOptions
import com.google.android.libraries.maps.model.Dash
import com.google.android.libraries.maps.model.Gap
import com.google.android.libraries.maps.model.MarkerOptions

val CURRENT_CIRCLE_OPTIONS: CircleOptions =
    CircleOptions()
        .strokeWidth(5f)
        .strokeColor(0x79FF0000)
        .strokePattern(listOf(Dash(30F), Gap(20F)))
        .fillColor(0x44FFA500)
        .visible(true)
        .zIndex(100f)

val ENTER_CIRCLE_OPTIONS: CircleOptions =
    CircleOptions()
        .strokeWidth(2f)
        .strokeColor(0x33FF2F09)
        .fillColor(0x44DCD90D)
        .visible(true)
        .zIndex(100f)

val EXIT_CIRCLE_OPTIONS: CircleOptions =
    CircleOptions()
        .strokeWidth(2f)
        .strokeColor(0x33BEB7B5)
        .fillColor(0x4400008B)
        .visible(true)
        .zIndex(100f)

val CURRENT_MARKER_OPTIONS: MarkerOptions =
    MarkerOptions()
        .visible(true)

val ENTER_MARKER_OPTIONS: MarkerOptions =
    MarkerOptions()
        .visible(true)

val EXIT_MARKER_OPTIONS: MarkerOptions =
    MarkerOptions()
        .visible(true)

