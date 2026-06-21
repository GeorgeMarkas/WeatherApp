package io.github.georgemarkas.weatherapp.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.georgemarkas.weatherapp.location.LocationService
import io.github.georgemarkas.weatherapp.location.LocationWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

// The current location provided by the fused location provider API
val Context.currentLocationDataStore by preferencesDataStore(name = "current_location")

// A static location explicitly set by the user in settings
val Context.specifiedLocationDataStore by preferencesDataStore(name = "specified_location")

class LocationRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val service: LocationService
) {
    companion object {
        val CURRENT_LATITUDE = doublePreferencesKey("current_latitude")
        val CURRENT_LONGITUDE = doublePreferencesKey("current_longitude")

        val SPECIFIED_LATITUDE = doublePreferencesKey("specified_latitude")
        val SPECIFIED_LONGITUDE = doublePreferencesKey("specified_longitude")
    }

    val currentLocationFlow: Flow<LocationWrapper?> =
        context.currentLocationDataStore.data.map { preferences ->
            val latitude = preferences[CURRENT_LATITUDE] ?: return@map null
            val longitude = preferences[CURRENT_LONGITUDE] ?: return@map null
            LocationWrapper(latitude, longitude)
        }

    val specifiedLocationFlow: Flow<LocationWrapper?> =
        context.specifiedLocationDataStore.data.map { preferences ->
            val latitude = preferences[CURRENT_LATITUDE] ?: return@map null
            val longitude = preferences[CURRENT_LONGITUDE] ?: return@map null
            LocationWrapper(latitude, longitude)
        }

    /**
     * Fetches the current location and saves it.
     */
    suspend fun updateCurrentLocation() {
        val location = service.getFreshLocation() ?: run {
            Timber.w("Failed to get current location, attempting to fall back to last known")
            service.getLastKnownLocation()
        } ?: run {
            Timber.e("Failed to update location")
            return
        }

        storeCurrentLocation(location)
        Timber.i("Updated current location")
    }

    /**
     * Saves the given location.
     */
    suspend fun updateSpecifiedLocation(location: LocationWrapper) {
        storeSpecifiedLocation(location)
        Timber.i("Updated specified location")
    }

    private suspend fun storeCurrentLocation(location: LocationWrapper) {
        context.currentLocationDataStore.edit { preferences ->
            preferences[CURRENT_LATITUDE] = location.latitude
            preferences[CURRENT_LONGITUDE] = location.longitude
            Timber.d("Stored current location in DataStore")
        }
    }

    private suspend fun storeSpecifiedLocation(location: LocationWrapper) {
        context.specifiedLocationDataStore.edit { preferences ->
            preferences[SPECIFIED_LATITUDE] = location.latitude
            preferences[SPECIFIED_LONGITUDE] = location.longitude
            Timber.d("Stored specified location in DataStore")
        }
    }
}