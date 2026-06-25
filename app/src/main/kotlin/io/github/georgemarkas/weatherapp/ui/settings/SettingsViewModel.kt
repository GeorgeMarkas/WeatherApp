package io.github.georgemarkas.weatherapp.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.georgemarkas.weatherapp.data.LocationRepository
import io.github.georgemarkas.weatherapp.background.WeatherUpdateWorker
import io.github.georgemarkas.weatherapp.data.SettingsRepository
import io.github.georgemarkas.weatherapp.location.LocationWrapper
import io.github.georgemarkas.weatherapp.settings.models.Units
import io.github.georgemarkas.weatherapp.settings.models.UpdateInterval
import io.github.georgemarkas.weatherapp.ui.settings.data.GeocodedLocation
import io.github.georgemarkas.weatherapp.ui.settings.data.SettingsUiState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    private val isSearching = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> =
        combine(
            locationRepository.specifiedLocationFlow,
            settingsRepository.settingsFlow,
            settingsRepository.geocodedLocationsFlow,
            isSearching
        ) { specifiedLocation, settings, searchResults, isSearching->
            SettingsUiState(
                specifiedLocality = specifiedLocation?.locality,
                specifiedAdmin1 = specifiedLocation?.admin1,
                specifiedCountryCode = specifiedLocation?.countryCode,
                settings = settings,
                searchResults = searchResults,
                isSearching = isSearching
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = SettingsUiState()
            )

    init {
        searchQuery
            .debounce(600.milliseconds)
            .filter { it.isNotBlank() }
            .distinctUntilChanged()
            .onEach { query ->
                isSearching.value = true
                settingsRepository.searchLocations(query)
                    .onFailure { e ->
                        Timber.e(e, "Geocoding search failed")
                    }
                isSearching.value = false
            }
            .launchIn(viewModelScope)
    }

    fun updateGeolocationResults(query: String) {
        searchQuery.value = query
        if (query.isBlank()) {
            settingsRepository.clearSearchResults()
        }
    }

    fun extractAndSetChoice(choice: GeocodedLocation) {
        viewModelScope.launch {
            val location = LocationWrapper(
                choice.latitude,
                choice.longitude,
                choice.name,
                choice.countryCode,
                choice.admin1
            )

            locationRepository.updateSpecifiedLocation(location)
        }
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

    fun setSpecificLocationEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSpecificLocationEnabled(enabled) }
    }

}