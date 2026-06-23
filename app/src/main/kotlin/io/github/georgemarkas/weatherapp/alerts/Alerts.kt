package io.github.georgemarkas.weatherapp.alerts

import io.github.georgemarkas.weatherapp.alerts.models.Alert
import io.github.georgemarkas.weatherapp.alerts.models.AlertSeverity
import io.github.georgemarkas.weatherapp.alerts.models.AlertType
import io.github.georgemarkas.weatherapp.openmeteo.models.forecast.WeatherCurrent
import io.github.georgemarkas.weatherapp.openmeteo.models.forecast.WeatherDaily
import io.github.georgemarkas.weatherapp.openmeteo.models.forecast.WeatherHourly
import io.github.georgemarkas.weatherapp.openmeteo.models.forecast.WeatherResponse
import kotlin.time.Clock

object Alerts {

    // These are all in metric since that's what OpenMeteo defaults to
    private const val HEAT_WARNING = 35.0
    private const val HEAT_SEVERE = 40.0

    private const val COLD_WARNING = -10.0
    private const val COLD_SEVERE = -20.0

    private const val WIND_WARNING = 60.0
    private const val WIND_SEVERE = 90.0

    private const val FORECAST_WINDOW_SECONDS = 24 * 3600L

    /**
     * Checks the given weather data for potential hazardous weather and
     * returns a sorted list of the most severe alerts of each type.
     */
    fun checkForAlerts(response: WeatherResponse): List<Alert> {
        val alerts = buildList {
            response.current?.let { addAll(currentAlerts(it)) }
            response.hourly?.let { addAll(hourlyAlerts(it)) }
            response.daily?.let { addAll(dailyAlerts(it)) }
        }

        return sieve(alerts)
    }

    /**
     * Creates a list of alerts from the current forecast data.
     */
    private fun currentAlerts(current: WeatherCurrent): List<Alert> = buildList {
        weatherCodeToAlert(current.weatherCode)?.let { (type, severity) ->
            add(Alert(type, severity, current.time))
        }

        current.windSpeed?.let { windSpeed ->
            windSeverity(windSpeed)?.let {
                add(Alert(AlertType.HIGH_WIND, it, current.time, windSpeed))
            }
        }

        current.temperature?.let { temperature ->
            temperatureAlerts(temperature, current.time, this)
        }
    }

    /**
     * Creates a list of alerts from the hourly forecast data.
     */
    private fun hourlyAlerts(hourly: WeatherHourly): List<Alert> = buildList {
        val now = Clock.System.now().epochSeconds
        hourly.time.forEachIndexed { i, time ->
            if (time !in now..now + FORECAST_WINDOW_SECONDS) return@forEachIndexed

            hourly.weatherCode?.getOrNull(i)?.let { weatherCode ->
                weatherCodeToAlert(weatherCode)?.let { (type, severity) ->
                    add(Alert(type, severity, time))
                }
            }

            hourly.windSpeed?.getOrNull(i)?.let { windSpeed ->
                windSeverity(windSpeed)?.let {
                    add(Alert(AlertType.HIGH_WIND, it, time, windSpeed))
                }
            }

            hourly.temperature?.getOrNull(i)?.let { temperature ->
                temperatureAlerts(temperature, time, this)
            }
        }
    }

    /**
     * Creates a list of alerts from the daily forecast data.
     */
    private fun dailyAlerts(daily: WeatherDaily): List<Alert> = buildList {
        daily.temperatureMax?.forEachIndexed { i, temperature ->
            if (temperature != null)
                temperatureAlerts(temperature, daily.time.getOrNull(i), this)
        }

        daily.temperatureMin?.forEachIndexed { i, temperature ->
            if (temperature != null)
                temperatureAlerts(temperature, daily.time.getOrNull(i), this)
        }
    }

    /**
     * Based on the given temperature, adds extreme cold or heat alerts to a list.
     */
    private fun temperatureAlerts(temperature: Double, time: Long?, sink: MutableList<Alert>) {
        heatSeverity(temperature)?.let {
            sink.add(Alert(AlertType.EXTREME_HEAT, it, time, temperature))
        }

        coldSeverity(temperature)?.let {
            sink.add(Alert(AlertType.EXTREME_COLD, it, time, temperature))
        }
    }

    private fun heatSeverity(temperature: Double): AlertSeverity? = when {
        temperature >= HEAT_SEVERE -> AlertSeverity.SEVERE
        temperature >= HEAT_WARNING -> AlertSeverity.WARNING
        else -> null
    }

    private fun coldSeverity(temperature: Double): AlertSeverity? = when {
        temperature <= COLD_SEVERE -> AlertSeverity.SEVERE
        temperature <= COLD_WARNING -> AlertSeverity.WARNING
        else -> null
    }

    private fun windSeverity(windSpeed: Double): AlertSeverity? = when {
        windSpeed >= WIND_SEVERE -> AlertSeverity.SEVERE
        windSpeed >= WIND_WARNING -> AlertSeverity.WARNING
        else -> null
    }

    /**
     * Returns alert type and severity for the given weather code or null if
     * non-applicable.
     */
    private fun weatherCodeToAlert(code: Int?): Pair<AlertType, AlertSeverity>? = when (code) {
        95 -> AlertType.THUNDERSTORM to AlertSeverity.WARNING
        96, 99 -> AlertType.THUNDERSTORM to AlertSeverity.SEVERE
        65 -> AlertType.HEAVY_RAIN to AlertSeverity.WARNING
        82 -> AlertType.HEAVY_RAIN to AlertSeverity.SEVERE
        75 -> AlertType.HEAVY_SNOW to AlertSeverity.WARNING
        86 -> AlertType.HEAVY_SNOW to AlertSeverity.SEVERE
        56, 66 -> AlertType.FREEZING_RAIN to AlertSeverity.WARNING
        57, 67 -> AlertType.FREEZING_RAIN to AlertSeverity.SEVERE
        else -> null
    }

    /**
     * Keeps one alert per type. Sorts by worst severity, then by earliest occurence.
     */
    private fun sieve(alerts: List<Alert>): List<Alert> {
        // Initially perform the sorting on each alert type
        val ranking = compareByDescending<Alert> { it.severity.ordinal }
            .thenBy { it.startTime ?: Long.MAX_VALUE }

        // Then select one from each type
        return alerts.groupBy { it.type }
            .map { (_, group) -> group.minWith(ranking) }
            .sortedWith(ranking)
    }
}