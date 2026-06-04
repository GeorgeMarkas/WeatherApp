package io.github.georgemarkas.weatherapp.data.weather

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.georgemarkas.weatherapp.location.LocationWrapper
import io.github.georgemarkas.weatherapp.openmeteo.OpenMeteoService
import timber.log.Timber
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val service: OpenMeteoService,
    private val dataStore: WeatherDataStore
) {
    val weatherFlow = context.weatherDataStore.data

    suspend fun updateWeather(location: LocationWrapper?) {
        if (location == null) throw IllegalArgumentException("Location can not be null")
        val weather = service.requestWeather(location)

       if (weather != null) {
           if (weather.error == null) {
               dataStore.cacheWeather(weather)
           } else {
               Timber.w(weather.reason ?: "Unknown error; OpenMeteo provided no reason")
           }
       } else {
           Timber.e("Failed to update weather")
       }
    }
}