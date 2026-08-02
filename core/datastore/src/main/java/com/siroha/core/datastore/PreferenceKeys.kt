package com.siroha.core.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

internal object PreferenceKeys {
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val DYNAMIC_COLOR_ENABLED = booleanPreferencesKey("dynamic_color_enabled")
    val ACCENT_COLOR_ARGB = longPreferencesKey("accent_color_argb")
    val BLUR_INTENSITY_PERCENT = intPreferencesKey("blur_intensity_percent")
    val CORNER_RADIUS_DP = intPreferencesKey("corner_radius_dp")
    val ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")
    val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
    val HIDDEN_APPS_ENABLED = booleanPreferencesKey("hidden_apps_enabled")

    // Taskbar
    val TASKBAR_ALIGNMENT = stringPreferencesKey("taskbar_alignment")
    val TASKBAR_HEIGHT_DP = intPreferencesKey("taskbar_height_dp")
    val TASKBAR_TRANSPARENCY_PERCENT = intPreferencesKey("taskbar_transparency_percent")
    val TASKBAR_BLUR_ENABLED = booleanPreferencesKey("taskbar_blur_enabled")
    val TASKBAR_AUTO_HIDE = booleanPreferencesKey("taskbar_auto_hide")
    val TASKBAR_LOCKED = booleanPreferencesKey("taskbar_locked")
    val TASKBAR_CORNER_RADIUS_DP = intPreferencesKey("taskbar_corner_radius_dp")

    // Start menu
    val START_MENU_WIDTH_DP = intPreferencesKey("start_menu_width_dp")
    val START_MENU_HEIGHT_DP = intPreferencesKey("start_menu_height_dp")
    val START_MENU_PINNED_ROWS = intPreferencesKey("start_menu_pinned_rows")
    val START_MENU_SHOW_RECOMMENDED = booleanPreferencesKey("start_menu_show_recommended")
    val START_MENU_ANIMATION_SPEED = floatPreferencesKey("start_menu_animation_speed")

    // Desktop
    val DESKTOP_GRID_COLUMNS = intPreferencesKey("desktop_grid_columns")
    val DESKTOP_GRID_ROWS = intPreferencesKey("desktop_grid_rows")
    val DESKTOP_ICON_SIZE_DP = intPreferencesKey("desktop_icon_size_dp")
    val DESKTOP_SHOW_LABELS = booleanPreferencesKey("desktop_show_labels")
    val DESKTOP_LAYOUT_LOCKED = booleanPreferencesKey("desktop_layout_locked")
    val DESKTOP_PAGE_COUNT = intPreferencesKey("desktop_page_count")
}
