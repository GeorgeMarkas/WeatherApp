package io.github.georgemarkas.weatherapp.openmeteo.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherCurrent(
    val time: Long,
    @SerialName("temperature_2m") val temperature: Double?,
    @SerialName("apparent_temperature") val apparentTemperature: Double?,
    @SerialName("relative_humidity_2m") val relativeHumidity: Int?,
    @SerialName("wind_speed_10m") val windSpeed: Double?,
    @SerialName("cloud_cover") val cloudCover: Int?,
    @SerialName("weather_code") val weatherCode: Int?,
    @SerialName("is_day") val isDay: Int?, // OpenMeteo returns integer, not boolean
)