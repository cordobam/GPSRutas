package com.example.melirutas.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = DarkBlue,
    onPrimary = Color.White,
    primaryContainer = DarkBlueContainer,
    onPrimaryContainer = OnDarkBlueContainer,
    secondary = BlueGray,
    onSecondary = Color.White,
    secondaryContainer = BlueGrayContainer,
    onSecondaryContainer = OnBlueGrayContainer,
    tertiary = NeonViolet,
    onTertiary = Color.White,
    tertiaryContainer = NeonVioletContainer,
    onTertiaryContainer = OnNeonVioletContainer,
    background = AppBackground,
    onBackground = OnAppBackground,
    surface = AppBackground,
    onSurface = OnAppBackground,
    surfaceVariant = AppSurfaceVariant,
    onSurfaceVariant = OnAppSurfaceVariant
)

@Composable
fun MeliRutasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
