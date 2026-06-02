package io.github.georgemarkas.weatherapp.data.model

import io.github.georgemarkas.weatherapp.openmeteo.model.WeatherResponse

sealed class WeatherResponseWrapper {
    data class Success(val result: WeatherResponse) : WeatherResponseWrapper()
    data class Error(val reason: String) : WeatherResponseWrapper()
}