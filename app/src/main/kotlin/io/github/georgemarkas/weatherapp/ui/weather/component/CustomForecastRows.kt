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
import androidx.compose.ui.text.font.FontWeight
import io.github.georgemarkas.weatherapp.openmeteo.models.forecast.WeatherDaily
import io.github.georgemarkas.weatherapp.openmeteo.models.forecast.WeatherHourly
import io.github.georgemarkas.weatherapp.settings.models.Units
import io.github.georgemarkas.weatherapp.ui.theme.dimens
import io.github.georgemarkas.weatherapp.util.celsiusToFahrenheit
import io.github.georgemarkas.weatherapp.util.degreesToDirection
import io.github.georgemarkas.weatherapp.util.kphToMph
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.roundToInt

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
        modifier = Modifier.padding(bottom = MaterialTheme.dimens.forecastRowBottomPadding)
    ) {
        items(temperatureMax.size) { index ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement =
                    Arrangement.spacedBy(MaterialTheme.dimens.forecastRowItemVerticalArrangement),
            ) {

                val dayEEE = dayShortFormatter
                    .format(Date(daily.time[index] * 1000))

                Text(
                    text = "$dayEEE",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                val dayDM = dayMonthFormatter
                    .format(Date(daily.time[index] * 1000))
                Text(
                    text = "$dayDM",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "${daily.weatherCode?.get(index)}WC",
                    style = MaterialTheme.typography.bodyLarge
                )

                val tempMax = temperatureMax[index]
                    ?.let { if (unitType == Units.IMPERIAL) celsiusToFahrenheit(it) else it }

                val tempMin = temperatureMin[index]
                    ?.let { if (unitType == Units.IMPERIAL) celsiusToFahrenheit(it) else it }

                Text(
                    text = tempMax?.let {
                        "${it.roundToInt()}$temperatureUnit"
                    } ?: "-",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(MaterialTheme.dimens.spacing4))

                Text(
                    text = "${daily.precipitationProbabilityMean?.get(index)}%",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )


                Text(
                    text = tempMin?.let {
                        "${it.roundToInt()}$temperatureUnit"
                    } ?: "-",
                    style = MaterialTheme.typography.bodyMedium
                )

            }
        }
    }

}

@Composable
fun DailyForecastRowWind(
    daily: WeatherDaily,
    unitType: Units
) {
    val listState = rememberLazyListState()

    val windUnit = unitType.windSpeed
    val windMax = daily.windSpeed ?: return
    val windDirectionDominant = daily.windDirection ?: return

    val locale = LocalLocale.current.platformLocale
    val dayShortFormatter = remember(locale) {
        SimpleDateFormat("EEE", locale)
    }
    val dayMonthFormatter = remember(locale) {
        SimpleDateFormat("dd-MM", locale)
    }

    ForecastRow(
        listState = listState,
        modifier = Modifier.padding(bottom = MaterialTheme.dimens.forecastRowBottomPadding)
    ) {
        items(windMax.size) { index ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement
                    .spacedBy(MaterialTheme.dimens.forecastRowItemVerticalArrangement),
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
                    text = windDirectionDominant[index]?.let { degreesToDirection(it) } ?: "—",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(Modifier.height(MaterialTheme.dimens.spacing4))

                val speedMax = windMax[index]
                    ?.let { if (unitType == Units.IMPERIAL) kphToMph(it) else it }

                Text(
                    text = speedMax?.let {
                        "${kphToMph(it).roundToInt()}${windUnit}"
                    } ?: "-",
                    style = MaterialTheme.typography.bodyMedium
                )

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
        SimpleDateFormat("H:mm", locale)
    }

    ForecastRow(
        listState = listState,
        modifier = Modifier.padding(bottom = MaterialTheme.dimens.forecastRowBottomPadding)
    ) {
        items(temperatures.size) { index ->

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement
                    .spacedBy(MaterialTheme.dimens.forecastRowItemVerticalArrangement),
            ) {
                val time = hoursMinutesFormatter
                    .format(Date(hourly.time[index] * 1000))

                val temperature = temperatures[index]
                    ?.let { if (unitType == Units.IMPERIAL) celsiusToFahrenheit(it) else it }

                Text(
                    text = time,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${hourly.weatherCode?.get(index)}WC",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(Modifier.height(MaterialTheme.dimens.spacing4))

                Text(
                    text = "${hourly.precipitationProbability?.get(index)}%",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = temperature?.let {
                        "${it.roundToInt()}${temperatureUnit}"
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
    val windDirection = hourly.windDirection ?: return

    val locale = LocalLocale.current.platformLocale
    val hoursMinutesFormatter = remember(locale) {
        SimpleDateFormat("H:mm", locale)
    }

    val listState = rememberLazyListState()
    ForecastRow(
        listState = listState,
        modifier = Modifier.padding(bottom = MaterialTheme.dimens.forecastRowBottomPadding)
    ) {
        items(windSpeed.size) { index ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement
                    .spacedBy(MaterialTheme.dimens.forecastRowItemVerticalArrangement),
            ) {
                val time = hoursMinutesFormatter
                    .format(Date(hourly.time[index] * 1000))

                Text(
                    text = time,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = windDirection[index]?.let { degreesToDirection(it) } ?: "—",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(Modifier.height(MaterialTheme.dimens.spacing4))

                val speedMax = windSpeed[index]
                    ?.let { if (unitType == Units.IMPERIAL) kphToMph(it) else it }

                Text(
                    speedMax?.let { "${it.roundToInt()}${unitType.windSpeed}" } ?: "—",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}