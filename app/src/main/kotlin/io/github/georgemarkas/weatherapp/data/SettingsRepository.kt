package io.github.georgemarkas.weatherapp.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.georgemarkas.weatherapp.settings.Settings
import io.github.georgemarkas.weatherapp.settings.models.Units
import io.github.georgemarkas.weatherapp.settings.models.UpdateInterval
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.settingsDataStore by preferencesDataStore(name = "settings")

class SettingsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private object Keys {
        val UPDATE_INTERVAL = longPreferencesKey("update_interval")
        val UNITS = stringPreferencesKey("units")
        val WEATHER_ALERTS = booleanPreferencesKey("weather_alerts")
    }

    val settingsFlow: Flow<Settings> = context.settingsDataStore.data.map { preferences ->
        Settings(
            updateInterval = UpdateInterval.fromMinutes(
                preferences[Keys.UPDATE_INTERVAL] ?: UpdateInterval.DEFAULT.minutes
            ),

            units = preferences[Keys.UNITS]?.let {
                runCatching { Units.valueOf(it) }.getOrNull()
            } ?: Units.METRIC,

            weatherAlerts = preferences[Keys.WEATHER_ALERTS] ?: false
        )
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
}