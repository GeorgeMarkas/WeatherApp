package io.github.georgemarkas.weatherapp.background

import android.content.Context
import android.content.pm.ServiceInfo
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.georgemarkas.weatherapp.data.LocationRepository
import io.github.georgemarkas.weatherapp.data.SettingsRepository
import io.github.georgemarkas.weatherapp.data.WeatherRepository
import io.github.georgemarkas.weatherapp.exceptions.LocationException
import io.github.georgemarkas.weatherapp.extensions.isOnline
import io.github.georgemarkas.weatherapp.extensions.isRunning
import io.github.georgemarkas.weatherapp.extensions.setForegroundSafely
import io.github.georgemarkas.weatherapp.extensions.workManager
import io.github.georgemarkas.weatherapp.notifications.Notifications
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class WeatherUpdateWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository,
    private val settingsRepository: SettingsRepository,
    private val notifier: WeatherUpdateNotifier
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (tags.contains(WORK_NAME_PERIODIC) && context.workManager.isRunning(WORK_NAME_ONE_SHOT))
            return Result.retry()

        if (!context.isOnline()) {
            Timber.w("No connection, retrying")
            return Result.retry()
        }

        setForegroundSafely()

        return withContext(Dispatchers.IO) {
            try {
                // TODO: Use user-picked location instead of current location
                //  if such a setting has been specified
                updateWeather(locationRepository, weatherRepository)

                val alertsEnabled = settingsRepository.settingsFlow.first().weatherAlerts
                if (alertsEnabled) {
                    val units = settingsRepository.settingsFlow.first().units
                    val weather = weatherRepository.weatherFlow.first()!!

                    Notifications.sendAlertNotification(context, weather, units)
                }

                // TODO: Show error notification if the update fails

                Result.success()
            } catch (e: Exception) {
                Timber.w(e, "Weather update failed, retrying")
                Result.retry()
            }
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo =
        ForegroundInfo(
            Notifications.ID_BACKGROUND,
            notifier.progressNotificationBuilder.build(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )

    companion object {
        private const val TAG = "weather_update"
        private const val WORK_NAME_PERIODIC = "weather_update_periodic"
        private const val WORK_NAME_ONE_SHOT = "weather_update_one_shot"
        private const val BACKOFF_DELAY: Long = 10

        suspend fun scheduleJob(
            context: Context,
            repository: SettingsRepository
        ) {
            val updateInterval = repository.settingsFlow.first().updateInterval
            val constraints = Constraints(
                requiredNetworkType = NetworkType.CONNECTED
            )

            val request = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(
                updateInterval.minutes,
                TimeUnit.MINUTES,
                BACKOFF_DELAY,
                TimeUnit.MINUTES
            )
                .addTag(TAG)
                .addTag(WORK_NAME_PERIODIC)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.MINUTES)
                .build()

            context.workManager.enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun start(context: Context): Boolean {
            val workManager = context.workManager
            if (workManager.isRunning(TAG)) return false

            val request = OneTimeWorkRequestBuilder<WeatherUpdateWorker>()
                .addTag(TAG)
                .addTag(WORK_NAME_ONE_SHOT)
                .build()

            workManager.enqueueUniqueWork(
                WORK_NAME_ONE_SHOT,
                ExistingWorkPolicy.KEEP,
                request
            )

            return true
        }

        fun isScheduled(context: Context): Boolean {
            val infos = context.workManager
                .getWorkInfosForUniqueWork(WORK_NAME_PERIODIC)
                .get()

            return infos.any {
                it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING
            }
        }

        // TODO: Get rid of this function, it's redundant
        suspend fun updateWeather(
            locationRepository: LocationRepository,
            weatherRepository: WeatherRepository,
        ) {
            // TODO: Handle the update failing

            locationRepository.updateLocation()
            val location = locationRepository.locationFlow.firstOrNull() ?: run {
                throw LocationException("Repository failed to provide location")
            }

            weatherRepository.updateWeather(location)
        }
    }
}