package io.github.georgemarkas.weatherapp.notifications

import android.Manifest
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.github.georgemarkas.weatherapp.openmeteo.models.forecast.WeatherCurrent
import io.github.georgemarkas.weatherapp.openmeteo.models.forecast.WeatherResponse
import io.github.georgemarkas.weatherapp.settings.models.Units
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import kotlin.time.Clock

@RunWith(RobolectricTestRunner::class)
class NotificationsTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    private val notificationManager: NotificationManager
        get() = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private fun now() = Clock.System.now().epochSeconds

    private fun grantNotificationPermission() {
        shadowOf(ApplicationProvider.getApplicationContext<Application>())
            .grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
    }

    // Faking an extreme heat alert
    private fun severeWeather() = WeatherResponse(
        current = WeatherCurrent(
            temperature = 41.0, // Severe heat threshold  is 40 degrees
            weatherCode = null,
            relativeHumidity = null,
            windSpeed = null,
            windDirection = null,
            cloudCover = null,
            time = now()
        )
    )

    private fun calmWeather() = WeatherResponse(
        current = WeatherCurrent(
            temperature = 20.0,
            weatherCode = 1,
            relativeHumidity = null,
            windSpeed = 5.0,
            windDirection = null,
            cloudCover = null,
            time = now(),
        )
    )

    @Before
    fun setUp() {
        Notifications.createNotificationChannels(context)
        grantNotificationPermission()
    }

    @Test
    fun sendAlertNotification_postsAlert_whenWeatherSevere() {
        Notifications.sendAlertNotification(context, severeWeather(), Units.METRIC)

        val shadow = shadowOf(notificationManager)
        assertEquals(1, shadow.size())
        assertNotNull(shadow.getNotification(Notifications.ID_ALERT))
    }

    @Test
    fun sendAlertNotification_clearsAlert_whenNoAlertsRemain() {
        Notifications.sendAlertNotification(context, severeWeather(), Units.METRIC)
        assertEquals(1, shadowOf(notificationManager).size())

        Notifications.sendAlertNotification(context, calmWeather(), Units.METRIC)
        assertNull(shadowOf(notificationManager).getNotification(Notifications.ID_ALERT))
        assertEquals(0, shadowOf(notificationManager).size())
    }

    @Test
    fun sendAlertNotification_postsNothing_whenWeatherCalm() {
        Notifications.sendAlertNotification(context, calmWeather(), Units.METRIC)
        assertEquals(0, shadowOf(notificationManager).size())
    }

    @Test
    fun sendAlertNotification_doesNotPost_whenPermissionDenied() {
        shadowOf(ApplicationProvider.getApplicationContext<Application>())
            .denyPermissions(Manifest.permission.POST_NOTIFICATIONS)

        Notifications.sendAlertNotification(context, severeWeather(), Units.METRIC)
        assertEquals(0, shadowOf(notificationManager).size())
    }
}