package io.github.georgemarkas.weatherapp.alerts

import io.github.georgemarkas.weatherapp.alerts.models.Alert
import io.github.georgemarkas.weatherapp.alerts.models.AlertSeverity
import io.github.georgemarkas.weatherapp.alerts.models.AlertType
import io.github.georgemarkas.weatherapp.openmeteo.models.forecast.WeatherCurrent
import io.github.georgemarkas.weatherapp.openmeteo.models.forecast.WeatherDaily
import io.github.georgemarkas.weatherapp.openmeteo.models.forecast.WeatherHourly
import io.github.georgemarkas.weatherapp.openmeteo.models.forecast.WeatherResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.collections.listOf
import kotlin.time.Clock

class AlertsTest {

    private fun current(
        temperature: Double? = null,
        weatherCode: Int? = null,
        windSpeed: Double? = null,
        time: Long = 0L,
    ) = WeatherCurrent(
        temperature = temperature,
        weatherCode = weatherCode,
        relativeHumidity = null,
        windSpeed = windSpeed,
        cloudCover = null,
        time = time,
    )

    private fun hourly(
        time: List<Long>,
        temperature: List<Double?>? = null,
        weatherCode: List<Int?>? = null,
        windSpeed: List<Double?>? = null,
    ) = WeatherHourly(
        temperature = temperature,
        weatherCode = weatherCode,
        relativeHumidity = null,
        windSpeed = windSpeed,
        cloudCover = null,
        precipitationProbability = null,
        isDay = null,
        time = time,
    )

    private fun daily(
        time: List<Long>,
        temperatureMax: List<Double?>? = null,
        temperatureMin: List<Double?>? = null,
    ) = WeatherDaily(
        temperatureMax = temperatureMax,
        temperatureMin = temperatureMin,
        relativeHumidityMean = null,
        cloudCoverMean = null,
        time = time
    )

    private fun checkCurrent(current: WeatherCurrent) =
        Alerts.checkForAlerts(WeatherResponse(current = current))

    private fun checkHourly(hourly: WeatherHourly) =
        Alerts.checkForAlerts(WeatherResponse(hourly = hourly))

    private fun checkDaily(daily: WeatherDaily) =
        Alerts.checkForAlerts(WeatherResponse(daily = daily))

    private fun List<Alert>.ofType(type: AlertType) = first { it.type == type }

    private fun now() = Clock.System.now().epochSeconds

    /*
     * checkForAlerts
     */
    @Test
    fun checkForAlerts_returnsEmpty_whenAllSourcesNull() {
        val alerts = Alerts.checkForAlerts(WeatherResponse())
        assertTrue(alerts.isEmpty())
    }

    @Test
    fun checkForAlerts_combinesSourcesAndKeepsWorstPerType() {
        val weather = WeatherResponse(
            current = current(temperature = 36.0, time = 100L),
            daily = daily(temperatureMax = listOf(41.0), time = listOf(200L)),
        )

        val alert = Alerts.checkForAlerts(weather).single()

        assertEquals(AlertType.EXTREME_HEAT, alert.type)
        assertEquals(AlertSeverity.SEVERE, alert.severity)
        assertEquals(41.0, alert.value!!, 0.0)
    }

    /*
     * currentAlerts
     */
    @Test
    fun currentAlerts_producesAlertFromWeatherCode() {
        val alert = checkCurrent(current(weatherCode = 95, time = 100L)).single()

        assertEquals(AlertType.THUNDERSTORM, alert.type)
        assertEquals(AlertSeverity.WARNING, alert.severity)
        assertNull(alert.value)
    }

    @Test
    fun currentAlerts_procudesAlertFromWindSpeed() {
        val alert = checkCurrent(current(windSpeed = 65.0, time = 100L)).single()

        assertEquals(AlertType.HIGH_WIND, alert.type)
        assertEquals(AlertSeverity.WARNING, alert.severity)
        assertEquals(65.0, alert.value!!, 0.0)
    }

    @Test
    fun currentAlerts_producesAlertFromHighTemperature() {
        val alert = checkCurrent(current(temperature = 36.0, time = 100L)).single()

        assertEquals(AlertType.EXTREME_HEAT, alert.type)
        assertEquals(AlertSeverity.WARNING, alert.severity)
        assertEquals(36.0, alert.value!!, 0.0)
    }

    @Test
    fun currentAlerts_producesAlertFromLowTemperature() {
        val alert = checkCurrent(current(temperature = -12.0, time = 100L)).single()

        assertEquals(AlertType.EXTREME_COLD, alert.type)
        assertEquals(AlertSeverity.WARNING, alert.severity)
        assertEquals(-12.0, alert.value!!, 0.0)
    }

    @Test
    fun currentAlerts_returnsEmpty_whenNull() {
        val alerts = checkCurrent(current(time = 100L))
        assertTrue(alerts.isEmpty())
    }

    /*
     * hourlyAlerts
     */
    @Test
    fun hourlyAlerts_producesAlertFromWeatherCode() {
        val withinForecastWindow = now() + 3600

        val alert = checkHourly(
            hourly(time = listOf(withinForecastWindow), weatherCode = listOf(95))
        ).single()

        assertEquals(AlertType.THUNDERSTORM, alert.type)
        assertEquals(AlertSeverity.WARNING, alert.severity)
    }

    @Test
    fun hourlyAlerts_producesAlertFromWindSpeed() {
        val withinForecastWindow = now() + 3600

        val alert = checkHourly(
            hourly(time = listOf(withinForecastWindow), windSpeed = listOf(95.0))
        ).single()

        assertEquals(AlertType.HIGH_WIND, alert.type)
        assertEquals(AlertSeverity.SEVERE, alert.severity)
        assertEquals(95.0, alert.value!!, 0.0)
    }

    @Test
    fun hourlyAlerts_producesAlertFromHighTemperature() {
        val withinForecastWindow = now() + 3600

        val alert = checkHourly(
            hourly(time = listOf(withinForecastWindow), temperature = listOf(36.0))
        ).single()

        assertEquals(AlertType.EXTREME_HEAT, alert.type)
        assertEquals(AlertSeverity.WARNING, alert.severity)
        assertEquals(36.0, alert.value!!, 0.0)
    }

    @Test
    fun hourlyAlerts_producesAlertFromLowTemperature() {
        val withinForecastWindow = now() + 3600

        val alert = checkHourly(
            hourly(time = listOf(withinForecastWindow), temperature = listOf(-12.0))
        ).single()

        assertEquals(AlertType.EXTREME_COLD, alert.type)
        assertEquals(AlertSeverity.WARNING, alert.severity)
        assertEquals(-12.0, alert.value!!, 0.0)
    }

    @Test
    fun hourlyAlerts_ignoresWeatherDataOutsideForecastWindow() {
        val alerts = checkHourly(
            hourly(
                time = listOf(now() - 3600, now() + 24 * 3600 + 3600),
                weatherCode = listOf(95, 95)
            )
        )

        assertTrue(alerts.isEmpty())
    }

    @Test
    fun hourlyAlerts_returnsEmpty_whenNull() {
        val withinForecastWindow = now() + 3600
        val alerts = checkHourly(hourly(time = listOf(withinForecastWindow)))
        assertTrue(alerts.isEmpty())
    }

    /*
     * dailyAlerts
     */

    // TODO: Implement
}