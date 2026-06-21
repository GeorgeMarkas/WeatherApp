package io.github.georgemarkas.weatherapp.openmeteo

import io.github.georgemarkas.weatherapp.openmeteo.models.geocoding.GeocodingResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoGeocodingApi {
    @GET("v1/search")
    suspend fun searchLocations(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): GeocodingResponse
}