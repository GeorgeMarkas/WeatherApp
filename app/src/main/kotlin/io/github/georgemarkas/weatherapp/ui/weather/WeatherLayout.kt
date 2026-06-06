package io.github.georgemarkas.weatherapp.ui.weather

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import io.github.georgemarkas.weatherapp.background.WeatherUpdateWorker
import io.github.georgemarkas.weatherapp.extensions.hasPermission
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun WeatherLayout(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    var locationGranted by remember {
        mutableStateOf(context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    // Background location permission is special and needs to be requested separately
    val bgLocationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // TODO: Possibly handle background location permission denial
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        locationGranted = grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(locationGranted) {
        if (locationGranted && !WeatherUpdateWorker.isScheduled(context)) {
            WeatherUpdateWorker.scheduleJob(context)
            WeatherUpdateWorker.start(context)
        }
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {

            if (context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {

                if (!context.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    // TODO: Perhaps add some sort of alert dialog here
                    bgLocationPermissionLauncher
                        .launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }

                // TODO: Maybe request battery optimization exemption
            } else {
                val permissions = buildList {
                    add(Manifest.permission.ACCESS_COARSE_LOCATION)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        add(Manifest.permission.POST_NOTIFICATIONS)
                }

                permissionsLauncher.launch(permissions.toTypedArray())
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
                Spacer(Modifier.height(8.dp))
                Text("Fetching weather...")
            }

            uiState.weather != null -> {
                Text(
                    text = "${uiState.weather!!.current?.temperature}°C",
                    style = MaterialTheme.typography.displayLarge,
                )

                Text(
                    text = "${
                        viewModel.getWeatherText(
                            context,
                            uiState.weather!!.current?.weatherCode
                        )
                    }",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(8.dp))

                val temperatureMin = uiState.weather!!.daily?.temperatureMin?.get(0)
                val temperatureMax = uiState.weather!!.daily?.temperatureMax?.get(0)
                Text(
                    text = "Low $temperatureMin°C • High $temperatureMax°C",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(8.dp))

                val lastUpdatedAt = SimpleDateFormat("HH:mm", LocalLocale.current.platformLocale)
                    .format(Date(uiState.weather!!.fetchedAt))
                Text(
                    text = "Last updated at $lastUpdatedAt",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // TODO: Handle erroneous states / lack of permissions somehow
        }
    }
}

@Preview
@Composable
fun WeatherLayoutPreview() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "21.8°C",
            style = MaterialTheme.typography.displayLarge,
        )
        Spacer(Modifier.height(8.dp))

        Text(
            text = "Partly cloudy",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(8.dp))

        Text(
            text = "Low 17.8°C • High 22.4°C",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(8.dp))

        Text(
            text = "Last updated at 01:53",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}