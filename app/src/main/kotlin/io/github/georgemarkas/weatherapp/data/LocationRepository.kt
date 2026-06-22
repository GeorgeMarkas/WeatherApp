package io.github.georgemarkas.weatherapp.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.georgemarkas.weatherapp.geocoding.GeocodingService
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
    private val locationService: LocationService,
    private val geocodingService: GeocodingService
) {
    companion object {
        val CURRENT_LATITUDE = doublePreferencesKey("current_latitude")
        val CURRENT_LONGITUDE = doublePreferencesKey("current_longitude")
        val CURRENT_LOCALITY = stringPreferencesKey("current_locality")

        val SPECIFIED_LATITUDE = doublePreferencesKey("specified_latitude")
        val SPECIFIED_LONGITUDE = doublePreferencesKey("specified_longitude")
        val SPECIFIED_LOCALITY =  stringPreferencesKey("specified_locality")

        private const val GEOCODER_ERROR_LOCALITY_PLACEHOLDER = "Rivendell"
    }

    val currentLocationFlow: Flow<LocationWrapper?> =
        context.currentLocationDataStore.data.map { preferences ->
            val latitude = preferences[CURRENT_LATITUDE] ?: return@map null
            val longitude = preferences[CURRENT_LONGITUDE] ?: return@map null
            val locality = preferences[CURRENT_LOCALITY] ?: return@map null

            LocationWrapper(latitude, longitude, locality)
        }

    val specifiedLocationFlow: Flow<LocationWrapper?> =
        context.specifiedLocationDataStore.data.map { preferences ->
            val latitude = preferences[SPECIFIED_LATITUDE] ?: return@map null
            val longitude = preferences[SPECIFIED_LONGITUDE] ?: return@map null
            val locality = preferences[SPECIFIED_LOCALITY] ?: return@map null

            LocationWrapper(latitude, longitude, locality)
        }

    /**
     * Fetches the current location and saves it.
     */
    suspend fun updateCurrentLocation() {
        val coords = locationService.getFreshLocation() ?: run {
            Timber.w("Failed to get current location, attempting to fall back to last known")
            locationService.getLastKnownLocation()
        } ?: run {
            Timber.e("Failed to update location")
            return
        }

        val locality = geocodingService.getLocality(coords.latitude, coords.longitude)

        storeCurrentLocation(LocationWrapper(coords.latitude, coords.longitude, locality))
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
            preferences[CURRENT_LOCALITY] = location.locality ?: run {
                Timber.e("Geocoder failed to supply the locality name")
                GEOCODER_ERROR_LOCALITY_PLACEHOLDER
            }
            Timber.d("Stored current location in DataStore")
        }
    }

    private suspend fun storeSpecifiedLocation(location: LocationWrapper) {
        context.specifiedLocationDataStore.edit { preferences ->
            preferences[SPECIFIED_LATITUDE] = location.latitude
            preferences[SPECIFIED_LONGITUDE] = location.longitude
            preferences[SPECIFIED_LOCALITY] = location.locality!!
            Timber.d("Stored specified location in DataStore")
        }
    }
}