package com.example.weatherapp.ui.weather

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle

@Composable
fun WeatherLayout(
    requestBatteryOptimizationExemption: () -> Unit,
    viewModel: WeatherViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // This permission is special and needs to be requested separately
    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        /* TODO: Maybe handle background location permission denial?
            Without it, the worker will fall back to cached location,
            which might also be acceptable. */
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
    }

    LaunchedEffect(lifecycleOwner) {

        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            val coarseLocationGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (coarseLocationGranted) {
                viewModel.onPermissionResult(true)

                val backgroundLocationGranted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (!backgroundLocationGranted) {
                    backgroundLocationLauncher
                        .launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }

                requestBatteryOptimizationExemption()
            } else {
                val permissions = buildList {
                    add(Manifest.permission.ACCESS_COARSE_LOCATION)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                permissionsLauncher.launch(permissions.toTypedArray())
            }
        }
    }

    when (val state = uiState) {
            WeatherUiState.Loading -> {
                CircularProgressIndicator()
                Spacer(Modifier.height(8.dp))
                Text("Fetching weather...")
            }

            WeatherUiState.PermissionDenied -> {
                Text("Location permission denied", style = MaterialTheme.typography.bodyLarge)
            }

            WeatherUiState.NoLocation -> {
                Text("Could not determine location", style = MaterialTheme.typography.bodyLarge)
            }

            is WeatherUiState.Error -> {
                Text("Error: ${state.message}", style = MaterialTheme.typography.bodyLarge)
            }

            is WeatherUiState.Success -> {
                val weatherState = state.weather
                HomeComposable(weatherState)
            }
        }
}