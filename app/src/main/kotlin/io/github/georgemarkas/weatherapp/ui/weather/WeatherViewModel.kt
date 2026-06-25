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
import io.github.georgemarkas.weatherapp.location.LocationWrapper
import io.github.georgemarkas.weatherapp.settings.models.Units
import io.github.georgemarkas.weatherapp.settings.models.UpdateInterval
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

    val uiState: StateFlow<WeatherUiState> =
        combine(
            locationRepository.specifiedLocationFlow,
            locationRepository.currentLocationFlow,
            weatherRepository.weatherFlow,
            settingsRepository.settingsFlow,
            isRefreshing
        ) { specLocality, locality, weather, settings, isRefreshing ->
            WeatherUiState(
                specifiedLocality = specLocality?.locality,
                currentLocality = locality?.locality,
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

    fun refresh(context: Context) {
        viewModelScope.launch {
            isRefreshing.value = true

            val preferenceSpecific = settingsRepository.settingsFlow.first().specificLocation
            try {
                if (context.isOnline()) {
                    // TODO: Use the user-specified location should it be chosen from settings
                    //  (Done but ugly)
                    if (preferenceSpecific) {
                        val location = locationRepository.specifiedLocationFlow.first()
                        if (location != null) {
                            weatherRepository.specifiedLocationWeatherUpdate(location)
                        } else {
                            Timber.w("Specification enabled, but no location set. Falling back to current location")
                            weatherRepository.currentLocationWeatherUpdate()
                        }
                    }else
                        weatherRepository.currentLocationWeatherUpdate()
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