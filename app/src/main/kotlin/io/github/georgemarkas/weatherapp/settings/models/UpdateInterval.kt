package io.github.georgemarkas.weatherapp.settings.models

import androidx.annotation.StringRes
import io.github.georgemarkas.weatherapp.R

enum class UpdateInterval(val minutes: Long,  @param:StringRes val labelRes: Int) {
    MIN_30(30, R.string.setting_update_interval_min_30),
    HOUR_1(60, R.string.setting_update_interval_hour_1),
    HOUR_1_5(90, R.string.setting_update_interval_hour_1_5),
    HOUR_2(120, R.string.setting_update_interval_hour_2),
    HOUR_3(180, R.string.setting_update_interval_hour_3),
    HOUR_6(360, R.string.setting_update_interval_hour_6),
    HOUR_12(720, R.string.setting_update_interval_hour_12),
    HOUR_24(1440, R.string.setting_update_interval_hour_24);

    companion object {
        val DEFAULT = HOUR_1

        fun fromMinutes(m: Long): UpdateInterval =
            entries.firstOrNull { it.minutes == m } ?: DEFAULT
    }
}