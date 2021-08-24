package com.example.geoalarm

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.geoalarm.data.Alarm
import com.example.geoalarm.data.AlarmType
import com.example.geoalarm.data.AlarmsScreenViewModel

@ExperimentalMaterialApi
@Composable
fun AlarmCard(
    alarm: Alarm,
    viewModel: AlarmsScreenViewModel,
    isActive: Boolean,
    toggleAlarm: (Boolean) -> Unit
) {

    Log.i("Screen", "AlarmCard")

    Card(modifier = Modifier.fillMaxWidth(), onClick = { viewModel.changeSelectedAlarm(alarm) }) {
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

@Composable
fun AlarmEditMenu(viewModel: AlarmsScreenViewModel) {

    val alarmName by viewModel.alarmName.observeAsState()
    val alarmType by viewModel.alarmType.observeAsState()
    val sliderPosition by viewModel.sliderPosition.observeAsState()
    val areaRadius by viewModel.areaRadius.observeAsState()

    Column(
        modifier = Modifier
            .background(Color(0xFF131E37))
            .wrapContentHeight()
    ) {

        OutlinedTextField(
            value = alarmName ?: "",
            onValueChange = { viewModel.onAlarmNameChange(it) },
            label = { Text("Alarm Name") },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color.White,
                cursorColor = Color.White,
                unfocusedBorderColor = Color.LightGray,
                unfocusedLabelColor = Color.DarkGray,
                focusedBorderColor = Color.LightGray,
                focusedLabelColor = Color.LightGray
            ),
            modifier = Modifier
                .padding(20.dp, 10.dp)
                .fillMaxWidth()
        )

        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Radius : ")
                }
                append("$areaRadius m")

            },
            color = Color.White,
            modifier = Modifier.padding(23.dp, 10.dp, 10.dp, 0.dp)
        )

        Slider(
            value = sliderPosition ?: 0F,
            onValueChange = { viewModel.onChangeSlider(it) },
            modifier = Modifier.padding(15.dp, 0.dp, 15.dp, 5.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xffbd93f9),
                activeTrackColor = Color(
                    0xffc296ff
                ),
                inactiveTrackColor = Color(0xFF8b6ab8)
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 0.dp, 0.dp, 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(
                onClick = { viewModel.onChangeAlarmType(AlarmType.ON_ENTRY) },
                modifier = Modifier
                    .background(
                        if (alarmType == AlarmType.ON_ENTRY) Color(0xFF3EB38A) else Color(
                            0xFFFFFFFF
                        )
                    )
                    .fillMaxWidth(0.35F)
            ) {
                Text(
                    "Entry",
                    color = if (alarmType == AlarmType.ON_ENTRY) Color(0xFFFFFFFF) else Color(
                        0xFF000000
                    )
                )

            }
            TextButton(
                onClick = { viewModel.onChangeAlarmType(AlarmType.ON_EXIT) },
                modifier = Modifier
                    .background(
                        if (alarmType == AlarmType.ON_EXIT) Color(0xFF3EB38A) else Color(
                            0xFFFFFFFF
                        )
                    )
                    .fillMaxWidth(0.5F)
            ) {
                Text(
                    "Exit",
                    color = if (alarmType == AlarmType.ON_EXIT) Color(0xFFFFFFFF) else Color(
                        0xFF000000
                    )
                )

            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            TextButton(onClick = { viewModel.menuClose() }) {
                Text(
                    "Close",
                    color = Color.LightGray
                )
            }

            TextButton(onClick = { viewModel.deleteAlarm() }) {
                Text("Delete", color = Color(0xFFFF5F6E))
            }

            TextButton(onClick = { viewModel.modifyAlarm() }) {
                Text("Save", color = Color(0xffc296ff))
            }

        }
    }
}

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun AlarmScreen(navController: NavHostController, viewModel: AlarmsScreenViewModel) {

    val alarms by viewModel.alarms.observeAsState()
    val selectedAlarm by viewModel.selectedAlarm.observeAsState()

    val context = LocalContext.current

    Log.i("Screen", "AlarmScreen")

    Scaffold(
        topBar = {
            TopAppBar(backgroundColor = Color(0xFF131E37)) {
                IconButton(onClick = {
                    viewModel.menuClose()
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_ghost),
                        contentDescription = "Ghost",
                        alignment = Alignment.Center,
                        modifier = Modifier.padding(100.dp, 80.dp, 100.dp, 60.dp)
                    )
                    Text(
                        "Ouhh..it's empty here.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        modifier = Modifier.padding(10.dp, 30.dp, 10.dp, 10.dp)
                    )
                    Text(
                        "Go back and add some GeoAlarms :)",
                        color = Color.DarkGray,
                        modifier = Modifier.padding(10.dp, 30.dp, 10.dp, 20.dp)
                    )
                }

            } else {

                Box(modifier = Modifier.fillMaxSize()) {

                    Box {
                        LazyColumn {
                            items(it) { alarm: Alarm ->
                                AlarmCard(
                                    alarm,
                                    viewModel,
                                    viewModel.database.isAlarmActive(alarm.id)
                                        .observeAsState(false).value
                                ) { _ -> viewModel.toggleAlarm(alarm, context) }
                            }
                        }
                    }

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .align(Alignment.BottomCenter)
                    ) {

                        AnimatedVisibility(
                            visible = (selectedAlarm != null),
                            enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
                            exit = fadeOut(animationSpec = tween(durationMillis = 1000))
                        ) {
                            AlarmEditMenu(viewModel)
                        }
                    }
                }

            }
        }
    }
}
