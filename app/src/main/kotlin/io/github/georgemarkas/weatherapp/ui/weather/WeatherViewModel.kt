package io.github.georgemarkas.weatherapp.ui.weather

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.georgemarkas.weatherapp.data.LocationRepository
import io.github.georgemarkas.weatherapp.data.WeatherRepository
import io.github.georgemarkas.weatherapp.openmeteo.OpenMeteoService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository,
    private val service: OpenMeteoService
) : ViewModel() {

    val uiState: StateFlow<WeatherUiState> = weatherRepository.weatherFlow
        .map { weather -> WeatherUiState(weather) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeatherUiState(isLoading = true)
        )

    fun refresh() {
        viewModelScope.launch {
            try {
                locationRepository.updateLocation()
                val location = locationRepository.locationFlow.first()
                weatherRepository.updateWeather(location)
            } catch (e: Exception) {
                Timber.w(e, "Refresh failed")
            }
        }
    }

    fun getWeatherText(context: Context, code: Int?): String? =
        service.getWeatherCodeDescription(context, code)
}