package io.github.georgemarkas.weatherapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import io.github.georgemarkas.weatherapp.R
import javax.inject.Inject

object Notifications {

    private const val CHANNEL_ALERT = "alert"
    private const val ID_ALERT = 0

    @Inject
    lateinit var notificationManager: NotificationManager

    fun createChannels(context: Context) {
        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ALERT,
                context.getString(R.string.notification_channel_alerts),
                NotificationManager.IMPORTANCE_HIGH
            )
        )
    }
}