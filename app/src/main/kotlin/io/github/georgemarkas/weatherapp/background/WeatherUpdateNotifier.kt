package io.github.georgemarkas.weatherapp.background

import android.content.Context
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.georgemarkas.weatherapp.R
import io.github.georgemarkas.weatherapp.notifications.Notifications
import javax.inject.Inject
import kotlin.getValue

class WeatherUpdateNotifier @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    val progressNotificationBuilder by lazy {
        NotificationCompat.Builder(context, Notifications.CHANNEL_BACKGROUND)
            .setContentTitle(context.getString(R.string.app_name))
            .setSmallIcon(R.drawable.sync_24px)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(0, 0, true)
    }

    // TODO: Implement update error notification
}