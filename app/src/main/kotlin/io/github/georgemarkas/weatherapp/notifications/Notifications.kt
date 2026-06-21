package io.github.georgemarkas.weatherapp.notifications

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_HIGH
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_MIN
import io.github.georgemarkas.weatherapp.R
import io.github.georgemarkas.weatherapp.alerts.AlertFormatter
import io.github.georgemarkas.weatherapp.alerts.Alerts
import io.github.georgemarkas.weatherapp.alerts.models.AlertSeverity
import io.github.georgemarkas.weatherapp.extensions.notify
import io.github.georgemarkas.weatherapp.settings.models.Units
import io.github.georgemarkas.weatherapp.openmeteo.models.forecast.WeatherResponse

object Notifications {
    private const val GROUP_WEATHER_APP = "group_weather_app"

    const val CHANNEL_ALERT = "alert"
    const val ID_ALERT = 0

    const val CHANNEL_BACKGROUND = "background"
    const val ID_BACKGROUND = 1

    fun createNotificationChannels(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)

        notificationManager.createNotificationChannelGroupsCompat(
            listOf(
                NotificationChannelGroupCompat.Builder(GROUP_WEATHER_APP)
                    .setName(context.getString(R.string.notification_channel_group))
                    .build()
            )
        )

        notificationManager.createNotificationChannelsCompat(
            listOf(
                NotificationChannelCompat.Builder(CHANNEL_ALERT, IMPORTANCE_HIGH)
                    .setName(context.getString(R.string.notification_channel_alert))
                    .setGroup(GROUP_WEATHER_APP)
                    .build(),

                NotificationChannelCompat.Builder(CHANNEL_BACKGROUND, IMPORTANCE_MIN)
                    .setName(context.getString(R.string.notification_channel_background))
                    .setGroup(GROUP_WEATHER_APP)
                    .setShowBadge(false)
                    .build()
            )
        )
    }

    /**
     * Sends a weather alert notification if the given weather data indicates
     * hazardous weather potential. Will also clear previous alerts if none are
     * present anymore.
     */
    fun sendAlertNotification(
        context: Context,
        weather: WeatherResponse,
        units: Units
    ) {
        val alerts = Alerts.checkForAlerts(weather)

        // If there are no alerts, clear any stale one left
        if (alerts.isEmpty()) {
            NotificationManagerCompat.from(context).cancel(ID_ALERT)
            return
        }

        val mostSevere = alerts.first()

        val title = "${AlertFormatter.severityLabel(context, mostSevere.severity)} - " +
                AlertFormatter.title(context, mostSevere.type)

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERT)
            .setContentTitle(title)
            .setContentText(AlertFormatter.message(context, mostSevere, units))
            .setSmallIcon(R.drawable.ic_notify_warning)
            .setPriority(mostSevere.severity.toPriority())
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .build()

        context.notify(ID_ALERT, notification)
    }

    private fun AlertSeverity.toPriority(): Int = when (this) {
        AlertSeverity.SEVERE -> NotificationCompat.PRIORITY_HIGH
        AlertSeverity.WARNING -> NotificationCompat.PRIORITY_DEFAULT
        AlertSeverity.ADVISORY -> NotificationCompat.PRIORITY_LOW
    }
}