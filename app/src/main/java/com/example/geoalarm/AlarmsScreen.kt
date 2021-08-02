package com.example.geoalarm

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState

@Composable
fun AlarmScreen(viewModel: AlarmsScreenViewModel){

    val alarms by viewModel.alarms.observeAsState()

    Column {
        alarms?.let{
            for (alarm in alarms!!){
                Text(alarm.toString())
            }
        }
    }

}