package com.siroha.core.domain.model

enum class ThemeMode { LIGHT, DARK, AUTO }

enum class TaskbarAlignment { CENTER, LEFT }

data class TaskbarSettings(
    val alignment: TaskbarAlignment = TaskbarAlignment.CENTER,
    val heightDp: Int = 48,
    val transparencyPercent: Int = 80,
    val blurEnabled: Boolean = true,
    val autoHide: Boolean = false,
    val isLocked: Boolean = false,
    val cornerRadiusDp: Int = 18
)

data class StartMenuSettings(
    val widthDp: Int = 640,
    val heightDp: Int = 720,
    val pinnedRowCount: Int = 4,
    val showRecommended: Boolean = true,
    val animationSpeedMultiplier: Float = 1.0f
)

data class DesktopSettings(
    val gridColumns: Int = 5,
    val gridRows: Int = 6,
    val iconSizeDp: Int = 56,
    val showLabels: Boolean = true,
    val isLayoutLocked: Boolean = false,
    val pageCount: Int = 1
)

data class LauncherSettings(
    val themeMode: ThemeMode = ThemeMode.AUTO,
    val dynamicColorEnabled: Boolean = true,
    val accentColorArgb: Long? = null,
    val blurIntensityPercent: Int = 60,
    val cornerRadiusDp: Int = 12,
    val animationsEnabled: Boolean = true,
    val taskbar: TaskbarSettings = TaskbarSettings(),
    val startMenu: StartMenuSettings = StartMenuSettings(),
    val desktop: DesktopSettings = DesktopSettings(),
    val appLockEnabled: Boolean = false,
    val hiddenAppsEnabled: Boolean = true
)
