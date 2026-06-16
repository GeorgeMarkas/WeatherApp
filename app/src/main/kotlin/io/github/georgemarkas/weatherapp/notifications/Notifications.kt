package io.github.georgemarkas.weatherapp.notifications

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_DEFAULT
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_HIGH
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_MIN
import io.github.georgemarkas.weatherapp.R
import io.github.georgemarkas.weatherapp.extensions.notify
import io.github.georgemarkas.weatherapp.openmeteo.OpenMeteoService
import io.github.georgemarkas.weatherapp.openmeteo.models.WeatherResponse

object Notifications {
    private const val GROUP_WEATHER_APP = "group_weather_app"

    const val CHANNEL_ALERT = "alert"
    const val ID_ALERT = 0

    const val CHANNEL_BACKGROUND = "background"
    const val ID_BACKGROUND = 1

    const val CHANNEL_FORECAST = "forecast"
    const val ID_FORECAST = 2

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
                    .build(),

                NotificationChannelCompat.Builder(CHANNEL_FORECAST, IMPORTANCE_DEFAULT)
                    .setName(context.getString(R.string.notification_channel_forecast))
                    .setGroup(GROUP_WEATHER_APP)
                    .build()
            )
        )
    }

    // TODO: Simple example, implement something a tad nicer
    fun sendForecastNotification(
        context: Context,
        weather: WeatherResponse
    ) {
        val weatherText = OpenMeteoService
            .getWeatherCodeDescription(context, weather.current?.weatherCode)

        val content = "${weatherText}, ${weather.current?.temperature}°C"

        val notification = NotificationCompat.Builder(context, CHANNEL_FORECAST)
            .setContentTitle(context.getString(R.string.app_name))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentText(content)
            .setOnlyAlertOnce(true)
            .build()

        context.notify(ID_FORECAST, notification)
    }
}