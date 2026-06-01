package io.github.georgemarkas.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.georgemarkas.weatherapp.data.WeatherRepository
import io.github.georgemarkas.weatherapp.openmeteo.models.WeatherResult
import io.github.georgemarkas.weatherapp.ui.theme.WeatherAppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.toString

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())
        enableEdgeToEdge()
        setContent {
            WeatherAppTheme {
                DemoLayout()
            }
        }
    }
}

@HiltViewModel
class DemoViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {
    private val _jsonResponse = MutableStateFlow<Result<WeatherResult>?>(null)
    val jsonResponse: StateFlow<Result<WeatherResult>?> = _jsonResponse.asStateFlow()

    init {
        fetchWeather()
    }

    fun fetchWeather() {
        viewModelScope.launch {
            _jsonResponse.value = repository.getWeather()
        }
    }
}

@Composable
fun DemoLayout(viewModel: DemoViewModel = hiltViewModel()) {
    val jsonResponse by viewModel.jsonResponse.collectAsStateWithLifecycle()

    Timber.d(jsonResponse.toString())

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        Text(text = jsonResponse.toString())
    }
}