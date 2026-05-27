package com.example.weatherapp.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@SuppressLint("MissingPermission")
@Singleton
class LocationService @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) {
    suspend fun getLastKnownLocation(): Location? = suspendCancellableCoroutine { cont ->
        if (!hasGrantedLocationPermission()) {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resume(null) }
    }

    suspend fun getFreshLocation(): Location? = suspendCancellableCoroutine { cont ->
        val cancellationTokenSource = CancellationTokenSource()

        if (!hasGrantedLocationPermission()) {
            cancellationTokenSource.cancel()
            cont.resume(null)
            return@suspendCancellableCoroutine
        }

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            cancellationTokenSource.token
        )
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resume(null) }

        cont.invokeOnCancellation { cancellationTokenSource.cancel() }
    }

    private fun hasGrantedLocationPermission() = ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}