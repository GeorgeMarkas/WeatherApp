package com.example.weatherapp

import androidx.activity.ComponentActivity
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
import com.example.weatherapp.openmeteo.WeatherService
import com.example.weatherapp.ui.theme.WeatherappTheme
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class MainActivity : ComponentActivity() {
    // ...
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