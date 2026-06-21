package io.github.georgemarkas.weatherapp.settings.models

import androidx.annotation.StringRes
import io.github.georgemarkas.weatherapp.R

enum class Units(
    @param:StringRes val labelRes: Int,
    val temperature: String
) {
    METRIC(
        R.string.setting_units_metric,
        "°C"
    ),

    IMPERIAL(
        R.string.setting_units_imperial,
        "°F"
    ),
}