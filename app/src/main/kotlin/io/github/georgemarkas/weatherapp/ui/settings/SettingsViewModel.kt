package io.github.georgemarkas.weatherapp.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.georgemarkas.weatherapp.data.LocationRepository
import io.github.georgemarkas.weatherapp.background.WeatherUpdateWorker
import io.github.georgemarkas.weatherapp.data.SettingsRepository
import io.github.georgemarkas.weatherapp.exceptions.GeocodingException
import io.github.georgemarkas.weatherapp.location.LocationWrapper
import io.github.georgemarkas.weatherapp.openmeteo.models.geocoding.GeocodingResult
import io.github.georgemarkas.weatherapp.settings.models.Units
import io.github.georgemarkas.weatherapp.settings.models.UpdateInterval
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
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
    private val searchResults = MutableStateFlow<List<GeocodingResult>?>(emptyList())

    val uiState: StateFlow<SettingsUiState> =
        combine(
            settingsRepository.settingsFlow,
            locationRepository.specifiedLocationFlow,
            searchResults,
            isSearching
        ) { settings, location, results, isSearching ->
            SettingsUiState(
                settings = settings,
                specifiedLocality = location?.locality,
                countryCode = location?.countryCode,
                admin1 = location?.admin1,
                searchResults = results,
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
            // TODO: Implement this properly, as of now it re-hits the API
            //  but at least the behavior is what the user would expect
//            .distinctUntilChanged()
            .onEach { query ->
                isSearching.value = true
                try {
                    searchResults.value = locationRepository.searchLocations(query)
                } catch (e: GeocodingException) {
                    searchResults.value = emptyList()
                    Timber.e(e)
                    // TODO: Anton, add some sort of popup for this please
                } finally {
                    isSearching.value = false
                }
            }
            .launchIn(viewModelScope)
    }

    fun clearSearchResults() {
        searchQuery.value = ""
        searchResults.value = emptyList()
    }

    fun updateGeolocationResults(query: String) {
        searchQuery.value = query
        if (query.isBlank()) searchResults.value = emptyList()
    }

    fun extractAndSetChoice(choice: GeocodingResult) {
        viewModelScope.launch {
            val location = LocationWrapper(
                choice.latitude!!,
                choice.longitude!!,
                choice.admin3,
                choice.countryCode,
                choice.admin1,
                choice.admin4
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
        viewModelScope.launch { settingsRepository.setSpecifiedLocation(enabled) }
    }
}