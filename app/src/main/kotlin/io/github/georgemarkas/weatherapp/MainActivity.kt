package io.github.georgemarkas.weatherapp

import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.georgemarkas.weatherapp.data.WeatherRepository
import io.github.georgemarkas.weatherapp.openmeteo.models.WeatherResponse
import io.github.georgemarkas.weatherapp.ui.theme.WeatherAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        StrictMode.setVmPolicy(
            VmPolicy.Builder(StrictMode.getVmPolicy())
                .detectLeakedClosableObjects()
                .build()
        )

        enableEdgeToEdge()
        setContent {
            WeatherAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    JsonDemoLayout(modifier = Modifier.padding((innerPadding)))
                }
            }
        }
    }
}

@HiltViewModel
class JsonDemoViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {
    private val _jsonResponse = MutableStateFlow<Result<WeatherResponse>?>(null)
    val jsonResponse: StateFlow<Result<WeatherResponse>?> = _jsonResponse.asStateFlow()

    init {
        fetchWeather()
    }

    fun fetchWeather() {
        viewModelScope.launch(Dispatchers.IO) {
            _jsonResponse.value = repository.getWeather()
        }
    }
}

@Composable
fun JsonDemoLayout(
    modifier: Modifier = Modifier,
    viewModel: JsonDemoViewModel = hiltViewModel()
) {
    val jsonResponse by viewModel.jsonResponse.collectAsStateWithLifecycle()
    val format = remember { Json { prettyPrint = true } }

    lateinit var text: String
    when (val result = jsonResponse) {
        null -> {
            text = "Loading..."
        }

        else -> {
            result.onSuccess { response ->
                text = if (response.error == true) {
                    val reason = response.reason ?: "Unknown error; OpenMeteo provided no reason"
                    Timber.d("Response contained error: ${response.reason}")
                    reason
                } else {
                    format.encodeToString(response)
                }
            }
                .onFailure { e ->
                    text = "An exception occurred: ${e.message}"
                }
        }
    }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        Text(
            text = text,
            modifier = modifier
        )
    }
}