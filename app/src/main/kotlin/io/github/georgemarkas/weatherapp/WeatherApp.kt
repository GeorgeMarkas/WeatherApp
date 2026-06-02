package io.github.georgemarkas.weatherapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.github.georgemarkas.weatherapp.logging.ReleaseTree
import timber.log.Timber

@HiltAndroidApp
class WeatherApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
    }
}