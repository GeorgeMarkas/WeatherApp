package io.github.georgemarkas.weatherapp.extensions

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.delay
import timber.log.Timber

val Context.workManager: WorkManager
    get() = WorkManager.getInstance(this)

fun WorkManager.isRunning(tag: String): Boolean {
    val infos = getWorkInfosByTag(tag).get()

    return infos.any {
        it.state == WorkInfo.State.RUNNING
    }
}

suspend fun CoroutineWorker.setForegroundSafely() {
    try {
        setForeground(getForegroundInfo())
        delay(500)
    } catch(_: IllegalStateException) {
        Timber.d("Not allowed to set foreground job")
    }
}