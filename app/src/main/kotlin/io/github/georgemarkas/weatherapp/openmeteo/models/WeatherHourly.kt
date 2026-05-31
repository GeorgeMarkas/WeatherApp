package io.github.georgemarkas.weatherapp.openmeteo.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherHourly(
    val time: List<Long>,
    @SerialName("temperature_2m") val temperature: List<Double?>?,
    @SerialName("apparent_temperature") val apparentTemperature: List<Double?>?,
    @SerialName("relative_humidity_2m") val relativeHumidity: List<Int?>?,
    @SerialName("wind_speed_10m") val windSpeed: List<Double?>?,
    @SerialName("cloud_cover") val cloudCover: List<Int?>?,
    @SerialName("weather_code") val weatherCode: List<Int?>?,
    @SerialName("is_day") val isDay: List<Int>?, // OpenMeteo returns integer, not boolean
)