package com.example.geoalarm

import com.google.android.libraries.maps.model.CircleOptions
import com.google.android.libraries.maps.model.MarkerOptions

val CURRENT_CIRCLE_OPTIONS: CircleOptions =
    CircleOptions()
        .strokeWidth(2f)
        .strokeColor(0x33DCD90D)
        .fillColor(0x44DCD90D)
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
        .fillColor(0x44DCD90D)
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

