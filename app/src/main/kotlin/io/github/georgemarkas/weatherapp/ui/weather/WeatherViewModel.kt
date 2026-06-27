package io.github.georgemarkas.weatherapp.ui.weather

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.georgemarkas.weatherapp.background.WeatherUpdateWorker
import io.github.georgemarkas.weatherapp.data.LocationRepository
import io.github.georgemarkas.weatherapp.data.SettingsRepository
import io.github.georgemarkas.weatherapp.data.WeatherRepository
import io.github.georgemarkas.weatherapp.extensions.isOnline
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
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val isRefreshing = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)
    private val flags = combine(isRefreshing, error) { refreshing, error ->
        refreshing to error
    }
    val uiState: StateFlow<WeatherUiState> =
        combine(
            locationRepository.specifiedLocationFlow,
            locationRepository.currentLocationFlow,
            weatherRepository.weatherFlow,
            settingsRepository.settingsFlow,
            flags
        ) { specLocality, locality, weather, settings, (refreshing, error) ->
            WeatherUiState(
                specifiedLocality = specLocality?.locality,
                currentLocality = locality?.locality,
                weather = weather,
                settings = settings,
                isRefreshing = refreshing,
                error = error
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = WeatherUiState(isLoading = true)
            )

    suspend fun scheduleInitialJob(context: Context) {
        WeatherUpdateWorker.scheduleJob(context, settingsRepository)
        WeatherUpdateWorker.start(context)
    }

    fun refresh(context: Context) {
        viewModelScope.launch {
            isRefreshing.value = true
            error.value = null

            try {
                if (context.isOnline()) {
                    val specifiedLocationSet = settingsRepository.settingsFlow.first().specifiedLocation
                    if (specifiedLocationSet) {
                        val location = locationRepository.specifiedLocationFlow.first()
                        weatherRepository.specifiedLocationWeatherUpdate(location)
                    } else {
                        weatherRepository.currentLocationWeatherUpdate()
                    }
                } else {
                    Timber.d("Can not refresh while offline")
                    Toast.makeText(context, "No internet connection", Toast.LENGTH_LONG).show()
                    error.value = "No internet connection"
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to refresh")
                Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show()
                error.value = "Failed to refresh"
            } finally {
                isRefreshing.value = false
            }
        }
    }
}