package com.example.geoalarm.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.geoalarm.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState

@ExperimentalPermissionsApi
@Composable
fun BackgroundLocationAccessRationale(bgLocationPermissionState: PermissionState) {
    Column(
        modifier = Modifier
            .background(Color(0xFF131E37))
            .padding(20.dp, 20.dp)
    ) {
        Text(
            "To set up GeoAlarms, we need access to your location at all times. Please grant the permission.",
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp, 20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { bgLocationPermissionState.launchPermissionRequest() },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF3EB38A)),
            ) {
                Text("Ok!")
            }
        }
    }
}

@Composable
fun BackgroundLocationAccessDenied(context: Context){
    Column {
        Text(
            "Location permission denied. Please, grant us access on the Settings screen."
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {

            //Navigate to app permissions settings
            val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ContextCompat.startActivity(context, intent, null)

        }) {
            Text("Open Settings")
        }
    }
}

@ExperimentalPermissionsApi
@Composable
fun LocationAccessRationale(locationPermissionState: PermissionState){
    Column(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
    ) {

        Image(
            painter = painterResource(R.drawable.map_logo),
            contentDescription = "Map Logo",
            alignment = Alignment.Center,
            modifier = Modifier.padding(100.dp, 80.dp, 100.dp, 60.dp)
        )

        Text(
            "Please give us access to your GPS location",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp, 20.dp, 15.dp, 60.dp)
        )

        Text(
            "This will us to notify you when you enter a area with a GeoAlarm. This is needed for the core functionality of the app",
            textAlign = TextAlign.Center,
            fontStyle = FontStyle.Italic,
            fontSize = 15.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp, 10.dp, 15.dp, 70.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = { locationPermissionState.launchPermissionRequest() },
                contentPadding = PaddingValues(40.dp, 30.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF3EB38A),
                    contentColor = Color.White
                ),
            ) {
                Text(text = "Ok!", fontSize = 30.sp)
            }
        }
    }
}

@Composable
fun LocationAccessDenied(context: Context){
    Column(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.map_logo),
            contentDescription = "Red cross",
            alignment = Alignment.Center,
            modifier = Modifier.padding(100.dp, 80.dp, 100.dp, 60.dp)
        )
        Text(
            "Location permission denied too many times. Please, grant us access on the Settings screen.",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp, 20.dp, 15.dp, 90.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))


        OutlinedButton(
            onClick = {
                //Navigate to app permissions settings
                val intent =
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", context.packageName, null)
                intent.data = uri
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                ContextCompat.startActivity(context, intent, null)

            },
            contentPadding = PaddingValues(30.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF3EB38A),
                contentColor = Color.White
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Open Settings", fontSize = 20.sp)
        }
    }
}