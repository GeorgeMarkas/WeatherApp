package io.github.georgemarkas.weatherapp.data.weather

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.georgemarkas.weatherapp.openmeteo.models.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

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

val Context.weatherDataStore: DataStore<WeatherResponse?> by dataStore(
    fileName = "cached_weather_response.json",
    serializer = WeatherResponseSerializer
)

class WeatherDataStore @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    suspend fun cacheWeather(response: WeatherResponse) {
        context.weatherDataStore.updateData { response }
        Timber.i("Cached WeatherResponse")
    }

}