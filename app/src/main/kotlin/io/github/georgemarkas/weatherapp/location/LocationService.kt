package io.github.georgemarkas.weatherapp.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.georgemarkas.weatherapp.extensions.hasPermission
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume

@SuppressLint("MissingPermission")
class LocationService @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val locationClient: FusedLocationProviderClient,
) {

    suspend fun getLastKnownLocation(): Location? =
        suspendCancellableCoroutine { continuation ->

            if (!hasLocationPermission()) {
                continuation.resume(null)
                Timber.d("Location permission not granted")
                return@suspendCancellableCoroutine
            }

            locationClient.lastLocation
                .addOnSuccessListener {
                    continuation.resume(it)
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
        }

    suspend fun getFreshLocation(): Location? =
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
                    continuation.resume(it)
                }
                .addOnFailureListener { e ->
                    Timber.e(e, "Fused location provider failed to supply the current location")
                    continuation.resume(null)
                }

            continuation.invokeOnCancellation { cancellationTokenSource.cancel() }
        }

    private fun hasLocationPermission() =
        context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
}