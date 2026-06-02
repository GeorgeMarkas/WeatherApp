package io.github.georgemarkas.weatherapp.openmeteo.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherCurrent(
    @SerialName("temperature_2m") val temperature: Double?,
    @SerialName("weather_code") val weatherCode: Int?,
    @SerialName("relative_humidity_2m") val relativeHumidity: Int?,
    @SerialName("wind_speed_10m") val windSpeed: Double?,
    @SerialName("cloud_cover") val cloudCover: Int?,
    val time: Long,
)