package com.example.mobilsmartwear.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Primary and Secondary colors
val Primary = Color(0xFF1B3A5B)      // Zengin lacivert
val Secondary = Color(0xFFD4AF37)     // Altın
val Navy900 = Color(0xFF0F233A)      // Koyu lacivert
val Navy800 = Color(0xFF1B3A5B)      // Ana lacivert
val Navy700 = Color(0xFF264B73)      // Orta lacivert
val Navy600 = Color(0xFF315C8C)      // Açık lacivert
val Gold500 = Color(0xFFD4AF37)      // Altın
val Gold400 = Color(0xFFDCBD5C)      // Açık altın
val Gold300 = Color(0xFFE5CC82)      // Çok açık altın
val Gray900 = Color(0xFF212121)      // En koyu gri
val Gray100 = Color(0xFFF5F5F5)      // En açık gri
val Gray50 = Color(0xFFFAFAFA)       // Neredeyse beyaz
val Error = Color(0xFFB71C1C)        // Koyu kırmızı
val Success = Color(0xFF2E7D32)      // Koyu yeşil

private val LightColorScheme = lightColorScheme(
    primary = NavyBlue,
    onPrimary = Color.White,
    primaryContainer = Navy600,
    onPrimaryContainer = Color.White,
    secondary = Gold500,
    onSecondary = Color.Black,
    secondaryContainer = Gold300,
    onSecondaryContainer = Gray900,
    background = Gray50,
    onBackground = Gray900,
    surface = Color.White,
    onSurface = Gray900,
    surfaceVariant = Gray100,
    onSurfaceVariant = Navy700,
    error = Error,
    onError = Color.White,
    outline = Navy600,
    outlineVariant = Gold400,
    scrim = Color(0x52000000),
    inverseSurface = Navy900,
    inverseOnSurface = Color.White,
    inversePrimary = Gold300
)

private val DarkColorScheme = darkColorScheme(
    primary = NavyBlue,
    onPrimary = Color.White,
    secondary = Gold500,
    onSecondary = Color.Black,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF121212),
    onSurface = Color.White
)

@Composable
fun MobilSmartWearTheme(
    darkTheme: Boolean = false,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Dinamik renkleri devre dışı bırakıyoruz
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb() // Status bar'ı lacivert yap
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false // Status bar ikonlarını beyaz yap
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}