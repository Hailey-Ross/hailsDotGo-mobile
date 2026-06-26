package live.hails.hailsdotgo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary             = PrimaryDark,
    onPrimary           = Color.White,
    primaryContainer    = PrimaryContainerDark,
    onPrimaryContainer  = Color.White,
    secondary           = SecondaryDark,
    onSecondary         = Color.Black,
    background          = BackgroundDark,
    onBackground        = OnSurfaceDark,
    surface             = SurfaceDark,
    onSurface           = OnSurfaceDark,
    surfaceVariant      = SurfaceVariantDark,
    onSurfaceVariant    = OnSurfaceVariantDark,
    error               = ErrorDark,
    onError             = Color.White,
)

private val LightColors = lightColorScheme(
    primary             = PrimaryLight,
    onPrimary           = Color.White,
    primaryContainer    = PrimaryContainerLight,
    onPrimaryContainer  = Color.White,
    secondary           = SecondaryLight,
    onSecondary         = Color.White,
    background          = BackgroundLight,
    onBackground        = OnSurfaceLight,
    surface             = SurfaceLight,
    onSurface           = OnSurfaceLight,
    surfaceVariant      = SurfaceVariantLight,
    onSurfaceVariant    = OnSurfaceVariantLight,
    error               = ErrorLight,
    onError             = Color.White,
)

@Composable
fun HailsDotGoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography  = HailsDotGoTypography,
        content     = content,
    )
}
