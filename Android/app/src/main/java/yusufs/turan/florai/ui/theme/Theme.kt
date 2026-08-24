package yusufs.turan.florai.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = LeafGreenLight,
    onPrimary = LeafGreenDark,
    primaryContainer = LeafGreenDark,
    onPrimaryContainer = MintContainer,
    secondary = SageLight,
    onSecondary = OnSageContainer,
    secondaryContainer = DarkSurfaceContainerHigh,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = Sunflower,
    onTertiary = OnSunflowerContainer,
    tertiaryContainer = CoralDark,
    onTertiaryContainer = Color.White,
    error = ErrorContainer,
    onError = OnErrorContainer,
    errorContainer = ErrorRed,
    onErrorContainer = Color.White,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = SageLight,
    outlineVariant = DarkSurfaceVariant,
    inverseSurface = FlorAISurface,
    inverseOnSurface = TextPrimary,
    inversePrimary = LeafGreen,
    surfaceTint = LeafGreenLight,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = LeafGreen,
    onPrimary = Color.White,
    primaryContainer = MintContainer,
    onPrimaryContainer = OnMintContainer,
    secondary = Sage,
    onSecondary = Color.White,
    secondaryContainer = SageContainer,
    onSecondaryContainer = OnSageContainer,
    tertiary = Sunflower,
    onTertiary = OnSunflowerContainer,
    tertiaryContainer = SunflowerContainer,
    onTertiaryContainer = OnSunflowerContainer,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = FlorAIBackground,
    onBackground = TextPrimary,
    surface = FlorAISurface,
    onSurface = TextPrimary,
    surfaceVariant = FlorAISurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = FlorAIOutline,
    outlineVariant = FlorAIOutlineVariant,
    inverseSurface = TextPrimary,
    inverseOnSurface = FlorAISurface,
    inversePrimary = LeafGreenLight,
    surfaceTint = LeafGreen,
    surfaceContainer = FlorAISurfaceContainer,
    surfaceContainerHigh = FlorAISurfaceContainerHigh,
    surfaceContainerHighest = FlorAISurfaceVariant
)

@Composable
fun FlorAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
