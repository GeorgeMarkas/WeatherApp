package io.github.georgemarkas.weatherapp.ui.weather

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.georgemarkas.weatherapp.data.LocationRepository
import io.github.georgemarkas.weatherapp.data.WeatherRepository
import io.github.georgemarkas.weatherapp.extensions.isOnline
import io.github.georgemarkas.weatherapp.openmeteo.OpenMeteoService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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

    private val isRefreshing = MutableStateFlow(false)
    val uiState: StateFlow<WeatherUiState> =
        combine(
            weatherRepository.weatherFlow,
            isRefreshing
        ) { weather, isRefreshing ->
            WeatherUiState(weather = weather, isRefreshing = isRefreshing)
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = WeatherUiState(isLoading = true)
            )

    fun refresh(context: Context) {
        viewModelScope.launch {
            isRefreshing.value = true
            try {
                if (context.isOnline()) {
                    locationRepository.updateLocation()
                    val location = locationRepository.locationFlow.first()
                    weatherRepository.updateWeather(location)
                } else {
                    Toast.makeText(context, "No internet connection", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to refresh")
                Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show()
            } finally {
                isRefreshing.value = false
            }
        }
    }

    fun getWeatherText(context: Context, code: Int?): String? =
        service.getWeatherCodeDescription(context, code)
}