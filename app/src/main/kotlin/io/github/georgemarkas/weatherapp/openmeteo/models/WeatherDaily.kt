package io.github.georgemarkas.weatherapp.openmeteo.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherDaily(
    val time: List<Long>,
    @SerialName("temperature_2m_max") val temperatureMax: List<Double?>?,
    @SerialName("temperature_2m_min") val temperatureMin: List<Double?>?,
    @SerialName("relative_humidity_2m_mean") val relativeHumidityMean: List<Int?>?,
    @SerialName("cloud_cover_mean") val cloudCoverMean: List<Int?>?,
)