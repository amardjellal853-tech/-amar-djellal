package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// سمة ضوء النهار والملاعب عالية التباين (Sunlight Daylight Theme)
// مصممة بدقة لتوفير قراءة مريحة وفورية لأستاذ التربية البدنية تحت أشعة الشمس المباشرة بدون أي عتمة
private val SunlightDaylightColorScheme = lightColorScheme(
    primary = SportBlueDark,
    onPrimary = Color.White,
    primaryContainer = SportBlueContainer,
    onPrimaryContainer = Color(0xFF0C4A6E),
    secondary = SportAmber,
    onSecondary = Color.White,
    secondaryContainer = SportAmberContainer,
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = SportTeal,
    onTertiary = Color.White,
    tertiaryContainer = SportTealContainer,
    onTertiaryContainer = Color(0xFF064E3B),
    background = BackgroundLight,
    onBackground = NeutralDark,
    surface = SurfaceLight,
    onSurface = NeutralDark,
    surfaceVariant = NeutralLight,
    onSurfaceVariant = NeutralMedium,
    outline = CardBorder,
    outlineVariant = Color(0xFFCBD5E1),
    error = ActionNegative,
    onError = Color.White,
    errorContainer = ActionNegativeContainer,
    onErrorContainer = ActionNegative
)

// سمة ليلية خفيفة اختيارية
private val IndoorNightColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF082F49),
    primaryContainer = Color(0xFF0369A1),
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = Color(0xFFFBBF24),
    onSecondary = Color(0xFF451A03),
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = Color(0xFFFEF3C7),
    tertiary = Color(0xFF34D399),
    onTertiary = Color(0xFF064E3B),
    tertiaryContainer = Color(0xFF065F46),
    onTertiaryContainer = Color(0xFFD1FAE5),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFE2E8F0),
    outline = Color(0xFF64748B),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A)
)

@Composable
fun MyApplicationTheme(
    forceDaylightMode: Boolean = true, // مفعل افتراضياً للقراءة تحت ضوء الشمس المباشر
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDark = !forceDaylightMode && darkTheme
    val colorScheme = if (isDark) IndoorNightColorScheme else SunlightDaylightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
