package io.github.georgemarkas.weatherapp.openmeteo

import io.github.georgemarkas.weatherapp.openmeteo.models.forecast.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoForecastApi {

    @GET("v1/forecast?timezone=auto&timeformat=unixtime")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String,
        @Query("hourly") hourly: String,
        @Query("daily") daily: String,
        @Query("forecast_days") forecastDays: Int,
    ): WeatherResponse
}