package yusufs.turan.florai.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val CyberBotanicalDarkColorScheme = darkColorScheme(
    primary = BiolumeLeaf,
    onPrimary = BiolumeLeafDeep,
    primaryContainer = BiolumeLeafContainer,
    onPrimaryContainer = OnBiolumeLeafContainer,
    secondary = LavenderSignal,
    onSecondary = LavenderSignalDeep,
    secondaryContainer = LavenderSignalContainer,
    onSecondaryContainer = OnLavenderSignalContainer,
    tertiary = CyanPulse,
    onTertiary = CyanPulseDeep,
    tertiaryContainer = CyanPulseContainer,
    onTertiaryContainer = OnCyanPulseContainer,
    error = ErrorGlow,
    onError = ErrorDeep,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = CyberGardenBackground,
    onBackground = TextOnDark,
    surface = CyberGardenSurface,
    onSurface = TextOnDark,
    surfaceVariant = CyberGardenSurfaceVariant,
    onSurfaceVariant = TextOnDarkVariant,
    outline = CyberGardenOutline,
    outlineVariant = CyberGardenOutlineVariant,
    inverseSurface = SoftPlumSurface,
    inverseOnSurface = TextOnLight,
    inversePrimary = ForestPrimary,
    surfaceTint = BiolumeLeaf,
    surfaceContainer = CyberGardenSurfaceContainer,
    surfaceContainerHigh = CyberGardenSurfaceContainerHigh,
    surfaceContainerHighest = CyberGardenSurfaceVariant
)

private val CyberBotanicalLightColorScheme = lightColorScheme(
    primary = ForestPrimary,
    onPrimary = Color.White,
    primaryContainer = ForestPrimaryContainer,
    onPrimaryContainer = OnForestPrimaryContainer,
    secondary = SoftLavender,
    onSecondary = Color.White,
    secondaryContainer = SoftLavenderContainer,
    onSecondaryContainer = OnSoftLavenderContainer,
    tertiary = DeepTeal,
    onTertiary = Color.White,
    tertiaryContainer = SoftTealContainer,
    onTertiaryContainer = OnSoftTealContainer,
    error = ErrorContainerDark,
    onError = Color.White,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = SoftPlumBackground,
    onBackground = TextOnLight,
    surface = SoftPlumSurface,
    onSurface = TextOnLight,
    surfaceVariant = SoftPlumSurfaceVariant,
    onSurfaceVariant = TextOnLightVariant,
    outline = SoftPlumOutline,
    outlineVariant = SoftPlumOutlineVariant,
    inverseSurface = CyberGardenSurface,
    inverseOnSurface = TextOnDark,
    inversePrimary = BiolumeLeaf,
    surfaceTint = ForestPrimary,
    surfaceContainer = SoftPlumSurfaceContainer,
    surfaceContainerHigh = SoftPlumSurfaceContainerHigh,
    surfaceContainerHighest = SoftPlumSurfaceVariant
)

@Composable
fun FlorAITheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> CyberBotanicalDarkColorScheme
        else -> CyberBotanicalLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
