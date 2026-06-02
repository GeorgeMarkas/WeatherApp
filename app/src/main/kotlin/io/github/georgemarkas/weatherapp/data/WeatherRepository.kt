package io.github.georgemarkas.weatherapp.data

import io.github.georgemarkas.weatherapp.data.model.WeatherResponseWrapper
import io.github.georgemarkas.weatherapp.openmeteo.OpenMeteoService
import timber.log.Timber
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val service: OpenMeteoService
) {

    // TODO: Implement retry/refresh + caching logic

    suspend fun getWeather(): Result<WeatherResponseWrapper> {
        val response = service.requestWeather()

        return runCatching {
            if (response.error != true) {
                WeatherResponseWrapper.Success(response)
            } else {
                // Getting an error with no reason being provided shouldn't happen in
                // practice, but OpenMeteo's API docs don't explicitely state that, so we
                // handle it just in case.
                WeatherResponseWrapper.Error(
                    response.reason ?: "Unknown error; OpenMeteo provided no reason"
                )
            }
        }
            .onFailure { e ->
                Timber.w(e, "Failed to fetch weather data")
            }
    }
}