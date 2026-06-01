package io.github.georgemarkas.weatherapp.data

import io.github.georgemarkas.weatherapp.openmeteo.OpenMeteoService
import io.github.georgemarkas.weatherapp.openmeteo.models.WeatherResult
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val service: OpenMeteoService
) {

    // TODO: Implement retry/refresh + caching logic

    suspend fun getWeather(): Result<WeatherResult> = runCatching {
        service.requestWeather()
    }
}