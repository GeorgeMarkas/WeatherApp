package io.github.georgemarkas.weatherapp.openmeteo

import android.content.Context
import io.github.georgemarkas.weatherapp.R
import io.github.georgemarkas.weatherapp.location.LocationWrapper
import io.github.georgemarkas.weatherapp.openmeteo.models.forecast.WeatherCurrent
import io.github.georgemarkas.weatherapp.openmeteo.models.forecast.WeatherDaily
import io.github.georgemarkas.weatherapp.openmeteo.models.forecast.WeatherHourly
import io.github.georgemarkas.weatherapp.openmeteo.models.forecast.WeatherResponse
import io.github.georgemarkas.weatherapp.openmeteo.models.geocoding.GeocodingResponse
import kotlinx.serialization.descriptors.SerialDescriptor
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import javax.inject.Inject

class OpenMeteoService @Inject constructor(
    private val client: OkHttpClient,
    converterFactory: Converter.Factory
) {
    private val forecastApiImpl: OpenMeteoForecastApi = Retrofit.Builder()
        .baseUrl(OPEN_METEO_FORECAST_API_URL)
        .client(client)
        .addConverterFactory(converterFactory)
        .build()
        .create(OpenMeteoForecastApi::class.java)

    private val geocodingApiImpl: OpenMeteoGeocodingApi = Retrofit.Builder()
        .baseUrl(OPEN_METEO_GEOCODING_API_URL)
        .client(client)
        .addConverterFactory(converterFactory)
        .build()
        .create(OpenMeteoGeocodingApi::class.java)

    /**
     * Fetches fully fledged weather information for the given location from OpenMeteo's
     * weather forecast API.
     */
    suspend fun requestWeather(location: LocationWrapper): Result<WeatherResponse> {
        val current = stringifySerialNames(WeatherCurrent.serializer().descriptor)
        val hourly = stringifySerialNames(WeatherHourly.serializer().descriptor)
        val daily = stringifySerialNames(WeatherDaily.serializer().descriptor)

        return runCatching {
            forecastApiImpl.getWeather(
                location.latitude,
                location.longitude,
                current,
                hourly,
                daily,
                7
            )
        }
    }

    /**
     * Fetches a list of matching locations for the given string query from OpenMeteo's
     * geocoding API.
     */
    suspend fun requestGeocodedLocations(query: String): Result<GeocodingResponse> {
        return runCatching { geocodingApiImpl.searchLocations(query) }
    }

    private fun stringifySerialNames(descriptor: SerialDescriptor): String =
        Array(descriptor.elementsCount) { descriptor.getElementName(it) }
            .filter { it != "time" } // This is returned automatically
            .joinToString(",")

    companion object {
        private const val OPEN_METEO_FORECAST_API_URL = "https://api.open-meteo.com/"
        private const val OPEN_METEO_GEOCODING_API_URL = "https://geocoding-api.open-meteo.com/"

        /**
         * Returns the description text for the given WMO 4677 weather code.
         */
        fun getWeatherCodeDescription(
            context: Context,
            code: Int?
        ): String? {
            return when (code) {
                null -> null
                0 -> context.getString(R.string.openmeteo_clear_sky)
                1 -> context.getString(R.string.openmeteo_mainly_clear)
                2 -> context.getString(R.string.openmeteo_partly_cloudy)
                3 -> context.getString(R.string.openmeteo_overcast)
                45 -> context.getString(R.string.openmeteo_fog)
                48 -> context.getString(R.string.openmeteo_depositing_rime_fog)
                51 -> context.getString(R.string.openmeteo_drizzle_light_intensity)
                53 -> context.getString(R.string.openmeteo_drizzle_moderate_intensity)
                55 -> context.getString(R.string.openmeteo_drizzle_dense_intensity)
                56 -> context.getString(R.string.openmeteo_freezing_drizzle_light_intensity)
                57 -> context.getString(R.string.openmeteo_freezing_drizzle_dense_intensity)
                61 -> context.getString(R.string.openmeteo_rain_slight_intensity)
                63 -> context.getString(R.string.openmeteo_rain_moderate_intensity)
                65 -> context.getString(R.string.openmeteo_rain_heavy_intensity)
                66 -> context.getString(R.string.openmeteo_freezing_rain_light_intensity)
                67 -> context.getString(R.string.openmeteo_freezing_rain_heavy_intensity)
                71 -> context.getString(R.string.openmeteo_snow_slight_intensity)
                73 -> context.getString(R.string.openmeteo_snow_moderate_intensity)
                75 -> context.getString(R.string.openmeteo_snow_heavy_intensity)
                77 -> context.getString(R.string.openmeteo_snow_grains)
                80 -> context.getString(R.string.openmeteo_rain_showers_slight)
                81 -> context.getString(R.string.openmeteo_rain_showers_moderate)
                82 -> context.getString(R.string.openmeteo_rain_showers_violent)
                85 -> context.getString(R.string.openmeteo_snow_showers_slight)
                86 -> context.getString(R.string.openmeteo_snow_showers_heavy)
                95 -> context.getString(R.string.openmeteo_thunderstorm_slight_or_moderate)
                96 -> context.getString(R.string.openmeteo_thunderstorm_with_slight_hail)
                99 -> context.getString(R.string.openmeteo_thunderstorm_with_heavy_hail)
                else -> throw IllegalArgumentException("Unknown weather code: $code")
            }
        }
    }
}