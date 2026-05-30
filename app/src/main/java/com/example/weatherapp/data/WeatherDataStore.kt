package com.example.weatherapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

data class CachedWeather(
    val temperature: Double = 0.0,
    val weatherCode: Int = 0,
    val isDay: Boolean = true,
    val temperatureMax: Double = 0.0,
    val temperatureMin: Double = 0.0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val lastUpdatedMs: Long = 0L
)

object CachedWeatherSerializer : Serializer<CachedWeather?> {
    private val gson = Gson()

    override val defaultValue: CachedWeather? = null

    override suspend fun readFrom(input: InputStream): CachedWeather? = try {
        input.bufferedReader().use {
            gson.fromJson(it.readText(), CachedWeather::class.java)
        }
    } catch (e: JsonSyntaxException) {
        Timber.w(e, "Attempted to read malformed JSON")
        null
    }

    override suspend fun writeTo(t: CachedWeather?, output: OutputStream) {
        output.bufferedWriter().use {
            it.write(gson.toJson(t))
        }
    }
}

private val Context.weatherDataStore: DataStore<CachedWeather?> by dataStore(
    fileName = "weather.json",
    serializer = CachedWeatherSerializer
)

@Singleton
class WeatherDataStore @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    val cachedWeather: Flow<CachedWeather?> = context.weatherDataStore.data
        .catch { emit(null) }

    suspend fun save(result: WeatherFetchResult.Success) {
        val current = result.weather.current
        val daily = result.weather.daily

        context.weatherDataStore.updateData {
            CachedWeather(
                temperature = current.temperature,
                weatherCode = current.weatherCode,
                isDay = current.isDay == 1,
                temperatureMax = daily.temperatureMax.firstOrNull() ?: current.temperature,
                temperatureMin = daily.temperatureMin.firstOrNull() ?: current.temperature,
                latitude = result.latitude,
                longitude = result.longitude,
                lastUpdatedMs = System.currentTimeMillis()
            )
        }
    }
}