package com.example.weatherapp.ui.weather

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weatherapp.ui.theme.WeatherappTheme

/**
 * This is just a demo that displays the fetched JSON data from OpenMeteo,
 * obviously to be removed later on when proper UI and logic is in place.
 */
@Composable
fun WeatherLayout(viewModel: WeatherViewModel = hiltViewModel()) {

    val weatherResponse by viewModel.weatherResponse.collectAsStateWithLifecycle()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        WeatherappTheme {
            Text(text = weatherResponse)
        }
    }
}