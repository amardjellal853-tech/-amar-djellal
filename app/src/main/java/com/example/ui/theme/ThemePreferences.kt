package com.example.ui.theme

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

class ThemePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("eps_theme_preferences", Context.MODE_PRIVATE)

    private val _currentTheme = mutableStateOf(
        runCatching {
            val savedName = prefs.getString("app_theme_preset", AppThemePreset.EMERALD_GOLD.name)
            AppThemePreset.valueOf(savedName ?: AppThemePreset.EMERALD_GOLD.name)
        }.getOrDefault(AppThemePreset.EMERALD_GOLD)
    )
    val currentTheme: State<AppThemePreset> = _currentTheme

    private val _darkModeOption = mutableStateOf(
        runCatching {
            val savedName = prefs.getString("dark_mode_option", DarkModeOption.LIGHT.name)
            DarkModeOption.valueOf(savedName ?: DarkModeOption.LIGHT.name)
        }.getOrDefault(DarkModeOption.LIGHT)
    )
    val darkModeOption: State<DarkModeOption> = _darkModeOption

    fun setThemePreset(preset: AppThemePreset) {
        _currentTheme.value = preset
        prefs.edit().putString("app_theme_preset", preset.name).apply()
    }

    fun setDarkModeOption(option: DarkModeOption) {
        _darkModeOption.value = option
        prefs.edit().putString("dark_mode_option", option.name).apply()
    }
}
