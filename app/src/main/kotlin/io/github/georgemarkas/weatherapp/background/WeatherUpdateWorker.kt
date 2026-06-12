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
import androidx.work.WorkQuery
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.georgemarkas.weatherapp.data.LocationRepository
import io.github.georgemarkas.weatherapp.data.WeatherRepository
import io.github.georgemarkas.weatherapp.extensions.isOnline
import io.github.georgemarkas.weatherapp.extensions.isRunning
import io.github.georgemarkas.weatherapp.extensions.setForegroundSafely
import io.github.georgemarkas.weatherapp.extensions.workManager
import io.github.georgemarkas.weatherapp.notification.Notifications
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes

@HiltWorker
class WeatherUpdateWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository,
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
                locationRepository.updateLocation()
                val location = locationRepository.locationFlow.first()

                weatherRepository.updateWeather(location)

                // TODO: Temporary for testing, make it not shit
                val weather = weatherRepository.weatherFlow.first()
                Notifications.sendForecastNotification(context, weather!!)

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

        fun scheduleJob(context: Context) {
            val updateInterval = 15.minutes // TODO: Have this be a setting
            val constraints = Constraints(
                requiredNetworkType = NetworkType.CONNECTED
            )

            val request = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(
                updateInterval.inWholeMinutes,
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

        fun stop(context: Context) {
            val workManager = context.workManager
            val workQuery = WorkQuery.Builder.fromTags(listOf(TAG))
                .addStates(listOf(WorkInfo.State.RUNNING))
                .build()

            workManager.getWorkInfos(workQuery).get()
                .forEach {
                    workManager.cancelWorkById(it.id)
                    if (it.tags.contains(WORK_NAME_PERIODIC)) scheduleJob(context)
                }
        }

        fun isScheduled(context: Context): Boolean {
            val infos = context.workManager
                .getWorkInfosForUniqueWork(WORK_NAME_PERIODIC)
                .get()

            return infos.any {
                it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING
            }
        }
    }
}