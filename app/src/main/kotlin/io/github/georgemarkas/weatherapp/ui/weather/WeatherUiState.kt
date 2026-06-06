package io.github.georgemarkas.weatherapp.ui.weather

import io.github.georgemarkas.weatherapp.openmeteo.models.WeatherResponse

data class WeatherUiState(
    val weather: WeatherResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)