package com.example.weatherapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.weatherapp.data.WeatherDataStore
import com.example.weatherapp.data.WeatherFetchResult
import com.example.weatherapp.data.WeatherRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class WeatherUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: WeatherRepository,
    private val dataStore: WeatherDataStore
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return when (val fetch = repository.fetchWeather()) {
            is WeatherFetchResult.Success -> {
                dataStore.save(fetch)
                Result.success()
            }

            is WeatherFetchResult.Error -> Result.retry()
            WeatherFetchResult.NoLocation -> Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "weather_update"

        // TODO: Have the update interval be adjustable by the user, not outright hard coded
        private const val UPDATE_INTERVAL = 15L

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(
                UPDATE_INTERVAL,
                TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}