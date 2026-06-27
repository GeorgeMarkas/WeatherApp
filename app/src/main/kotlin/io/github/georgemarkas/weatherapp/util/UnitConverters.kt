package io.github.georgemarkas.weatherapp.util

fun celsiusToFahrenheit(celsius: Double): Double = celsius * 9 / 5 + 32

fun kphToMph(kph: Double): Double = kph * 0.6213711922

fun mmToIn(mm: Double): Double = mm * 0.0393700787

// TODO: TEMPORARY, REPLACE WITH ICON 'WHEN'
fun degreesToDirection(degrees: Double): String {
    val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    val index = ((degrees + 22.5) / 45.0).toInt() % 8
    return directions[index]
}