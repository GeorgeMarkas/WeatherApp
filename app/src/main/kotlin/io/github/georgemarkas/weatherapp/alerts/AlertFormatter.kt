package io.github.georgemarkas.weatherapp.alerts

import android.content.Context
import io.github.georgemarkas.weatherapp.R
import io.github.georgemarkas.weatherapp.alerts.models.Alert
import io.github.georgemarkas.weatherapp.alerts.models.AlertSeverity
import io.github.georgemarkas.weatherapp.alerts.models.AlertType
import kotlin.math.roundToInt

object AlertFormatter {

    fun title(context: Context, type: AlertType): String = context.getString(
        when (type) {
            AlertType.THUNDERSTORM -> R.string.alert_thunderstorm_title
            AlertType.HEAVY_RAIN -> R.string.alert_heavy_rain_title
            AlertType.HEAVY_SNOW -> R.string.alert_heavy_snow_title
            AlertType.FREEZING_RAIN -> R.string.alert_freezing_rain_title
            AlertType.HIGH_WIND -> R.string.alert_high_wind_title
            AlertType.EXTREME_HEAT -> R.string.alert_extreme_heat_title
            AlertType.EXTREME_COLD -> R.string.alert_extreme_cold_title
        }
    )

    fun severityLabel(context: Context, severity: AlertSeverity): String = context.getString(
        when (severity) {
            AlertSeverity.ADVISORY -> R.string.alert_severity_advisory
            AlertSeverity.WARNING -> R.string.alert_severity_warning
            AlertSeverity.SEVERE -> R.string.alert_severity_severe
        }
    )

    fun message(
        context: Context,
        alert: Alert,
    ): String {
        // TODO: Make this be based on user settings
        val unit = "°C"

        val value = alert.value?.roundToInt()
        return when (alert.type) {
            AlertType.HIGH_WIND ->
                context.getString(R.string.alert_high_wind_message, value)

            AlertType.EXTREME_HEAT ->
                context.getString(R.string.alert_extreme_heat_message, value, unit)

            AlertType.EXTREME_COLD ->
                context.getString(R.string.alert_extreme_cold_message, value, unit)

            AlertType.THUNDERSTORM ->
                context.getString(R.string.alert_thunderstorm_message)

            AlertType.HEAVY_RAIN ->
                context.getString(R.string.alert_heavy_rain_message)

            AlertType.HEAVY_SNOW ->
                context.getString(R.string.alert_heavy_snow_message)

            AlertType.FREEZING_RAIN ->
                context.getString(R.string.alert_freezing_rain_message)
        }
    }
}