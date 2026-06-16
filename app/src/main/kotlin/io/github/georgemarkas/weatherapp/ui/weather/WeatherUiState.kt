package io.github.georgemarkas.weatherapp.ui.weather

import io.github.georgemarkas.weatherapp.openmeteo.models.WeatherResponse

data class WeatherUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val weather: WeatherResponse? = null,
    val error: String? = null
)