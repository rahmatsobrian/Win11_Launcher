package com.siroha.core.data.backup

import kotlinx.serialization.Serializable

@Serializable
data class LauncherBackup(
    val schemaVersion: Int = 1,
    val settings: SettingsBackup,
    val pinnedTaskbarComponentKeys: List<String>,
    val pinnedStartComponentKeys: List<String>,
    val hiddenComponentKeys: List<String>
)

@Serializable
data class SettingsBackup(
    val themeMode: String,
    val dynamicColorEnabled: Boolean,
    val accentColorArgb: Long?,
    val blurIntensityPercent: Int,
    val cornerRadiusDp: Int,
    val animationsEnabled: Boolean,
    val taskbarAlignment: String,
    val taskbarHeightDp: Int,
    val taskbarTransparencyPercent: Int,
    val taskbarBlurEnabled: Boolean,
    val taskbarAutoHide: Boolean,
    val taskbarLocked: Boolean,
    val taskbarCornerRadiusDp: Int,
    val startMenuWidthDp: Int,
    val startMenuHeightDp: Int,
    val startMenuPinnedRows: Int,
    val startMenuShowRecommended: Boolean,
    val startMenuAnimationSpeed: Float,
    val desktopGridColumns: Int,
    val desktopGridRows: Int,
    val desktopIconSizeDp: Int,
    val desktopShowLabels: Boolean,
    val desktopLayoutLocked: Boolean,
    val desktopPageCount: Int,
    val appLockEnabled: Boolean,
    val hiddenAppsEnabled: Boolean
)
