package com.example.weatherapp.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.location.LocationService
import com.example.weatherapp.openmeteo.WeatherService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val locationService: LocationService
) : ViewModel() {

    private val _weatherResponse = MutableStateFlow("Loading...")
    val weatherResponse = _weatherResponse.asStateFlow()

    fun onPermissionResult(granted: Boolean) {
        if (!granted) {
            _weatherResponse.value = "Location permission denied"
            return
        }

        viewModelScope.launch {
            val location = locationService.getLastKnownLocation()
                ?: locationService.getFreshLocation()

            if (location == null) {
                _weatherResponse.value = "Could not retrieve location"
                return@launch
            }

            fetchWeather(location.latitude, location.longitude)
        }
    }

    private suspend fun fetchWeather(latitude: Double, longitude: Double) {
        try {
            val result = withContext(Dispatchers.IO) {
                WeatherService.forecastApi.getWeather(
                    latitude = latitude,
                    longitude = longitude
                )
            }
            _weatherResponse.value = result.toString()
        } catch (e: Exception) {
            _weatherResponse.value = e.message ?: "Unknown error"
        }
    }
}