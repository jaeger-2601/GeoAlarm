package com.example.geoalarm.navigation

interface NavigationCommand {
    val destination: String
}

object Directions {

    val Map = object : NavigationCommand {
        override val destination: String = "map"
    }

    val Alarms = object : NavigationCommand {
        override val destination: String = "alarms"
    }

    val Default = Map
}