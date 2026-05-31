package io.github.georgemarkas.weatherapp.openmeteo

import okhttp3.OkHttpClient
import javax.inject.Inject

class OpenMeteoService @Inject constructor(
    private val client: OkHttpClient
) {
}