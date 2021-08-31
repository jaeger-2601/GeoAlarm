package com.example.geoalarm.navigation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NavigationManager {

    private var _command : MutableLiveData<NavigationCommand> = MutableLiveData(Directions.Default)
    val command : LiveData<NavigationCommand>
        get() = _command

    fun navigate(direction: NavigationCommand) {
        _command.value = direction
    }

}