package io.github.georgemarkas.weatherapp.data

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.georgemarkas.weatherapp.exceptions.WeatherException
import io.github.georgemarkas.weatherapp.location.LocationWrapper
import io.github.georgemarkas.weatherapp.openmeteo.OpenMeteoService
import io.github.georgemarkas.weatherapp.openmeteo.models.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

val Context.weatherDataStore: DataStore<WeatherResponse?> by dataStore(
    fileName = "weather.json",
    serializer = WeatherResponseSerializer
)

object WeatherResponseSerializer : Serializer<WeatherResponse?> {

    override val defaultValue: WeatherResponse? = null

    override suspend fun readFrom(input: InputStream): WeatherResponse? =
        try {
            Json.decodeFromString<WeatherResponse>(
                input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            throw CorruptionException("Unable to read WeatherResponse", e)
        }

    override suspend fun writeTo(t: WeatherResponse?, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(t)
                    .encodeToByteArray()
            )
        }
    }
}

class WeatherRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val service: OpenMeteoService
) {
    val weatherFlow: Flow<WeatherResponse?> = context.weatherDataStore.data

    suspend fun updateWeather(location: LocationWrapper?) {
        if (location == null) throw WeatherException("Provided location is null")
        val weather = service.requestWeather(location)

        weather.onSuccess { response ->
            if (response.error == null) {
                storeWeather(response)
                Timber.i("Updated weather")
            } else {
                val reason = response.reason ?: "Unknown error; OpenMeteo provided no reason"
                throw WeatherException("OpenMeteo's response contains an error: $reason")
            }
        }.onFailure { e ->
               throw WeatherException("Failed to update weather", e)
        }
    }

    private suspend fun storeWeather(weather: WeatherResponse) {
        context.weatherDataStore.updateData { weather }
        Timber.d("Stored weather in DataStore")
    }
}