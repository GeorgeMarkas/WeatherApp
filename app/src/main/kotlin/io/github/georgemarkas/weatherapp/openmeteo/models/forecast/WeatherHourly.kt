package io.github.georgemarkas.weatherapp.openmeteo.models.forecast

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherHourly(
    @SerialName("temperature_2m") val temperature: List<Double?>?,
    @SerialName("weather_code") val weatherCode: List<Int?>?,
    @SerialName("relative_humidity_2m") val relativeHumidity: List<Int?>?,
    @SerialName("wind_speed_10m") val windSpeed: List<Double?>?,
//    @SerialName("wind_direction_10m") val windDirection: List<Double?>?,
    @SerialName("cloud_cover") val cloudCover: List<Int?>?,
    @SerialName("precipitation_probability") val precipitationProbability: List<Int?>?,
    @SerialName("is_day") val isDay: List<Int>?, // OpenMeteo returns integer, not boolean
    val time: List<Long>
)