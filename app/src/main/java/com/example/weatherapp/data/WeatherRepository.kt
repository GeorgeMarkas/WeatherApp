package com.example.weatherapp.data

import com.example.weatherapp.location.LocationService
import com.example.weatherapp.openmeteo.WeatherResult
import com.example.weatherapp.openmeteo.WeatherService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

sealed class WeatherFetchResult {
    data class Success(
        val weather: WeatherResult,
        val latitude: Double,
        val longitude: Double
    ) : WeatherFetchResult()

    data class Error(val message: String) : WeatherFetchResult()
    object NoLocation : WeatherFetchResult()
}

@Singleton
class WeatherRepository @Inject constructor(
    private val locationService: LocationService
) {
    suspend fun fetchWeather(): WeatherFetchResult {
        val location = locationService.getLastKnownLocation()
            ?: locationService.getFreshLocation()
            ?: return WeatherFetchResult.NoLocation

        return try {
            val result = withContext(Dispatchers.IO) {
                WeatherService.forecastApi.getWeather(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            }

            WeatherFetchResult.Success(result, location.latitude, location.longitude)
        } catch (e: Exception) {
            WeatherFetchResult.Error(e.message ?: "Unknown error")
        }
    }
}