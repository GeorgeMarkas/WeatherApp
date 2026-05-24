package com.example.weatherapp

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.example.weatherapp.openmeteo.WeatherService
import com.example.weatherapp.ui.theme.WeatherappTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class MainActivity : ComponentActivity() {

    private val cts = CancellationTokenSource()
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) getLocation()
    }

    private var latitude by mutableStateOf<Double?>(null)
    private var longitude by mutableStateOf<Double?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationPermissionRequest.launch(Manifest.permission.ACCESS_COARSE_LOCATION)

        setContent {
            val lat = latitude
            val lon = longitude
            if (lat != null && lon != null) {
                WeatherDemo(latitude = lat, longitude = lon)
            } else {
                Text("Waiting for location...")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cts.cancel()
    }

    private fun getLocation() {
        val client = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                } else {
                    client.lastLocation.addOnSuccessListener { last: Location? ->
                        last?.let {
                            latitude = it.latitude
                            longitude = it.longitude
                        }
                    }
                }
            }
    }
}

@Composable
fun WeatherDemo(latitude: Double, longitude: Double) {
    var weatherText by remember { mutableStateOf("Loading...") }

    LaunchedEffect(latitude, longitude) {
        WeatherService.forecastApi.getWeather(
            latitude = latitude,
            longitude = longitude
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> weatherText = result.toString() },
                { error -> weatherText = error.message ?: "Unknown error" }
            )
    }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        WeatherappTheme {
            Text(text = weatherText)
        }
    }
}