package io.github.georgemarkas.weatherapp.openmeteo.models.geocoding

import kotlinx.serialization.Serializable

@Serializable
data class GeocodingResponse(
    val results: List<GeocodingResult>? = null,
    val error: Boolean? = null,
    val reason: String? = null
)