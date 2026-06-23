package io.github.georgemarkas.weatherapp.location

data class LocationWrapper(
    val latitude: Double,
    val longitude: Double,
    val locality: String?
)