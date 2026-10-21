package io.github.annusshahzad.solarx.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Amber80,
    secondary = Teal80,
    tertiary = Slate80,
    surfaceVariant = WarmGrey30,
    surfaceContainerHighest = WarmGrey30,
)

private val LightColorScheme = lightColorScheme(
    primary = Amber40,
    secondary = Teal40,
    tertiary = Slate40,
    surfaceVariant = WarmGrey90,
    surfaceContainerHighest = WarmGrey90,
)

@Composable
fun SolarXTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Wallpaper colours (Android 12+) are opt-in so the app keeps its own look by default.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
