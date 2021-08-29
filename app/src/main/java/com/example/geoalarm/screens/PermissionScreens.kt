package com.example.geoalarm.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.platform.LocalContext
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

@RequiresApi(Build.VERSION_CODES.M)
@ExperimentalPermissionsApi
@Composable
fun BackgroundLocationAccessRationale(bgLocationPermissionState: PermissionState) {

    val resources = LocalContext.current.resources
    val theme = LocalContext.current.theme

    Column(
        modifier = Modifier
            .background(Color(resources.getColor(R.color.dark_blue, theme)))
            .padding(20.dp, 20.dp)
    ) {
        Text(
            resources.getString(R.string.bg_location_prompt),
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
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(resources.getColor(R.color.light_green, theme))),
            ) {
                Text(resources.getString(R.string.ok))
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.M)
@Composable
fun BackgroundLocationAccessDenied(context: Context){
    val resources = LocalContext.current.resources
    val theme = LocalContext.current.theme

    Column (
        modifier = Modifier
            .background(Color(resources.getColor(R.color.dark_blue, theme)))
            .padding(20.dp, 20.dp)
    ){
        Text(
            resources.getString(R.string.bg_location_denied),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp, 20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = {

                //Navigate to app permissions settings
                val intent =
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", context.packageName, null)
                intent.data = uri
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                ContextCompat.startActivity(context, intent, null)

            },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(resources.getColor(R.color.light_green, theme)))
            ) {
                Text(resources.getString(R.string.open_settings))
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.M)
@ExperimentalPermissionsApi
@Composable
fun LocationAccessRationale(locationPermissionState: PermissionState){

    val resources = LocalContext.current.resources
    val theme = LocalContext.current.theme


    Column(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
    ) {

        Image(
            painter = painterResource(R.drawable.map_logo),
            contentDescription = resources.getString(R.string.map_icon),
            alignment = Alignment.Center,
            modifier = Modifier.padding(100.dp, 80.dp, 100.dp, 60.dp)
        )

        Text(
            resources.getString(R.string.location_prompt),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp, 20.dp, 15.dp, 60.dp)
        )

        Text(
            resources.getString(R.string.location_rationale),
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
                    backgroundColor = Color(resources.getColor(R.color.light_green, theme)),
                    contentColor = Color.White
                ),
            ) {
                Text(text = resources.getString(R.string.ok), fontSize = 30.sp)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.M)
@Composable
fun LocationAccessDenied(context: Context){

    val resources = LocalContext.current.resources
    val theme = LocalContext.current.theme

    Column(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.ic_close),
            contentDescription = resources.getString(R.string.red_cross_icon),
            alignment = Alignment.Center,
            modifier = Modifier.padding(100.dp, 80.dp, 100.dp, 60.dp)
        )
        Text(
            resources.getString(R.string.location_denied),
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
                backgroundColor = Color(resources.getColor(R.color.light_green, theme)),
                contentColor = Color.White
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = resources.getString(R.string.open_settings), fontSize = 20.sp)
        }
    }
}