package io.github.georgemarkas.weatherapp.data

import io.github.georgemarkas.weatherapp.openmeteo.OpenMeteoService
import io.github.georgemarkas.weatherapp.openmeteo.models.WeatherResponse
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val service: OpenMeteoService,
    private val dataStore: WeatherDataStore
) {
    val weatherFlow: Flow<WeatherResponse?> = dataStore.cachedWeatherResponse

//    suspend fun updateWeather(): Result<WeatherResponse> {
//        return runCatching { service.requestWeather() }
//            .onSuccess { response ->
//                dataStore.save(response)
//            }
//            .onFailure { e ->
//                Timber.w(e, "Failed to fetch weather data")
//            }
//    }

    suspend fun updateWeather() {
        try {
            val response = service.requestWeather()
            dataStore.save(response)
        } catch (e: Exception) {
            Timber.w(e, "Failed to fetch weather data")
        }
    }
}