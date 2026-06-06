package io.github.georgemarkas.weatherapp.background

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.georgemarkas.weatherapp.data.LocationRepository
import io.github.georgemarkas.weatherapp.data.WeatherRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes

@HiltWorker
class WeatherUpdateWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!isOnline(context)) {
            Timber.d("No connection, retrying...")
            return Result.retry()
        }

        // TODO: Might need to have the worker run in the context of a foreground service

        return try {
            locationRepository.updateLocation()
            val location = locationRepository.locationFlow.first()
            weatherRepository.updateWeather(location)
            Result.success()
        } catch (e: Exception) {
            Timber.w(e, "Weather update failed")
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "weather_update"
        private const val WORK_NAME_PERIODIC = "weather_update_periodic"
        private const val WORK_NAME_ONE_SHOT = "weather_update_one_shot"
        private const val BACKOFF_DELAY: Long = 10

        fun scheduleJob(context: Context) {
            Timber.d("scheduleJob called")

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

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun start(context: Context): Boolean {
            val workManager = WorkManager.getInstance(context)
            if (isRunning(workManager, TAG)) return false

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
            val workManager = WorkManager.getInstance(context)
            val workQuery = WorkQuery.Builder.fromTags(listOf(TAG))
                .addStates(listOf(WorkInfo.State.RUNNING))
                .build()

            workManager.getWorkInfos(workQuery).get()
                .forEach {
                    workManager.cancelWorkById(it.id)
                    if (it.tags.contains(WORK_NAME_PERIODIC)) scheduleJob(context)
                }
        }

        private fun isRunning(workManager: WorkManager, tag: String): Boolean {
            val list = workManager.getWorkInfosByTag(tag).get()
            return list.any { it.state == WorkInfo.State.RUNNING }
        }
    }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }
}