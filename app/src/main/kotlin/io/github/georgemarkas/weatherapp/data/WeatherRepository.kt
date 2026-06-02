package io.github.georgemarkas.weatherapp.data

import io.github.georgemarkas.weatherapp.openmeteo.OpenMeteoService
import io.github.georgemarkas.weatherapp.openmeteo.model.WeatherResult
import timber.log.Timber
import javax.inject.Inject

sealed class WeatherResultWrapper {
    data class Success(val result: WeatherResult) : WeatherResultWrapper()
    data class Error(val message: String) : WeatherResultWrapper()
}

class WeatherRepository @Inject constructor(
    private val service: OpenMeteoService
) {

    // TODO: Implement retry/refresh + caching logic

    suspend fun getWeather(): Result<WeatherResultWrapper> {
        val response = service.requestWeather()
        return runCatching {
            if (response.error != true) {
                WeatherResultWrapper.Success(response)
            } else {
                // Getting an error with no reason being provided shouldn't happen in
                // practice, but OpenMeteo's API doesn't explicitely state that, so we
                // handle it just in case.
                WeatherResultWrapper.Error(
                    response.reason ?: "Unknown error; OpenMeteo provided no reason"
                )
            }
        }
            .onFailure { e ->
                Timber.w(e, "Failed to fetch weather data")
            }
    }
}