package io.github.georgemarkas.weatherapp.data.location

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.georgemarkas.weatherapp.location.LocationWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

private val Context.locationDataStore by preferencesDataStore(name = "location")

class LocationDataStore @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    object LocationKeys {
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
    }

    suspend fun cacheLocation(location: LocationWrapper) {
        context.locationDataStore.edit { preferences ->
            preferences[LocationKeys.LATITUDE] = location.latitude
            preferences[LocationKeys.LONGITUDE] = location.longitude
            Timber.i("Cached location")
        }
    }

    fun locationFlow(): Flow<LocationWrapper?> =
        context.locationDataStore.data.map { preferences ->
            val latitude = preferences[LocationKeys.LATITUDE] ?: return@map null
            val longitude = preferences[LocationKeys.LONGITUDE] ?: return@map null
            LocationWrapper(latitude, longitude)
        }
}