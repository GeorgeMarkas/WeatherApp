package io.github.georgemarkas.weatherapp.openmeteo

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

    private val forecastApi: ForecastApi = Retrofit.Builder()
        .baseUrl(openMeteoApiUrl)
        .client(client)
        .addConverterFactory(
            Json.asConverterFactory("application/json".toMediaType())
        )
        .build()
        .create(ForecastApi::class.java)
}