package io.github.georgemarkas.weatherapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.georgemarkas.weatherapp.R
import io.github.georgemarkas.weatherapp.ui.settings.SettingsScreen
import io.github.georgemarkas.weatherapp.ui.weather.WeatherLayout
import io.github.georgemarkas.weatherapp.ui.weather.WeatherViewModel

@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = hiltViewModel()
) {
    var showSettings by rememberSaveable { mutableStateOf(false) }

    if (showSettings) {
        SettingsScreen(onBack = { showSettings = false }, viewModel = viewModel)
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        WeatherLayout(viewModel = viewModel)

        IconButton(
            onClick = { showSettings = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(8.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_settings),
                contentDescription = "Settings"
            )
        }
    }
}