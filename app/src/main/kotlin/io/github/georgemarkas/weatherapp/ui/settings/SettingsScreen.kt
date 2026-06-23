package io.github.georgemarkas.weatherapp.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.georgemarkas.weatherapp.settings.models.Units
import io.github.georgemarkas.weatherapp.settings.models.UpdateInterval
import io.github.georgemarkas.weatherapp.ui.settings.composables.PopupDialog
import io.github.georgemarkas.weatherapp.ui.settings.composables.SettingsGroupCard
import io.github.georgemarkas.weatherapp.ui.settings.composables.SettingsItem
import io.github.georgemarkas.weatherapp.ui.weather.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: WeatherViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = uiState.settings
    val context = LocalContext.current

    var locationPreferenceSpecific  by rememberSaveable(settings.specificLocation) {
        mutableStateOf(settings.specificLocation)
    }

    var showUnitDialog                      by remember { mutableStateOf(false) }
    var showUpdateIntervalDialog            by remember { mutableStateOf(false) }
    var showLocationPreferenceDialog        by remember { mutableStateOf(false) }
    var showWeatherAlertsDialog             by remember { mutableStateOf(false) }
    var showLocationPreferenceFieldDialog   by remember { mutableStateOf(false) }

    if (showUnitDialog) {
        PopupDialog(onDismissRequest = { showUnitDialog = false }) {
            Units.entries.forEach { unit ->
                RadioRow(
                    label = stringResource(unit.labelRes),
                    selected = settings.units == unit,
                    onSelect = { viewModel.setUnits(unit) }
                )
            }
        }
    }
    if (showUpdateIntervalDialog) {
        PopupDialog(onDismissRequest = { showUpdateIntervalDialog = false }) {
            UpdateInterval.entries.forEach { interval ->
                RadioRow(
                    label = stringResource(interval.labelRes),
                    selected = settings.updateInterval == interval,
                    onSelect = { viewModel.setUpdateInterval(interval, context) }
                )
            }
        }
    }
    if (showLocationPreferenceDialog) {
        PopupDialog(onDismissRequest = { showLocationPreferenceDialog = false }) {
            RadioRow(
                label = "Use Current Location",
                selected = !locationPreferenceSpecific,
                onSelect = { locationPreferenceSpecific = false }
            )
            RadioRow(
                label = "Use Specified Location",
                selected = locationPreferenceSpecific,
                onSelect = { locationPreferenceSpecific = true }
            )
        }
    }
    if (showWeatherAlertsDialog) {
        PopupDialog(onDismissRequest = { showWeatherAlertsDialog = false }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Weather alerts", modifier = Modifier.weight(1f))
                Switch(
                    checked = settings.weatherAlerts,
                    onCheckedChange = { viewModel.setWeatherAlerts(it) }
                )
            }
        }
    }
    if (showLocationPreferenceFieldDialog) {
        PopupDialog(onDismissRequest = { showLocationPreferenceFieldDialog = false }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    state = rememberTextFieldState(),
                    label = { Text("Location Preference") },
                    modifier = Modifier
                        .padding(16.dp)
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            SettingsGroupCard(
                items = listOf(
                    SettingsItem(
                        "Unit type",
                        "Metric/Imperial",
                        Icons.Default.Build,
                        onClick = { showUnitDialog = true}
                    ),
                    SettingsItem(
                        "Background Updates",
                        "Update interval",
                        Icons.Default.Refresh,
                        onClick = { showUpdateIntervalDialog = true }
                    ),
                    SettingsItem(
                        "Weather Alerts",
                        "Enable/Disable Alerts",
                        Icons.Default.Notifications,
                        composable = {
                            Switch(
                                checked = settings.weatherAlerts,
                                onCheckedChange = { viewModel.setWeatherAlerts(it) }
                            )
                        }
                    )
                )
            )

            Spacer(modifier = Modifier.height(25.dp))

            SettingsGroupCard(
                items = listOf(
                    SettingsItem(
                        "Location Preferences",
                        "Use Current/Specify Location",
                        Icons.Default.Place,
                        onClick = { showLocationPreferenceDialog = true }
                    ),
                    SettingsItem(
                        "Specify Location...",
                        "Custom Location Field",
                        Icons.Default.Notifications,
                        isActive = locationPreferenceSpecific,
                        onClick = {showLocationPreferenceFieldDialog = true}
                    )
                )
            )

        }
    }
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(16.dp))
        Text(label)
    }
}