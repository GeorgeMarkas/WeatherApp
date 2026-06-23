package io.github.georgemarkas.weatherapp.openmeteo.models.forecast

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherDaily(
    @SerialName("temperature_2m_max") val temperatureMax: List<Double?>?,
    @SerialName("temperature_2m_min") val temperatureMin: List<Double?>?,
    @SerialName("relative_humidity_2m_mean") val relativeHumidityMean: List<Int?>?,
    @SerialName("cloud_cover_mean") val cloudCoverMean: List<Int?>?,
    val time: List<Long>
)