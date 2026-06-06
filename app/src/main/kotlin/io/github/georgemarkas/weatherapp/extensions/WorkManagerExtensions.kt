package io.github.georgemarkas.weatherapp.extensions

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkManager

val Context.workManager: WorkManager
    get() = WorkManager.getInstance(this)

fun WorkManager.isRunning(tag: String): Boolean {
    val infos = getWorkInfosByTag(tag).get()

    return infos.any {
        it.state == WorkInfo.State.RUNNING
    }
}