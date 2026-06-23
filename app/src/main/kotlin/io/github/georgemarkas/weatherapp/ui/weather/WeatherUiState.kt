package io.github.georgemarkas.weatherapp.ui.weather

import io.github.georgemarkas.weatherapp.settings.Settings
import io.github.georgemarkas.weatherapp.openmeteo.models.forecast.WeatherResponse

data class WeatherUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val weather: WeatherResponse? = null,
    val locality: String? = null,
    val settings: Settings = Settings(),
    val error: String? = null
)