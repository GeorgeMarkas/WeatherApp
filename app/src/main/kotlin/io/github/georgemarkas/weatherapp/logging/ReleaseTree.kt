package io.github.georgemarkas.weatherapp.logging

import android.util.Log
import timber.log.Timber

class ReleaseTree : Timber.DebugTree() {

    override fun isLoggable(tag: String?, priority: Int): Boolean {
        return priority >= Log.WARN
    }
}