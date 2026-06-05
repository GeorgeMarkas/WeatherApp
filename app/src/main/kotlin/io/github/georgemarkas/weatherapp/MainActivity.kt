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
import io.github.georgemarkas.weatherapp.data.LocationDataStore
import io.github.georgemarkas.weatherapp.data.WeatherDataStore
import io.github.georgemarkas.weatherapp.ui.theme.WeatherAppTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
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
                    DemoLayout(modifier = Modifier.padding((innerPadding)))
                }
            }
        }
    }
}

@HiltViewModel
class DemoViewModel @Inject constructor(
    private val locationDataStore: LocationDataStore,
    private val weatherDataStore: WeatherDataStore
) : ViewModel() {

    val weather = weatherDataStore.weatherFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

    init {
        viewModelScope.launch {
            locationDataStore.updateLocation()
            val location = locationDataStore.locationFlow.first()
            weatherDataStore.updateWeather(location)
        }
    }
}

@Composable
fun DemoLayout(
    modifier: Modifier = Modifier,
    viewModel: DemoViewModel = hiltViewModel()
) {
    val weather by viewModel.weather.collectAsStateWithLifecycle()
    val format = remember { Json { prettyPrint = true } }

    val text: String = when (val result = weather) {

        null -> {
            "Loading..."
        }

        else -> {
            if (result.error == true) {
                val reason = result.reason ?: "Unknown error; OpenMeteo provided no reason"
                Timber.d("Response contained error: ${result.reason}")
                reason
            } else {
                format.encodeToString(result)
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