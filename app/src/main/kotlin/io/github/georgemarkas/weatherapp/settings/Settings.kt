package io.github.georgemarkas.weatherapp.settings

import io.github.georgemarkas.weatherapp.settings.models.Units
import io.github.georgemarkas.weatherapp.settings.models.UpdateInterval

data class Settings(
    val updateInterval: UpdateInterval = UpdateInterval.DEFAULT,
    val units: Units = Units.METRIC,
    val weatherAlerts: Boolean = false,
)