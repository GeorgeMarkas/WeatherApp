package io.github.georgemarkas.weatherapp.geocoding

import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.resume

class GeocodingService @Inject constructor(
    private val geocoder: Geocoder
) {
    suspend fun getLocality(latitude: Double, longitude: Double): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    continuation.resume(addresses.firstOrNull()?.locality)
                }
            }
        } else {
            withContext(Dispatchers.IO) {
                try {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(latitude, longitude, 1)
                        ?.firstOrNull()?.locality
                } catch (e: IOException) {
                    Timber.e(e, "Geocoder I/O error")
                    null
                }
            }
        }
    }
}