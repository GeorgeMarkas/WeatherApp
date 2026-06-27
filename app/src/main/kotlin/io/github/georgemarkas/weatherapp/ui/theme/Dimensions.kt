package io.github.georgemarkas.weatherapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Dimensions(
    val spacing1: Dp = 4.dp,
    val spacing2: Dp = 8.dp,
    val spacing3: Dp = 12.dp,
    val spacing4: Dp = 16.dp,
//    val spacingLarge: Dp = 24.dp,

    val iconSizeSmall: Dp = 24.dp,
    val iconSizeMedium: Dp = 48.dp,
    val iconSizeLarge: Dp = 96.dp,

    val forecastBoxColumnPadding: Dp = 8.dp,
    val forecastBoxBordersHorizontal: Dp = 20.dp,

    val forecastRowContentsHPadding: Dp = 12.dp,
    val forecastRowContentsHArrangement: Dp = 16.dp,
    val forecastRowBottomPadding: Dp = 12.dp,

    val forecastRowItemVerticalArrangement: Dp = 6.dp,
    )

val LocalDimensions = staticCompositionLocalOf { Dimensions() }

val MaterialTheme.dimens: Dimensions
    @Composable
    get() = LocalDimensions.current
