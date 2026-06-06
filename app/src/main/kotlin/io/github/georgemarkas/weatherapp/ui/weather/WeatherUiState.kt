package io.github.georgemarkas.weatherapp.ui.weather

import io.github.georgemarkas.weatherapp.location.LocationWrapper
import io.github.georgemarkas.weatherapp.openmeteo.models.WeatherResponse

data class WeatherUiState(
    val location: LocationWrapper? = null,
    val weather: WeatherResponse? = null,
    val isLoading: Boolean = false,
)