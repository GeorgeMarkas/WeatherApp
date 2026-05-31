package com.example.weatherapp.ui.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.unit.dp
import com.example.weatherapp.data.CachedWeather
import com.example.weatherapp.openmeteo.weatherCodeToDescription
import java.text.SimpleDateFormat
import java.util.Date

/* Basic Temp, high/low and location */

@Composable
fun HeaderComposable(
    location: String,
    temperature: Double,
    temperatureMax: Double,
    temperatureMin: Double,
    condition: String,
    updatedAt: String,
    dlat: Double,
    dlong: Double,
) {

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = location,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = condition,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "${temperature}°C",
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "High ${temperatureMax}°C • Low ${temperatureMin}°C",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Latitude: %.4f • Longitude: %.4f".format(
                dlat,
                dlong
            ),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Last updated: $updatedAt",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/*
* TODO: Replace colors with standardized MaterialTheme coloring, make responsive to dark/light theme
* TODO: Replace magic number spacing with standardized spacing through resources/strings.xml
*/


/* Forecast data goes here in two Boxes. Consider splitting into functions. */
@Composable
fun BodyComposable(
    hourlyTime: List<String>,
    hourlyTemperature: List<Double>,
    dailyTime: List<String>,
    dailyTemperature: List<Double>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        val bgBox = MaterialTheme.colorScheme.primary

        // Hourly
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.15f)
                .clip(RoundedCornerShape(9.dp))
                .background(bgBox),
            contentAlignment = Alignment.Center
        ) {
            LazyRow(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(15.dp, 15.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val zipped = hourlyTemperature.zip(hourlyTime)
                items(zipped) { (temp, time) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = time)
                        Text(text = "$temp")
                    }
                }
            }
        }

        // Daily
        Box(
            modifier = Modifier
                .fillMaxHeight(0.6f)
                .fillMaxWidth(0.8f)
                .clip( RoundedCornerShape(9.dp))
                .background(bgBox)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn (
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(15.dp, 15.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                val zipped = dailyTemperature.zip(dailyTime)
                items(zipped) { (temp, time) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = time
                        )

                        Text(
                            text = "$temp"
                        )
                    }
                }
            }
        }

    }
}


/* Parent Composable. */
@Composable
fun HomeComposable(
    weatherState: CachedWeather
) {
    val condition = weatherCodeToDescription(weatherState.weatherCode)
    val updatedAt = SimpleDateFormat("HH:mm", LocalLocale.current.platformLocale)
        .format(Date(weatherState.lastUpdatedMs))

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeaderComposable(
                "Ophelia VII",
                weatherState.temperature,
                weatherState.temperatureMax,
                weatherState.temperatureMin,
                condition,
                updatedAt,
                weatherState.latitude,
                weatherState.longitude
            )
            Spacer(Modifier.height(50.dp))

            BodyComposable(
                weatherState.hourlyTime,
                weatherState.hourlyTemperature,
                weatherState.dailyTime,
                weatherState.dailyMaxTemperature
            )
        }
    }

}