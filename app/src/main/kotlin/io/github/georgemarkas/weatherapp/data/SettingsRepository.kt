package io.github.georgemarkas.weatherapp.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.georgemarkas.weatherapp.settings.Settings
import io.github.georgemarkas.weatherapp.settings.models.Units
import io.github.georgemarkas.weatherapp.settings.models.UpdateInterval
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val UPDATE_INTERVAL = longPreferencesKey("update_interval")
        val UNITS = stringPreferencesKey("units")
        val WEATHER_ALERTS = booleanPreferencesKey("weather_alerts")
    }

    val settingsFlow: Flow<Settings> = dataStore.data.map { preferences ->
        Settings(
            updateInterval = UpdateInterval.fromMinutes(
                preferences[UPDATE_INTERVAL] ?: UpdateInterval.DEFAULT.minutes
            ),

            units = preferences[UNITS]?.let { runCatching { Units.valueOf(it) }.getOrNull() }
                ?: Units.METRIC,

            weatherAlerts = preferences[WEATHER_ALERTS] ?: false
        )
    }

    suspend fun setUpdateInterval(interval: UpdateInterval) {
        dataStore.edit { it[UPDATE_INTERVAL] = interval.minutes }
    }

    suspend fun setUnits(units: Units) {
        dataStore.edit { it[UNITS] = units.name }
    }

    suspend fun setWeatherAlerts(enabled: Boolean) {
        dataStore.edit { it[WEATHER_ALERTS] = enabled }
    }
}