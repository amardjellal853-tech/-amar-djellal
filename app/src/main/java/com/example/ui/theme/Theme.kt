package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ==========================================
// 1. خيارات وأنماط الثيم والألوان
// ==========================================

enum class AppThemePreset(
    val titleAr: String,
    val subtitleAr: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val tertiaryColor: Color,
    val isRecommended: Boolean = false
) {
    EMERALD_GOLD(
        titleAr = "الأولمبي الأخضر والذهبي",
        subtitleAr = "مستوحى من الملاعب العشبية والراية الوطنية",
        primaryColor = OlympicGreenPrimary,
        secondaryColor = OlympicGoldSecondary,
        tertiaryColor = OlympicTealTertiary,
        isRecommended = true
    ),
    OCEAN_ORANGE(
        titleAr = "الأزرق الملكي والبرتقالي",
        subtitleAr = "ألوان رياضية ديناميكية مفعمة بالطاقة والنشاط",
        primaryColor = OceanBluePrimary,
        secondaryColor = DynamicOrangeSecondary,
        tertiaryColor = AthleticSkyTertiary
    ),
    CRIMSON_TRACK(
        titleAr = "مضمار العنابي والذهبي",
        subtitleAr = "مستوحى من مضمار ألعاب القوى والكرامة الرياضية",
        primaryColor = CrimsonTrackPrimary,
        secondaryColor = SolarAmberSecondary,
        tertiaryColor = BronzeTertiary
    ),
    SUNLIGHT_HIGH_CONTRAST(
        titleAr = "النهاري عالي التباين",
        subtitleAr = "قراءة فائقة الوضوح تحت أشعة الشمس المباشرة",
        primaryColor = SunlightIntensePrimary,
        secondaryColor = SunlightIntenseSecondary,
        tertiaryColor = SunlightIntenseTertiary
    ),
    ELECTRIC_CYAN(
        titleAr = "السيان الرياضي المضيء",
        subtitleAr = "ألوان عصرية منعشة ذات تباين ناصع وحيوي",
        primaryColor = ElectricCyanPrimary,
        secondaryColor = ElectricCoralSecondary,
        tertiaryColor = ElectricMintTertiary
    )
}

enum class DarkModeOption(val titleAr: String) {
    LIGHT("نهاري (فاتح)"),
    DARK("ليلي (داكن)"),
    SYSTEM("تلقائي (حسب النظام)")
}

// ==========================================
// 2. دوال بناء لوحات الألوان (ColorSchemes)
// ==========================================

fun getThemeColorScheme(preset: AppThemePreset, isDark: Boolean): ColorScheme {
    return if (isDark) {
        when (preset) {
            AppThemePreset.EMERALD_GOLD -> darkColorScheme(
                primary = OlympicGreenDarkPrimary,
                onPrimary = Color(0xFF052E16),
                primaryContainer = OlympicGreenDarkContainer,
                onPrimaryContainer = Color(0xFFDCFCE7),
                secondary = OlympicGoldSecondary,
                onSecondary = Color.Black,
                secondaryContainer = Color(0xFF78350F),
                onSecondaryContainer = Color(0xFFFEF3C7),
                tertiary = Color(0xFF2DD4BF),
                onTertiary = Color(0xFF042F2E),
                tertiaryContainer = Color(0xFF115E59),
                onTertiaryContainer = Color(0xFFCCFBF1),
                background = OlympicGreenDarkBackground,
                onBackground = Color(0xFFF0FDF4),
                surface = OlympicGreenDarkSurface,
                onSurface = Color(0xFFF0FDF4),
                surfaceVariant = OlympicGreenDarkSurfaceVariant,
                onSurfaceVariant = Color(0xFFBBF7D0),
                outline = Color(0xFF15803D),
                outlineVariant = Color(0xFF166534),
                error = Color(0xFFF87171),
                onError = Color(0xFF450A0A)
            )

            AppThemePreset.OCEAN_ORANGE -> darkColorScheme(
                primary = OceanBlueDarkPrimary,
                onPrimary = Color(0xFF082F49),
                primaryContainer = OceanBlueDarkContainer,
                onPrimaryContainer = Color(0xFFDBEAFE),
                secondary = DynamicOrangeSecondary,
                onSecondary = Color.Black,
                secondaryContainer = Color(0xFF7C2D12),
                onSecondaryContainer = Color(0xFFFFEDD5),
                tertiary = Color(0xFF38BDF8),
                onTertiary = Color(0xFF0C4A6E),
                background = OceanBlueDarkBackground,
                onBackground = Color(0xFFF8FAFC),
                surface = OceanBlueDarkSurface,
                onSurface = Color(0xFFF8FAFC),
                surfaceVariant = OceanBlueDarkSurfaceVariant,
                onSurfaceVariant = Color(0xFFCBD5E1),
                outline = Color(0xFF3B82F6),
                outlineVariant = Color(0xFF1E40AF),
                error = Color(0xFFF87171),
                onError = Color(0xFF450A0A)
            )

            AppThemePreset.CRIMSON_TRACK -> darkColorScheme(
                primary = CrimsonTrackDarkPrimary,
                onPrimary = Color(0xFF450A0A),
                primaryContainer = CrimsonTrackDarkContainer,
                onPrimaryContainer = Color(0xFFFEE2E2),
                secondary = SolarAmberSecondary,
                onSecondary = Color.Black,
                secondaryContainer = Color(0xFF78350F),
                onSecondaryContainer = Color(0xFFFEF3C7),
                tertiary = Color(0xFFFBBF24),
                onTertiary = Color(0xFF451A03),
                background = CrimsonTrackDarkBackground,
                onBackground = Color(0xFFFFF1F2),
                surface = CrimsonTrackDarkSurface,
                onSurface = Color(0xFFFFF1F2),
                surfaceVariant = CrimsonTrackDarkSurfaceVariant,
                onSurfaceVariant = Color(0xFFFECDD3),
                outline = Color(0xFFDC2626),
                outlineVariant = Color(0xFF991B1B),
                error = Color(0xFFF87171),
                onError = Color(0xFF450A0A)
            )

            AppThemePreset.SUNLIGHT_HIGH_CONTRAST -> darkColorScheme(
                primary = Color(0xFF38BDF8),
                onPrimary = Color(0xFF082F49),
                primaryContainer = Color(0xFF0369A1),
                onPrimaryContainer = Color(0xFFE0F2FE),
                secondary = Color(0xFFFBBF24),
                onSecondary = Color(0xFF451A03),
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

            AppThemePreset.ELECTRIC_CYAN -> darkColorScheme(
                primary = ElectricCyanDarkPrimary,
                onPrimary = Color(0xFF082F49),
                primaryContainer = ElectricCyanDarkContainer,
                onPrimaryContainer = Color(0xFFE0F2FE),
                secondary = Color(0xFFFB7185),
                onSecondary = Color(0xFF4C0519),
                secondaryContainer = Color(0xFF881337),
                onSecondaryContainer = Color(0xFFFFE4E6),
                tertiary = Color(0xFF34D399),
                onTertiary = Color(0xFF064E3B),
                background = ElectricCyanDarkBackground,
                onBackground = Color(0xFFF8FAFC),
                surface = ElectricCyanDarkSurface,
                onSurface = Color(0xFFF8FAFC),
                surfaceVariant = ElectricCyanDarkSurfaceVariant,
                onSurfaceVariant = Color(0xFFE2E8F0),
                outline = Color(0xFF0284C7),
                error = Color(0xFFF87171),
                onError = Color(0xFF450A0A)
            )
        }
    } else {
        when (preset) {
            AppThemePreset.EMERALD_GOLD -> lightColorScheme(
                primary = OlympicGreenPrimary,
                onPrimary = Color.White,
                primaryContainer = OlympicGreenContainer,
                onPrimaryContainer = OlympicGreenOnContainer,
                secondary = OlympicGoldSecondary,
                onSecondary = Color.White,
                secondaryContainer = OlympicGoldContainer,
                onSecondaryContainer = OlympicGoldOnContainer,
                tertiary = OlympicTealTertiary,
                onTertiary = Color.White,
                tertiaryContainer = OlympicTealContainer,
                onTertiaryContainer = Color(0xFF134E4A),
                background = Color(0xFFF8FAF8),
                onBackground = Color(0xFF0F172A),
                surface = Color.White,
                onSurface = Color(0xFF0F172A),
                surfaceVariant = Color(0xFFF1F5F2),
                onSurfaceVariant = Color(0xFF334155),
                outline = Color(0xFFCBD5E1),
                outlineVariant = Color(0xFFE2E8F0),
                error = ActionNegative,
                onError = Color.White,
                errorContainer = ActionNegativeContainer,
                onErrorContainer = ActionNegative
            )

            AppThemePreset.OCEAN_ORANGE -> lightColorScheme(
                primary = OceanBluePrimary,
                onPrimary = Color.White,
                primaryContainer = OceanBlueContainer,
                onPrimaryContainer = OceanBlueOnContainer,
                secondary = DynamicOrangeSecondary,
                onSecondary = Color.White,
                secondaryContainer = DynamicOrangeContainer,
                onSecondaryContainer = DynamicOrangeOnContainer,
                tertiary = AthleticSkyTertiary,
                onTertiary = Color.White,
                tertiaryContainer = AthleticSkyContainer,
                onTertiaryContainer = Color(0xFF0C4A6E),
                background = Color(0xFFF8FAFC),
                onBackground = Color(0xFF0F172A),
                surface = Color.White,
                onSurface = Color(0xFF0F172A),
                surfaceVariant = Color(0xFFF1F5F9),
                onSurfaceVariant = Color(0xFF334155),
                outline = Color(0xFFCBD5E1),
                outlineVariant = Color(0xFFE2E8F0),
                error = ActionNegative,
                onError = Color.White,
                errorContainer = ActionNegativeContainer,
                onErrorContainer = ActionNegative
            )

            AppThemePreset.CRIMSON_TRACK -> lightColorScheme(
                primary = CrimsonTrackPrimary,
                onPrimary = Color.White,
                primaryContainer = CrimsonTrackContainer,
                onPrimaryContainer = CrimsonTrackOnContainer,
                secondary = SolarAmberSecondary,
                onSecondary = Color.White,
                secondaryContainer = SolarAmberContainer,
                onSecondaryContainer = SolarAmberOnContainer,
                tertiary = BronzeTertiary,
                onTertiary = Color.White,
                tertiaryContainer = BronzeContainer,
                onTertiaryContainer = Color(0xFF78350F),
                background = Color(0xFFFCF8F8),
                onBackground = Color(0xFF1A0A0A),
                surface = Color.White,
                onSurface = Color(0xFF1A0A0A),
                surfaceVariant = Color(0xFFF9F1F1),
                onSurfaceVariant = Color(0xFF451A1A),
                outline = Color(0xFFE2D0D0),
                outlineVariant = Color(0xFFF3E5E5),
                error = ActionNegative,
                onError = Color.White,
                errorContainer = ActionNegativeContainer,
                onErrorContainer = ActionNegative
            )

            AppThemePreset.SUNLIGHT_HIGH_CONTRAST -> lightColorScheme(
                primary = SunlightIntensePrimary,
                onPrimary = Color.White,
                primaryContainer = SunlightIntenseContainer,
                onPrimaryContainer = Color(0xFF0C4A6E),
                secondary = SunlightIntenseSecondary,
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFFEF3C7),
                onSecondaryContainer = Color(0xFF78350F),
                tertiary = SunlightIntenseTertiary,
                onTertiary = Color.White,
                tertiaryContainer = Color(0xFFDCFCE7),
                onTertiaryContainer = Color(0xFF064E3B),
                background = Color(0xFFF8FAFC),
                onBackground = Color(0xFF020617),
                surface = Color.White,
                onSurface = Color(0xFF020617),
                surfaceVariant = Color(0xFFF1F5F9),
                onSurfaceVariant = Color(0xFF1E293B),
                outline = Color(0xFF94A3B8),
                outlineVariant = Color(0xFFCBD5E1),
                error = ActionNegative,
                onError = Color.White,
                errorContainer = ActionNegativeContainer,
                onErrorContainer = ActionNegative
            )

            AppThemePreset.ELECTRIC_CYAN -> lightColorScheme(
                primary = ElectricCyanPrimary,
                onPrimary = Color.White,
                primaryContainer = ElectricCyanContainer,
                onPrimaryContainer = Color(0xFF0C4A6E),
                secondary = ElectricCoralSecondary,
                onSecondary = Color.White,
                secondaryContainer = ElectricCoralContainer,
                onSecondaryContainer = Color(0xFF881337),
                tertiary = ElectricMintTertiary,
                onTertiary = Color.White,
                tertiaryContainer = Color(0xFFD1FAE5),
                onTertiaryContainer = Color(0xFF064E3B),
                background = Color(0xFFF8FAFC),
                onBackground = Color(0xFF0F172A),
                surface = Color.White,
                onSurface = Color(0xFF0F172A),
                surfaceVariant = Color(0xFFF1F5F9),
                onSurfaceVariant = Color(0xFF334155),
                outline = Color(0xFFCBD5E1),
                outlineVariant = Color(0xFFE2E8F0),
                error = ActionNegative,
                onError = Color.White,
                errorContainer = ActionNegativeContainer,
                onErrorContainer = ActionNegative
            )
        }
    }
}

// ==========================================
// 3. Composable الأساسي لتطبيق الثيم
// ==========================================

@Composable
fun MyApplicationTheme(
    preset: AppThemePreset = AppThemePreset.EMERALD_GOLD,
    darkModeOption: DarkModeOption = DarkModeOption.LIGHT,
    forceDaylightMode: Boolean = false,
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when {
        forceDaylightMode -> false
        darkModeOption == DarkModeOption.DARK -> true
        darkModeOption == DarkModeOption.LIGHT -> false
        darkModeOption == DarkModeOption.SYSTEM -> isSystemDark
        else -> darkTheme
    }

    val colorScheme = getThemeColorScheme(preset, isDark)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
