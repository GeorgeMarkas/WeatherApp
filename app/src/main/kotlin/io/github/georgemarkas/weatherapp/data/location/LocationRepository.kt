package io.github.georgemarkas.weatherapp.data.location

import io.github.georgemarkas.weatherapp.location.LocationService
import timber.log.Timber
import javax.inject.Inject

class LocationRepository @Inject constructor(
    private val service: LocationService,
    private val dataStore: LocationDataStore
) {
    val locationFlow = dataStore.locationFlow()

    suspend fun updateLocation() {
        val location = service.getFreshLocation() ?: run {
            Timber.w("Failed to get current location, falling back to last known")
            service.getLastKnownLocation()
        }

        if (location != null) {
            dataStore.cacheLocation(location)
        } else {
            Timber.e("Failed to update location")
        }
    }
}