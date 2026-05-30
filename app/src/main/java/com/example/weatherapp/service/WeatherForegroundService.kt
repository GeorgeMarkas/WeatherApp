package com.example.weatherapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.weatherapp.MainActivity
import com.example.weatherapp.data.WeatherDataStore
import com.example.weatherapp.data.WeatherFetchResult
import com.example.weatherapp.data.WeatherRepository
import com.example.weatherapp.R
import com.example.weatherapp.data.CachedWeather
import com.example.weatherapp.openmeteo.weatherCodeToDescription
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

@AndroidEntryPoint
class WeatherForegroundService : Service() {

    @Inject
    lateinit var repository: WeatherRepository

    @Inject
    lateinit var dataStore: WeatherDataStore

    @Inject
    lateinit var notificationManager: NotificationManager

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    private var update_interval = 15.minutes // TODO: Make this a user setting

    private var isRunning = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopSelf()
            else -> if (!isRunning) {
                isRunning = true
                start()
            }
        }

        return START_STICKY
    }

    private fun start() {
        startForeground(NOTIFICATION_ID_STATUS, buildStatusNotification(cached = null))

        serviceScope.launch {
            val cached = dataStore.cachedWeather.first()
            if (cached != null) updateStatusNotification(cached)

            while (true) {
                fetchAndNotify()
                delay(update_interval)
            }
        }
    }

    private suspend fun fetchAndNotify() {
        when (val result = repository.fetchWeather()) {
            is WeatherFetchResult.Success -> {
                dataStore.save(result)
                val cached = dataStore.cachedWeather.first() ?: return
                updateStatusNotification(cached)
                // TODO: Check for alerts
            }

            is WeatherFetchResult.Error -> {
                Timber.w("Failed to fetch weather update: ${result.message}")
            }

            WeatherFetchResult.NoLocation -> {
                Timber.w("Location unavailable")
            }
        }
    }

    private fun createNotificationChannel() {
        val statusChannel = NotificationChannel(
            CHANNEL_ID_STATUS,
            "Weather Status",
            NotificationManager.IMPORTANCE_LOW
        )

        notificationManager.createNotificationChannel(statusChannel)
    }

    private fun buildStatusNotification(cached: CachedWeather?): Notification {
        val tapIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val content = if (cached != null) {
            "${cached.temperature} degrees, ${weatherCodeToDescription(cached.weatherCode).lowercase()}"
        } else {
            "Fetching weather..."
        }

        return NotificationCompat.Builder(this, CHANNEL_ID_STATUS)
            .setContentTitle("Weather")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(tapIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateStatusNotification(weather: CachedWeather) {
        notificationManager.notify(NOTIFICATION_ID_STATUS, buildStatusNotification(weather))
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_STOP = "STOP"
        private const val CHANNEL_ID_STATUS = "weather_status"
        private const val NOTIFICATION_ID_STATUS = 1

        fun start(context: Context) {
            context.startForegroundService(Intent(context, WeatherForegroundService::class.java))
        }

        fun stop(context: Context) {
            context.startService(
                Intent(context, WeatherForegroundService::class.java)
                    .setAction(ACTION_STOP)
            )
        }
    }
}