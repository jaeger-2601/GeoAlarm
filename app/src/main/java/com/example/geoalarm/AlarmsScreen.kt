package com.example.geoalarm

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.geoalarm.data.Alarm
import com.example.geoalarm.data.AlarmType
import com.example.geoalarm.data.AlarmsScreenViewModel

@Composable
fun AlarmCard(alarm: Alarm, isActive: Boolean, toggleAlarm: (Boolean) -> Unit){

    Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(10.dp, 10.dp, 20.dp, 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {

                    Row(modifier = Modifier.padding(15.dp, 10.dp, 10.dp, 5.dp)) {

                        Text(
                            text = alarm.name,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                        )

                    }

                    Row(modifier = Modifier.padding(10.dp, 0.dp, 10.dp, 0.dp)) {

                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Location Icon",
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
                Switch(
                    checked = isActive,
                    onCheckedChange = toggleAlarm,
                )
            }
        }
}



@Composable
fun AlarmScreen(navController: NavHostController, viewModel: AlarmsScreenViewModel){

    val alarms by viewModel.alarms.observeAsState()

    Scaffold(
        topBar = {
            TopAppBar(backgroundColor = Color(51, 65, 145)) {
                IconButton(onClick = {
                    navController.popBackStack()
                }) {
                    Icon(Icons.Default.ArrowBack, "Menu", tint = Color.White)
                }
                Text(text = "Geo Alarm")

            }
        }
    ) {
        Column {
            alarms?.let {

                for (i in 0..it.lastIndex) {

                    AlarmCard(
                        it[i],
                        viewModel.database.isAlarmActive(it[i].id).observeAsState(false).value
                    ) { _ -> viewModel.toggleAlarm(it[i]) }
                }

            }
        }
    }

}