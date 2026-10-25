package com.jesse.taskapp.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

/**
 * Applies the app Material theme.
 *
 * @param darkTheme true to use the dark palette when dynamic color is off.
 * @param dynamicColor true to use the device wallpaper colors on Android 12 and newer.
 * @param content screens drawn inside the theme.
 */
@Composable
fun TaskAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // Dynamic color needs Android 12. Previews turn it off so they use the fixed palettes.
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

/**
 * Preview of [TaskAppTheme] using the fixed light palette.
 */
@Preview(showBackground = true, name = "Light")
@Composable
private fun TaskAppThemeLightPreview() {
    TaskAppTheme(darkTheme = false, dynamicColor = false) {
        Text("Tasks")
    }
}

/**
 * Preview of [TaskAppTheme] using the fixed dark palette.
 */
@Preview(showBackground = true, name = "Dark")
@Composable
private fun TaskAppThemeDarkPreview() {
    TaskAppTheme(darkTheme = true, dynamicColor = false) {
        Text("Tasks")
    }
}