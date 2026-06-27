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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import io.github.georgemarkas.weatherapp.background.WeatherUpdateWorker
import io.github.georgemarkas.weatherapp.extensions.hasPermission
import io.github.georgemarkas.weatherapp.settings.models.Units
import io.github.georgemarkas.weatherapp.util.celsiusToFahrenheit
import io.github.georgemarkas.weatherapp.openmeteo.OpenMeteoService
import io.github.georgemarkas.weatherapp.ui.theme.dimens
import io.github.georgemarkas.weatherapp.ui.weather.component.DailyForecastRowConditions
import io.github.georgemarkas.weatherapp.ui.weather.component.DailyForecastRowWind
import io.github.georgemarkas.weatherapp.ui.weather.component.ForecastBox
import io.github.georgemarkas.weatherapp.ui.weather.component.ForecastTab
import io.github.georgemarkas.weatherapp.ui.weather.component.HourlyForecastRowConditions
import io.github.georgemarkas.weatherapp.ui.weather.component.HourlyForecastRowWind
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.roundToInt

@Composable
fun WeatherLayout(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    var coarseLocationGranted by remember {
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
    ) { permissions ->
        coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.POST_NOTIFICATIONS
    }

    // TODO: This is most likely crap and needs to be redone
    LaunchedEffect(coarseLocationGranted) {
        if (coarseLocationGranted && !WeatherUpdateWorker.isScheduled(context))
            viewModel.scheduleInitialJob(context)
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {

            if (context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {

                if (!context.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    // TODO: Perhaps add some sort of dialog popup here
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

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refresh(context) },
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                uiState.isLoading -> {
                    LinearProgressIndicator()
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Fetching weather...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                uiState.weather != null -> {
                    Text(
                        text = "${uiState.locality}",
                        style =  MaterialTheme.typography.displayMedium
                    )
                    Spacer(Modifier.height(10.dp))

                    val temperatureUnit = uiState.settings.units.temperature

                    var temperature = uiState.weather?.current?.temperature
                    if (uiState.settings.units == Units.IMPERIAL && temperature != null)
                        temperature = celsiusToFahrenheit(temperature)

                    Text(
                        text = temperature?.let { "${it.roundToInt()}${temperatureUnit}" } ?: "—",
                        style = MaterialTheme.typography.displayLarge,
                    )

                    Text(
                        text = "${
                            OpenMeteoService.getWeatherCodeDescription(
                                context,
                                uiState.weather!!.current?.weatherCode
                            )
                        }",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(Modifier.height(8.dp))

                    var temperatureMin = uiState.weather!!.daily?.temperatureMin?.get(0)
                    var temperatureMax = uiState.weather!!.daily?.temperatureMin?.get(0)
                    if (uiState.settings.units == Units.IMPERIAL) {
                        temperatureMin = celsiusToFahrenheit(temperatureMin!!)
                        temperatureMax = celsiusToFahrenheit(temperatureMax!!)
                    }

                    Text(
                        text = "Low $temperatureMin${temperatureUnit} • High $temperatureMax${temperatureUnit}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(8.dp))


                    val daily = uiState.weather?.daily
                    if (daily != null) {
                        Spacer(Modifier.height(32.dp))
                        ForecastBox(
                            title = "Daily forecast",
                            tabs = listOf(
                                ForecastTab("Conditions") {
                                    DailyForecastRowConditions(daily, uiState.settings.units)
                                },
                                ForecastTab("Wind") {
                                    DailyForecastRowWind(daily, uiState.settings.units)
                                }
                            )
                        )
                    } else {
                        Text(
                            text = "No Daily Forecast available - Something went wrong.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }


                    Spacer(Modifier.height(MaterialTheme.dimens.spacing3))


                    val hourly = uiState.weather?.hourly
                    if (hourly != null) {
                        ForecastBox(
                            title = "Hourly forecast",
                            tabs = listOf(
                                ForecastTab("Conditions") {
                                    HourlyForecastRowConditions(
                                        hourly,
                                        uiState.settings.units,
                                    )
                                },
                                ForecastTab("Wind") {
                                    HourlyForecastRowWind(
                                        hourly,
                                        uiState.settings.units,
                                    )
                                }
                            )
                        )
                    } else {
                        Text(
                            text = "No Hourly Forecast available - Something went wrong.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                else -> {
                    Text(
                        text = "Something went wrong",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // TODO: Handle erroneous states / lack of permissions somehow
            }

        }
    }
}