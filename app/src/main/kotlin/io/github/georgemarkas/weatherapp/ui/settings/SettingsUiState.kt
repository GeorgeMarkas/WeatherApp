package io.github.georgemarkas.weatherapp.ui.settings

import io.github.georgemarkas.weatherapp.openmeteo.models.geocoding.GeocodingResult
import io.github.georgemarkas.weatherapp.settings.Settings

data class SettingsUiState(
    val settings: Settings = Settings(),
    val specifiedLocality: String? = null,
    val countryCode: String? = null,
    val admin1: String? = null,
    val searchResults: List<GeocodingResult>? = emptyList(),
    val isSearching: Boolean = false
)