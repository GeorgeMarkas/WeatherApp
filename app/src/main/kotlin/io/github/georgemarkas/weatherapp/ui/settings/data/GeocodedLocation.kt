package io.github.georgemarkas.weatherapp.ui.settings.data

data class GeocodedLocation (
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String,
    val countryCode: String,
    val admin1: String,
    val admin3: String
)