package io.github.georgemarkas.weatherapp.ui.weather.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.unit.dp
import io.github.georgemarkas.weatherapp.openmeteo.models.forecast.WeatherDaily
import io.github.georgemarkas.weatherapp.openmeteo.models.forecast.WeatherHourly
import io.github.georgemarkas.weatherapp.settings.models.Units
import io.github.georgemarkas.weatherapp.util.celsiusToFahrenheit
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun DailyForecastRowConditions(
    daily: WeatherDaily,
    unitType: Units
) {
    val listState = rememberLazyListState()

    // TODO: DECIDE WHAT TO DISPLAY ON NO RESULTS.
    val temperatureMax = daily.temperatureMax ?: return // or show an empty/error state
    val temperatureMin = daily.temperatureMin ?: return
    val temperatureUnit = unitType.temperature

    val locale = LocalLocale.current.platformLocale
    val dayShortFormatter = remember(locale) {
        SimpleDateFormat("EEE", locale)
    }
    val dayMonthFormatter = remember(locale) {
        SimpleDateFormat("dd-MM", locale)
    }

    ForecastRow(
        listState = listState,
        // TODO: REPLACE MANUAL DP
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        items(daily.temperatureMax.size) { index ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                // TODO: REPLACE MANUAL DP
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {

                val dayEEE = dayShortFormatter
                    .format(Date(daily.time[index] * 1000))

                Text(
                    text = "$dayEEE",
                    style = MaterialTheme.typography.bodyLarge,
                )

                val dayDM = dayMonthFormatter
                    .format(Date(daily.time[index] * 1000))
                Text(
                    text = "$dayDM",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "${daily.cloudCoverMean?.get(index)}CC",
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = temperatureMax[index]?.let {
                        "%.1f${temperatureUnit}"
                            .format(it)
                    } ?: "-",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "${daily.relativeHumidityMean?.get(index)}RH%",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = temperatureMin[index]?.let {
                        "%.1f${temperatureUnit}"
                            .format(it)
                    } ?: "-",
                    style = MaterialTheme.typography.bodyMedium
                )

            }
        }
    }

}

// TODO: IMPLEMENT WIND SECTION
@Composable
fun DailyForecastRowWind(
    daily: WeatherDaily,
    unitType: Units
) {
    val listState = rememberLazyListState()

    // TODO: RESOLVE SITUATION WITH WEATHER RESPONSE OBJECTS NOT SERIALIZING!
    val temperatureMax = daily.temperatureMax ?: return // or show an empty/error state
    val temperatureMin = daily.temperatureMin ?: return
    val temperatureUnit = unitType.temperature

    val locale = LocalLocale.current.platformLocale
    val dayShortFormatter = remember(locale) {
        SimpleDateFormat("EEE", locale)
    }
    val dayMonthFormatter = remember(locale) {
        SimpleDateFormat("dd-MM", locale)
    }

    ForecastRow(
        listState = listState,
        // TODO: REPLACE MANUAL DP
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        items(daily.temperatureMax.size) { index ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                // TODO: REPLACE MANUAL DP
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {

                val dayEEE = dayShortFormatter
                    .format(Date(daily.time[index] * 1000))

                Text(
                    text = "$dayEEE",
                    style = MaterialTheme.typography.bodyLarge,
                )

                val dayDM = dayMonthFormatter
                    .format(Date(daily.time[index] * 1000))
                Text(
                    text = "$dayDM",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )

//                Text(
//                    text = "${daily.cloudCoverMean?.get(index)}CC",
//                    style = MaterialTheme.typography.bodyLarge
//                )

//                Text(
//                    text = temperatureMax[index]?.let {
//                        "%.1f${temperatureUnit}"
//                            .format(it)
//                    } ?: "-",
//                    style = MaterialTheme.typography.bodyMedium
//                )

//                Text(
//                    text = "${daily.relativeHumidityMean?.get(index)}RH%",
//                    color = Color.Gray,
//                    style = MaterialTheme.typography.bodySmall
//                )

//                Text(
//                    text = temperatureMin[index]?.let {
//                        "%.1f${temperatureUnit}"
//                            .format(it)
//                    } ?: "-",
//                    style = MaterialTheme.typography.bodyMedium
//                )

            }
        }
    }

}

@Composable
fun HourlyForecastRowConditions(
    hourly: WeatherHourly,
    unitType: Units
) {
    val listState = rememberLazyListState()

    val temperatures = hourly.temperature ?: return // or show an empty/error state
    val temperatureUnit = unitType.temperature

    val locale = LocalLocale.current.platformLocale
    val hoursMinutesFormatter = remember(locale) {
        SimpleDateFormat("HH:mm", locale)
    }

    ForecastRow(
        listState = listState,
        // TODO: REPLACE MANUAL DP
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        items(temperatures.size) { index ->

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                // TODO: REPLACE MANUAL DP
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val time = hoursMinutesFormatter
                    .format(Date(hourly.time[index] * 1000))

                val temperature = temperatures[index]
                    ?.let { if (unitType == Units.IMPERIAL) celsiusToFahrenheit(it) else it }

                Text(
                    text = time,
                    style = MaterialTheme.typography.bodyLarge
                )

                // TODO: REPLACE WITH THEME DIMENSIONS
                Spacer(Modifier.height(16.dp))

                Text(
                    text = temperature?.let {
                        "%.1f${temperatureUnit}"
                            .format(it)
                    } ?: "—",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun HourlyForecastRowWind(
    hourly: WeatherHourly,
    unitType: Units
) {
    val windSpeed = hourly.windSpeed ?: return
//    val windDirection = hourly.windDirection ?: return

    val locale = LocalLocale.current.platformLocale
    val hoursMinutesFormatter = remember(locale) {
        SimpleDateFormat("HH:mm", locale)
    }

    val listState = rememberLazyListState()
    ForecastRow(
        listState = listState,
        // TODO: REPLACE MANUAL DP
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        items(windSpeed.size) { index ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                // TODO: REPLACE MANUAL DP
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val time = hoursMinutesFormatter
                    .format(Date(hourly.time[index] * 1000))

                Text(
                    text = time,
                    style = MaterialTheme.typography.bodyLarge
                )
//                Text(
//                    windDirection[index]?.let { "${it}DIC"} ?: "—",
//                    color = Color.Gray,
//                    style = MaterialTheme.typography.bodySmall
//                )

                // TODO: REPLACE MANUAL DP
                Spacer(Modifier.height(16.dp))

                Text(
                    windSpeed[index]?.let { "${it}B" } ?: "—",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}