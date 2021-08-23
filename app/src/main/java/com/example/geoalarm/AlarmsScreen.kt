package com.example.geoalarm

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.geoalarm.data.Alarm
import com.example.geoalarm.data.AlarmType
import com.example.geoalarm.data.AlarmsScreenViewModel

@ExperimentalMaterialApi
@Composable
fun AlarmCard(alarm: Alarm, isActive: Boolean, toggleAlarm: (Boolean) -> Unit) {

    Log.i("Screen", "AlarmCard")

    Card(modifier = Modifier.fillMaxWidth(), onClick = {}) {
        Row(
            modifier = Modifier.padding(10.dp, 10.dp, 20.dp, 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {

                Row(
                    modifier = Modifier
                        .padding(15.dp, 10.dp, 10.dp, 15.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = alarm.name,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Switch(
                        checked = isActive,
                        onCheckedChange = toggleAlarm,
                        modifier = Modifier.padding(0.dp, 17.dp, 0.dp, 10.dp)
                    )

                }

                Row(
                    modifier = Modifier
                        .padding(15.dp, 0.dp, 10.dp, 0.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    /*
                    }*/

                    Row {
                        Icon(
                            if (alarm.type == AlarmType.ON_ENTRY) Icons.Default.ArrowForward else Icons.Default.ArrowBack,
                            contentDescription = "Location Icon",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp),

                            )

                        Text(
                            text = if (alarm.type == AlarmType.ON_ENTRY) "Entry" else "Exit",
                            fontStyle = FontStyle.Italic,
                            color = Color.Gray
                        )
                    }

                    Row {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Location Icon",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp),

                            )

                        Text(
                            text = "%.4f, %.4f".format(
                                alarm.location.latitude,
                                alarm.location.longitude
                            ),
                            fontStyle = FontStyle.Italic,
                            color = Color.Gray
                        )
                    }

                    Row {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search icon",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp),

                            )

                        Text(
                            text = "${alarm.radius} m",
                            fontStyle = FontStyle.Italic,
                            color = Color.Gray
                        )
                    }


                }
            }

        }
    }
}


@ExperimentalMaterialApi
@Composable
fun AlarmScreen(navController: NavHostController, viewModel: AlarmsScreenViewModel) {

    val alarms by viewModel.alarms.observeAsState()
    val context = LocalContext.current

    Log.i("Screen", "AlarmScreen")

    Scaffold(
        topBar = {
            TopAppBar(backgroundColor = Color(0xFF131E37)) {
                IconButton(onClick = {
                    navController.popBackStack()
                }) {
                    Icon(Icons.Default.ArrowBack, "Menu", tint = Color.White)
                }
                Text(text = "Geo Alarm", color = Color.White)

            }
        }
    ) {
        alarms?.let {
            if (it.isEmpty()) {

            } else {
                LazyColumn {
                    items(it) { alarm: Alarm ->
                        AlarmCard(
                            alarm,
                            viewModel.database.isAlarmActive(alarm.id).observeAsState(false).value
                        ) { _ -> viewModel.toggleAlarm(alarm, context) }
                    }
                }
            }
        }
    }
}
