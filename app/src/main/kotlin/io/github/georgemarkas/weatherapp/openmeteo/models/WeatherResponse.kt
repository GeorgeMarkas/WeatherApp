package io.github.georgemarkas.weatherapp.openmeteo.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    @SerialName("current") val current: WeatherCurrent? = null,
    val hourly: WeatherHourly? = null,
    val daily: WeatherDaily? = null,
    val error: Boolean? = null,
    val reason: String? = null,
)