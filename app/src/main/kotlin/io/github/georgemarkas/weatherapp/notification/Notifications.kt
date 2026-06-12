package io.github.georgemarkas.weatherapp.notification

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_DEFAULT
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_HIGH
import io.github.georgemarkas.weatherapp.R

object Notifications {
    private const val GROUP_WEATHER_APP = "group_weather_app"

    private const val CHANNEL_ALERT = "alert"
    private const val ID_ALERT = 0

    private const val CHANNEL_FORECAST = "forecast"
    private const val ID_FORECAST = 1

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

                NotificationChannelCompat.Builder(CHANNEL_FORECAST, IMPORTANCE_DEFAULT)
                    .setName(context.getString(R.string.notification_channel_forecast))
                    .setGroup(GROUP_WEATHER_APP)
                    .build()
            )
        )
    }
}