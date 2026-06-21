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

// TODO: Perhaps have this be provided by a module
val Context.locationDataStore by preferencesDataStore(name = "location")

class LocationRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val service: LocationService
) {
    private companion object {
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
    }

    val locationFlow: Flow<LocationWrapper?> =
        context.locationDataStore.data.map { preferences ->
            val latitude = preferences[LATITUDE] ?: return@map null
            val longitude = preferences[LONGITUDE] ?: return@map null
            LocationWrapper(latitude, longitude)
        }

    suspend fun updateLocation() {
        val location = service.getFreshLocation() ?: run {
            Timber.w("Failed to get current location, attempting to fall back to last known")
            service.getLastKnownLocation()
        } ?: run {
            Timber.e("Failed to update location")
            return
        }

        storeLocation(location)
        Timber.i("Updated location")
    }

    private suspend fun storeLocation(location: LocationWrapper) {
        context.locationDataStore.edit { preferences ->
            preferences[LATITUDE] = location.latitude
            preferences[LONGITUDE] = location.longitude
            Timber.d("Stored location in DataStore")
        }
    }
}