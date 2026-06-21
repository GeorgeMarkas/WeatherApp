package io.github.georgemarkas.weatherapp.openmeteo.models.geocoding

import kotlinx.serialization.Serializable

@Serializable
data class GeocodingResponse(
    val results: List<GeocodingResult>? = null
)