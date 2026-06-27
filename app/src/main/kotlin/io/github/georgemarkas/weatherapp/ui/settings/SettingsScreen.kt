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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.georgemarkas.weatherapp.R
import io.github.georgemarkas.weatherapp.settings.models.Units
import io.github.georgemarkas.weatherapp.settings.models.UpdateInterval
import io.github.georgemarkas.weatherapp.ui.settings.composables.LocationSearchBar
import io.github.georgemarkas.weatherapp.ui.settings.composables.PopupDialog
import io.github.georgemarkas.weatherapp.ui.settings.composables.SettingsGroupCard
import io.github.georgemarkas.weatherapp.ui.settings.composables.SettingsItem
import androidx.compose.ui.res.vectorResource

@Suppress("AssignedValueIsNeverRead")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = uiState.settings
    val context = LocalContext.current

    var specifiedLocationToggle by rememberSaveable(settings.specifiedLocation) {
        mutableStateOf(settings.specifiedLocation)
    }

    var showUnitDialog by remember { mutableStateOf(false) }
    var showUpdateIntervalDialog by remember { mutableStateOf(false) }
    var showLocationPreferenceDialog by remember { mutableStateOf(false) }
    var showWeatherAlertsDialog by remember { mutableStateOf(false) }
    var showLocationPreferenceFieldDialog by remember { mutableStateOf(false) }

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
                label = stringResource(R.string.setting_location_source_popup_current),
                selected = !specifiedLocationToggle,
                onSelect = { viewModel.setSpecificLocationEnabled(false) }
            )
            RadioRow(
                label = stringResource(R.string.setting_location_source_popup_custom),
                selected = specifiedLocationToggle,
                onSelect = { viewModel.setSpecificLocationEnabled(true) }
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

                LocationSearchBar(
                    viewModel,
                    uiState
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_header)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.arrow_back_24px),
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
                        stringResource(R.string.setting_units),
                        stringResource(R.string.setting_units_description),
                        ImageVector.vectorResource(R.drawable.measuring_tape_24px),
                        onClick = { showUnitDialog = true }
                    ),
                    SettingsItem(
                        stringResource(R.string.setting_update_interval),
                        stringResource(R.string.setting_update_interval_description),
                        ImageVector.vectorResource(R.drawable.refresh_24px),
                        onClick = { showUpdateIntervalDialog = true }
                    ),
                    SettingsItem(
                        stringResource(R.string.setting_weather_alerts),
                        stringResource(R.string.setting_weather_alerts_description),
                        ImageVector.vectorResource(R.drawable.notifications_24px),
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
                        stringResource(R.string.setting_location_source),
                        stringResource(R.string.setting_location_source_description),
                        ImageVector.vectorResource(R.drawable.location_on_24px),
                        onClick = { showLocationPreferenceDialog = true }
                    ),
                    SettingsItem(
                        if (uiState.specifiedLocality == null) {
                            "No location specified"
                        } else {
                            listOfNotNull(
                                uiState.countryCode,
                                uiState.specifiedLocality,
                                uiState.admin1
                            )
                                .filter { it.isNotBlank() }
                                .joinToString(", ")
                        },
                        null,
                        ImageVector.vectorResource(R.drawable.edit_location_alt_24px),
                        isActive = specifiedLocationToggle,
                        onClick = { showLocationPreferenceFieldDialog = true }
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