package io.github.georgemarkas.weatherapp.openmeteo.models.geocoding

import kotlinx.serialization.Serializable

@Serializable
data class GeocodingResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val admin1: String? = null
)
