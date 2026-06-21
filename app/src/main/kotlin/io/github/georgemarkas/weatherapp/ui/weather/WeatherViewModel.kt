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
import io.github.georgemarkas.weatherapp.settings.models.Units
import io.github.georgemarkas.weatherapp.settings.models.UpdateInterval
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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

    val uiState: StateFlow<WeatherUiState> =
        combine(
            weatherRepository.weatherFlow,
            settingsRepository.settingsFlow,
            isRefreshing
        ) { weather, settings, isRefreshing ->
            WeatherUiState(
                weather = weather,
                settings = settings,
                isRefreshing = isRefreshing
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

    fun setUpdateInterval(interval: UpdateInterval, context: Context) {
        viewModelScope.launch {
            settingsRepository.setUpdateInterval(interval)
            WeatherUpdateWorker.scheduleJob(context, settingsRepository)
        }
    }

    fun setUnits(units: Units) {
        viewModelScope.launch { settingsRepository.setUnits(units) }
    }

    fun setWeatherAlerts(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setWeatherAlerts(enabled) }
    }

    fun refresh(context: Context) {
        viewModelScope.launch {
            isRefreshing.value = true
            try {
                if (context.isOnline()) {
                    WeatherUpdateWorker.updateWeatherWithCurrentLocation(locationRepository, weatherRepository)
                } else {
                    Timber.d("Can not refresh while offline")
                    Toast.makeText(context, "No internet connection", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to refresh")
                Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show()
            } finally {
                isRefreshing.value = false
            }
        }
    }
}