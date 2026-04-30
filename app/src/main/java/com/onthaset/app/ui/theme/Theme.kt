package com.onthaset.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = OnThaSetOrange,
    onPrimary = OnThaSetLight,
    secondary = OnThaSetOrangeDark,
    background = OnThaSetLight,
    surface = OnThaSetLight,
    onBackground = OnThaSetBlack,
    onSurface = OnThaSetBlack,
)

private val DarkColors = darkColorScheme(
    primary = OnThaSetOrange,
    onPrimary = OnThaSetLight,
    secondary = OnThaSetOrangeDark,
    background = OnThaSetBlack,
    surface = OnThaSetGray,
    onBackground = OnThaSetLight,
    onSurface = OnThaSetLight,
)

@Composable
fun OnThaSetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors, typography = Typography, content = content)
}
