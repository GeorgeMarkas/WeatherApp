package io.github.georgemarkas.weatherapp.ui.settings.data

import io.github.georgemarkas.weatherapp.settings.Settings

data class SettingsUiState(
    val specifiedLocality: String? = null,
    val specifiedCountryCode: String? = null,
    val specifiedAdmin1: String? = null,
    val settings: Settings = Settings(),
    val searchResults: List<GeocodedLocation>? = null,
    val isSearching: Boolean = false
)