package io.github.georgemarkas.weatherapp.data

import io.github.georgemarkas.weatherapp.openmeteo.OpenMeteoService
import io.github.georgemarkas.weatherapp.openmeteo.models.WeatherResponse
import timber.log.Timber
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val service: OpenMeteoService
) {

    // TODO: Implement retry/refresh + caching logic

    suspend fun getWeather(): Result<WeatherResponse> {
        val response = service.requestWeather()

        return runCatching { response }
            .onFailure { e ->
                Timber.w(e, "Failed to fetch weather data")
            }
    }
}