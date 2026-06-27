package io.github.georgemarkas.weatherapp.ui.settings.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.georgemarkas.weatherapp.R
import io.github.georgemarkas.weatherapp.ui.settings.SettingsViewModel
import io.github.georgemarkas.weatherapp.ui.settings.SettingsUiState
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchBar(
    settingsViewModel: SettingsViewModel,
    uiState: SettingsUiState
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val suggestions = uiState.searchResults

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            @Suppress("AssignedValueIsNeverRead")
            expanded = it
        }
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { newValue ->
                val filtered = newValue
                    .filterNot { it == '\n' || it == '\r' }
                    .take(30)
                query = filtered
                if (filtered.isEmpty()) {
                    settingsViewModel.clearSearchResults()
                } else {
                    settingsViewModel.updateGeolocationResults(filtered)
                }
            },
            singleLine = true,
            label = { Text(stringResource(R.string.location_search_label)) },
            placeholder = { Text(stringResource(R.string.location_search_placeholder)) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                .fillMaxWidth(),
            trailingIcon = {
                if (uiState.isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .heightIn(max = 200.dp)
                .exposedDropdownSize()
        ) {

            if (suggestions.isNullOrEmpty()) {
                DropdownMenuItem(
                    text = { Text("No results") },
                    onClick = {},
                    enabled = false
                )
            } else {
                suggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                listOfNotNull(
                                    suggestion.countryCode,
                                    suggestion.name,
                                    suggestion.admin1
                                )
                                    .filter { it.isNotBlank() }
                                    .joinToString(", ")
                            )
                        },
                        onClick = {
                            settingsViewModel.extractAndSetChoice(suggestion)
                            query = suggestion.name ?: run {
                                Timber.e("Suggestion name is null")
                                "null"
                            }
                            expanded = false
                            settingsViewModel.clearSearchResults()
                        }
                    )
                }
            }
        }
    }
}