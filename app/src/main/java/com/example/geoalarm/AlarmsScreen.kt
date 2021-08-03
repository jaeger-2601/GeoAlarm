package com.example.geoalarm

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.geoalarm.data.Alarm
import com.example.geoalarm.data.AlarmType

@Composable
fun AlarmInfo(checked : Boolean, onCheckedChange: (Boolean) -> Unit?){
    Switch(checked = checked, onCheckedChange = { onCheckedChange(it) })
}

@Composable
fun AlarmScreen(viewModel: AlarmsScreenViewModel){

    val alarms by viewModel.alarms.observeAsState()

    Log.d("AlarmScreen", "Function composed")

    Column {
        alarms?.let{

            for (i in 0..it.lastIndex){
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {


                        Row(modifier = Modifier.fillMaxWidth()) {
                            Icon(
                                if (it[i].type == AlarmType.ON_ENTRY) Icons.Default.ArrowForward else Icons.Default.ArrowBack,
                                contentDescription = if (it[i].type == AlarmType.ON_ENTRY) "On Entry" else "On Exit"
                            )

                            Text(
                                it[i].name
                            )

                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "Location Icon"
                            )

                            Text(
                                "${it[i].radius} m"
                            )
                        }
                        Row {
                            IconButton(onClick = { /*TODO*/ }) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = "Delete"
                                )
                            }

                            IconButton(onClick = { /*TODO*/ }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit"
                                )
                            }

                            Switch(checked = viewModel.database.isAlarmActive(it[i].id).observeAsState(false).value, onCheckedChange = { checked ->
                                viewModel.toggleAlarm(it[i])
                            })

                        }
                    }
                }
            }
        }
    }

}