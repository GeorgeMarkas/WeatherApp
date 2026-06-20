package io.github.georgemarkas.weatherapp.alerts.models

data class Alert(
    val type: AlertType,
    val severity: AlertSeverity,
    val startTime: Long? = null,
    val value: Double? = null,
)