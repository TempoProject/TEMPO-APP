package com.tempo.tempoapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class CustomColors(
    val bleeding: Color,
    val success: Color,
    val neutral: Color,
    val infusion: Color
)

val LightCustomColors = CustomColors(
    bleeding = BleedingLight,
    success = SuccessLight,
    neutral = NeutralLight,
    infusion = InfusionLight
)

val DarkCustomColors = CustomColors(
    bleeding = BleedingDark,
    success = SuccessDark,
    neutral = NeutralDark,
    infusion = InfusionDark
)

val LocalCustomColors = staticCompositionLocalOf { LightCustomColors }

val MaterialTheme.customColors: CustomColors
    @Composable
    @ReadOnlyComposable
    get() = LocalCustomColors.current