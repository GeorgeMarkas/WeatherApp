package io.github.georgemarkas.weatherapp.openmeteo

import io.github.georgemarkas.weatherapp.openmeteo.models.WeatherCurrent
import io.github.georgemarkas.weatherapp.openmeteo.models.WeatherDaily
import io.github.georgemarkas.weatherapp.openmeteo.models.WeatherHourly
import io.github.georgemarkas.weatherapp.openmeteo.models.WeatherResult
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Inject

class OpenMeteoService @Inject constructor(
    private val client: OkHttpClient,
) {
    private val openMeteoApiUrl = "https://api.open-meteo.com/"

    private val json = Json { ignoreUnknownKeys = true }

    private val forecastApiImpl: OpenMeteoForecastApi = Retrofit.Builder()
        .baseUrl(openMeteoApiUrl)
        .client(client)
        .addConverterFactory(
            json.asConverterFactory("application/json".toMediaType())
        )
        .build()
        .create(OpenMeteoForecastApi::class.java)

    suspend fun requestWeather(): WeatherResult {
        val current = stringifySerialNames(WeatherCurrent.serializer().descriptor)
        val hourly = stringifySerialNames(WeatherHourly.serializer().descriptor)
        val daily = stringifySerialNames(WeatherDaily.serializer().descriptor)

        return forecastApiImpl.getWeather(
            // TODO: Dummy values, swap out with location service ones
            37.9838,
            23.7275,
            current,
            hourly,
            daily,
            7 // TODO: Have this be an adjustable setting
        )
    }

    private fun stringifySerialNames(descriptor: SerialDescriptor): String =
        Array(descriptor.elementsCount) { descriptor.getElementName(it) }
            .filter { it != "time" } // This is returned automatically
            .joinToString(",")
}