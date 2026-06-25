package io.github.georgemarkas.weatherapp.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.georgemarkas.weatherapp.openmeteo.OpenMeteoService
import io.github.georgemarkas.weatherapp.openmeteo.models.geocoding.GeocodingResult
import io.github.georgemarkas.weatherapp.settings.Settings
import io.github.georgemarkas.weatherapp.settings.models.Units
import io.github.georgemarkas.weatherapp.settings.models.UpdateInterval
import io.github.georgemarkas.weatherapp.ui.settings.data.GeocodedLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

val Context.settingsDataStore by preferencesDataStore(name = "settings")

class SettingsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val geolocationService: OpenMeteoService,
) {
    private object Keys {
        val UPDATE_INTERVAL = longPreferencesKey("update_interval")
        val UNITS = stringPreferencesKey("units")
        val WEATHER_ALERTS = booleanPreferencesKey("weather_alerts")
        val SPECIFIC_LOCATION_ENABLED = booleanPreferencesKey("specific_location_enabled")
    }

    val settingsFlow: Flow<Settings> = context.settingsDataStore.data.map { preferences ->
        Settings(
            updateInterval = UpdateInterval.fromMinutes(
                preferences[Keys.UPDATE_INTERVAL] ?: UpdateInterval.DEFAULT.minutes
            ),
            units = preferences[Keys.UNITS]?.let {
                runCatching { Units.valueOf(it) }.getOrNull()
            } ?: Units.METRIC,
            weatherAlerts = preferences[Keys.WEATHER_ALERTS] ?: false,
            specificLocation = preferences[Keys.SPECIFIC_LOCATION_ENABLED] ?: false
        )
    }

    private val _geocodingResults = MutableStateFlow<List<GeocodingResult>>(emptyList())
    val geocodedLocationsFlow: Flow<List<GeocodedLocation>> = _geocodingResults.map { results ->
        results.mapNotNull { it.toDomainOrNull() }
    }

    /**
     * Triggers a search, pushing results (or an empty list on failure) into geocodedLocationsFlow.
     * Returns Result<Unit> purely so the caller can react to/log failures if needed.
     */
    suspend fun searchLocations(query: String): Result<Unit> {
        return try {
            val response = geolocationService.requestGeocodedLocations(query).getOrThrow()
            _geocodingResults.value = response.results ?: emptyList()
            Result.success(Unit)
        } catch (e: Exception) {
            _geocodingResults.value = emptyList()
            Result.failure(e)
        }
    }
    fun clearSearchResults() {
        _geocodingResults.value = emptyList()
    }

    private fun GeocodingResult.toDomainOrNull(): GeocodedLocation? {
        if (name == null || latitude == null || longitude == null) {
            Timber.w("Discarding incomplete geocoding result: $this")
            return null
        }
        return GeocodedLocation(
            name, latitude, longitude,
            country ?: "", countryCode ?: "",
            admin1 ?: "", admin3 ?: "")
    }


    suspend fun setUpdateInterval(interval: UpdateInterval) {
        context.settingsDataStore.edit { it[Keys.UPDATE_INTERVAL] = interval.minutes }
    }

    suspend fun setUnits(units: Units) {
        context.settingsDataStore.edit { it[Keys.UNITS] = units.name }
    }

    suspend fun setWeatherAlerts(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.WEATHER_ALERTS] = enabled }
    }

    suspend fun setSpecificLocationEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.SPECIFIC_LOCATION_ENABLED] = enabled }
    }
}