package io.github.georgemarkas.weatherapp.util

import io.github.georgemarkas.weatherapp.R

fun celsiusToFahrenheit(celsius: Double): Double = celsius * 9 / 5 + 32

fun kphToMph(kph: Double): Double = kph * 0.6213711922

fun mmToIn(mm: Double): Double = mm * 0.0393700787

// TODO: TEMPORARY, REPLACE WITH ICON 'WHEN'
fun degreesToDirectionIcon(degrees: Double): Int {
    val index = ((degrees + 22.5) / 45.0).toInt() % 8
    return when (index) {
        0 -> R.drawable.ic_compass_n
        1 -> R.drawable.ic_compass_ne
        2 -> R.drawable.ic_compass_e
        3 -> R.drawable.ic_compass_se
        4 -> R.drawable.ic_compass_s
        5 -> R.drawable.ic_compass_sw
        6 -> R.drawable.ic_compass_w
        7 -> R.drawable.ic_compass_nw
        else -> R.drawable.ic_compass_n
    }
}

fun wmoCodeToDrawable(code: Int): Int {
    return when (code) {
        0 -> R.drawable.ic_clear
        1 -> R.drawable.ic_mostly_clear_day
        2 -> R.drawable.ic_partly_cloudy_day
        3 -> R.drawable.ic_cloudy
        45 -> R.drawable.ic_fog
        48 -> R.drawable.ic_fog
        51 -> R.drawable.ic_drizzle
        53 -> R.drawable.ic_drizzle
        55 -> R.drawable.ic_extreme_drizzle
        56 -> R.drawable.ic_overcast_drizzle
        57 -> R.drawable.ic_extreme_drizzle
        61 -> R.drawable.ic_rain
        63 -> R.drawable.ic_overcast_rain
        65 -> R.drawable.ic_extreme_rain
        66 -> R.drawable.ic_overcast_sleet
        67 -> R.drawable.ic_extreme_sleet
        71 -> R.drawable.ic_overcast_snow
        73 -> R.drawable.ic_overcast_snow
        75 -> R.drawable.ic_extreme_snow
        77 -> R.drawable.ic_snowflake
        80 -> R.drawable.ic_partly_cloudy_day_rain
        81 -> R.drawable.ic_overcast_rain
        82 -> R.drawable.ic_extreme_rain
        85 -> R.drawable.ic_mostly_clear_day_snow
        86 -> R.drawable.ic_extreme_snow
        95 -> R.drawable.ic_thunderstorms
        96 -> R.drawable.ic_thunderstorms_hail
        99 -> R.drawable.ic_thunderstorms_extreme_hail
        else -> R.drawable.ic_overcast
    }
}

fun kphToBeaufortDrawable(kph: Double): Int {
    return when {
        kph < 1 -> R.drawable.ic_wind_beaufort_0
        kph < 6 -> R.drawable.ic_wind_beaufort_1
        kph < 12 -> R.drawable.ic_wind_beaufort_2
        kph < 20 -> R.drawable.ic_wind_beaufort_3
        kph < 29 -> R.drawable.ic_wind_beaufort_4
        kph < 39 -> R.drawable.ic_wind_beaufort_5
        kph < 50 -> R.drawable.ic_wind_beaufort_6
        kph < 62 -> R.drawable.ic_wind_beaufort_7
        kph < 75 -> R.drawable.ic_wind_beaufort_8
        kph < 89 -> R.drawable.ic_wind_beaufort_9
        kph < 103 -> R.drawable.ic_wind_beaufort_10
        kph < 118 -> R.drawable.ic_wind_beaufort_11
        else -> R.drawable.ic_wind_beaufort_12
    }
}