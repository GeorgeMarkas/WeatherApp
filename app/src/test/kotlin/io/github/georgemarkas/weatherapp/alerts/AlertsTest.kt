package io.github.georgemarkas.weatherapp.alerts

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

    private fun now() = Clock.System.now().epochSeconds

    private fun checkCurrent(current: WeatherCurrent) =
        Alerts.checkForAlerts(WeatherResponse(current = current))

    private fun checkHourly(hourly: WeatherHourly) =
        Alerts.checkForAlerts(WeatherResponse(hourly = hourly))

    private fun checkDaily(daily: WeatherDaily) =
        Alerts.checkForAlerts(WeatherResponse(daily = daily))

    /*
     * checkForAlerts
     */
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

    @Test
    fun checkForAlerts_producesNoAlert_whenWeatherNotExtreme() {
        val weather = WeatherResponse(
            current = current(temperature = 20.0, time = 100L)
        )

        val alerts = Alerts.checkForAlerts(weather)
        assertTrue(alerts.isEmpty())
    }

    @Test
    fun checkForAlerts_returnsEmpty_whenAllSourcesNull() {
        val alerts = Alerts.checkForAlerts(WeatherResponse())
        assertTrue(alerts.isEmpty())
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
    fun currentAlerts_producesNoAlert_whenWeathrNotExtreme() {
        val alerts = checkCurrent(current(temperature = 20.0, time = 100L))
        assertTrue(alerts.isEmpty())
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
    fun hourlyAlerts_producesNoAlert_whenWeatherNotExtreme() {
        val withinForecastWindow = now() + 3600

        val alerts = checkHourly(
            hourly(time = listOf(withinForecastWindow), temperature = listOf(20.0))
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
    @Test
    fun dailyAlerts_producesAlertFromLowMinTemperature() {
        val alert = checkDaily(
            daily(time = listOf(100L), temperatureMin = listOf(-12.0))
        ).single()

        assertEquals(AlertType.EXTREME_COLD, alert.type)
        assertEquals(AlertSeverity.WARNING, alert.severity)
        assertEquals(-12.0, alert.value!!, 0.0)
    }

    @Test
    fun dailyAlerts_producesAlertFromHighMinTemperature() {
        val alert = checkDaily(
            daily(time = listOf(100L), temperatureMin = listOf(36.0))
        ).single()

        assertEquals(AlertType.EXTREME_HEAT, alert.type)
        assertEquals(AlertSeverity.WARNING, alert.severity)
        assertEquals(36.0, alert.value!!, 0.0)
    }

    @Test
    fun dailyAlerts_producesAlertFromLowMaxTemperature() {
        val alert = checkDaily(
            daily(time = listOf(100L), temperatureMin = listOf(-12.0))
        ).single()

        assertEquals(AlertType.EXTREME_COLD, alert.type)
        assertEquals(AlertSeverity.WARNING, alert.severity)
        assertEquals(-12.0, alert.value!!, 0.0)
    }

    @Test
    fun dailyAlerts_producesAlertFromHighMaxTemperature() {
        val alert = checkDaily(
            daily(time = listOf(100L), temperatureMin = listOf(36.0))
        ).single()

        assertEquals(AlertType.EXTREME_HEAT, alert.type)
        assertEquals(AlertSeverity.WARNING, alert.severity)
        assertEquals(36.0, alert.value!!, 0.0)
    }

    @Test
    fun dailyAlerts_skipsNullEntries() {
        val alert = checkDaily(
            daily(time = listOf(100L, 200L), temperatureMax = listOf(null, 36.0))
        ).single()

        assertEquals(AlertType.EXTREME_HEAT, alert.type)
        assertEquals(AlertSeverity.WARNING, alert.severity)
        assertEquals(36.0, alert.value!!, 0.0)
    }

    @Test
    fun dailyAlerts_procudesNoAlert_whenWeatherNotExtreme() {
        val alerts = checkDaily(
            daily(time = listOf(100L), temperatureMax = listOf(20.0), temperatureMin = listOf(5.0))
        )

        assertTrue(alerts.isEmpty())
    }

    /*
     * heatSeverity
     */
    private fun heatSeverityOf(temperature: Double): AlertSeverity? =
        checkCurrent(current(temperature = temperature, time = 100L))
            .firstOrNull { it.type == AlertType.EXTREME_HEAT }?.severity

    @Test
    fun heatSeverity_returnsNull_whenTemperatureBelowThreshold() =
        assertNull(heatSeverityOf(34.9))

    @Test
    fun heatSeverity_returnsSeverityWarning_whenTemperatureAtWarningThreshold() =
        assertEquals(AlertSeverity.WARNING, heatSeverityOf(35.0))

    @Test
    fun heatSeverity_returnsSeveritySevere_whenTemperatureAtSevereThreshold() =
        assertEquals(AlertSeverity.SEVERE, heatSeverityOf(40.0))

    /*
     * coldSeverity
     */
    private fun coldSeverityOf(temperature: Double): AlertSeverity? =
        checkCurrent(current(temperature = temperature, time = 100L))
            .firstOrNull { it.type == AlertType.EXTREME_COLD }?.severity

    @Test
    fun coldSeverity_returnsNull_whenTemperatureAboveThreshold() =
        assertNull(heatSeverityOf(-9.9))

    @Test
    fun coldSeverity_returnsSeverityWarning_whenTemperatureAtWarningThreshold() =
        assertEquals(AlertSeverity.WARNING, coldSeverityOf(-10.0))

    @Test
    fun coldSeverity_returnsSeveritySevere_whenTemperatureAtSevereThreshold() =
        assertEquals(AlertSeverity.SEVERE, coldSeverityOf(-20.0))

    /*
     * windSeverity
     */
    private fun windSeverityOf(windSpeed: Double): AlertSeverity? =
        checkCurrent(current(windSpeed = windSpeed, time = 100L))
            .firstOrNull { it.type == AlertType.HIGH_WIND }?.severity

    @Test
    fun windSeverity_returnsNull_whenTemperatureBelowThreshold() =
        assertNull(windSeverityOf(59.9))

    @Test
    fun windSeverity_returnsSeverityWarning_whenTemperatureAtWarningThreshold() =
        assertEquals(AlertSeverity.WARNING, windSeverityOf(89.9))

    @Test
    fun windSeverity_returnsSeveritySevere_whenTemperatureAtSevereThreshold() =
        assertEquals(AlertSeverity.SEVERE, windSeverityOf(90.0))

    /*
     * sieve
     */
    @Test
    fun sieve_keepsOneAlertPerType() {
        val alert = checkHourly(
            hourly(time = listOf(now() + 3600, now() + 7200), weatherCode = listOf(95, 95))
        ).single()

        assertEquals(AlertType.THUNDERSTORM, alert.type)
    }

    @Test
    fun sieve_keepsWorstSeverity() {
        val weather = WeatherResponse(
            current = current(weatherCode = 95, time = 100L),
            hourly = hourly(time = listOf(now() + 3600), weatherCode = listOf(96)),
        )

        val alert = Alerts.checkForAlerts(weather).single()
        assertEquals(AlertType.THUNDERSTORM, alert.type)
        assertEquals(AlertSeverity.SEVERE, alert.severity)
    }

    @Test
    fun sieve_keepsEarliest() {
        val alert = checkHourly(
            hourly(time = listOf(now() + 7200, now() + 3600), weatherCode = listOf(95, 95))
        ).single()

        assertEquals(now() + 3600, alert.startTime)
    }

    @Test
    fun sieve_sortsBySeverityThenTime() {
        val severityOrdered = checkCurrent(
            current(temperature = 36.0, windSpeed = 95.0, time = 100L)
        )

        assertEquals(2, severityOrdered.size)
        assertEquals(AlertType.HIGH_WIND, severityOrdered[0].type)
        assertEquals(AlertType.EXTREME_HEAT, severityOrdered[1].type)

        val timeOrdered = Alerts.checkForAlerts(
            WeatherResponse(
                current = current(weatherCode = 65, time = 200L),
                daily = daily(time = listOf(100L), temperatureMax = listOf(36.0)),
            )
        )

        assertEquals(2, timeOrdered.size)
        assertEquals(AlertType.EXTREME_HEAT, timeOrdered[0].type)
        assertEquals(AlertType.HEAVY_RAIN, timeOrdered[1].type)
    }
}
