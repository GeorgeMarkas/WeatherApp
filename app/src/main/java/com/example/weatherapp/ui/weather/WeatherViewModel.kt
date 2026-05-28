package com.example.weatherapp.ui.weather

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.CachedWeather
import com.example.weatherapp.data.WeatherDataStore
import com.example.weatherapp.data.WeatherFetchResult
import com.example.weatherapp.data.WeatherRepository
import com.example.weatherapp.worker.WeatherUpdateWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    object PermissionDenied : WeatherUiState()
    object NoLocation : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
    data class Success(val weather: CachedWeather) : WeatherUiState()
}

@HiltViewModel
class WeatherViewModel @Inject constructor(
    application: Application,
    private val repository: WeatherRepository,
    private val dataStore: WeatherDataStore
) : AndroidViewModel(application) {

    val uiState: StateFlow<WeatherUiState> = dataStore.cachedWeather
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        ).let { flow ->
            MutableStateFlow<WeatherUiState>(WeatherUiState.Loading).also { state ->
                viewModelScope.launch {
                    flow.collect { cached ->
                        if (cached != null) state.value = WeatherUiState.Success(cached)
                    }
                }
            }
        }

    private val _uiState = uiState as MutableStateFlow<WeatherUiState>

    fun onPermissionResult(granted: Boolean) {
        if (!granted) {
            WeatherUpdateWorker.cancel(getApplication())
            _uiState.value = WeatherUiState.PermissionDenied
            return
        }

        WeatherUpdateWorker.enqueue(getApplication())

        if (_uiState.value is WeatherUiState.Success) return

        viewModelScope.launch {
            if (dataStore.cachedWeather.first() != null) return@launch

            _uiState.value = WeatherUiState.Loading

            when (val result = repository.fetchWeather()) {
                is WeatherFetchResult.Success -> dataStore.save(result)
                is WeatherFetchResult.Error   -> _uiState.value = WeatherUiState.Error(result.message)
                WeatherFetchResult.NoLocation -> _uiState.value = WeatherUiState.NoLocation
            }
        }
    }
}