package io.github.georgemarkas.weatherapp.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume

@SuppressLint("MissingPermission")
class LocationService @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val locationClient: FusedLocationProviderClient
) {

    suspend fun getLastKnownLocation(): LocationWrapper? =
        suspendCancellableCoroutine { continuation ->

            if (!hasLocationPermission()) {
                continuation.resume(null)
                Timber.d("Location permission not granted")
                return@suspendCancellableCoroutine
            }

            locationClient.lastLocation
                .addOnSuccessListener {
                    continuation.resume(LocationWrapper(it.latitude, it.longitude))
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
        }

    suspend fun getFreshLocation(): LocationWrapper? =
        suspendCancellableCoroutine { continuation ->

            val cancellationTokenSource = CancellationTokenSource()

            if (!hasLocationPermission()) {
                cancellationTokenSource.cancel()
                continuation.resume(null)
                Timber.d("Location permission not granted")
                return@suspendCancellableCoroutine
            }

            locationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            )
                .addOnSuccessListener {
                    continuation.resume(LocationWrapper(it.latitude, it.longitude))
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }

            continuation.invokeOnCancellation { cancellationTokenSource.cancel() }
        }

    private fun hasLocationPermission() = ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}