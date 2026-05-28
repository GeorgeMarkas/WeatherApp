package com.example.weatherapp.ui.weather

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weatherapp.openmeteo.weatherCodeToDescription
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.ui.platform.LocalLocale
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle

@Composable
fun WeatherLayout(viewModel: WeatherViewModel = hiltViewModel()) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            viewModel.onPermissionResult(granted)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                val condition = weatherCodeToDescription(weatherState.weatherCode)
                val updatedAt = SimpleDateFormat("HH:mm", LocalLocale.current.platformLocale)
                    .format(Date(weatherState.lastUpdatedMs))

                Text(
                    text = "${weatherState.temperature}°C",
                    style = MaterialTheme.typography.displayLarge
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = condition,
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "High ${weatherState.temperatureMax}°C • Low ${weatherState.temperatureMin}°C",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Latitude: %.4f • Longitude: %.4f".format(weatherState.latitude, weatherState.longitude),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Last updated: $updatedAt",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}