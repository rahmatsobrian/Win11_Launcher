package com.siroha.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.siroha.core.data.backup.LauncherBackup
import com.siroha.core.data.backup.SettingsBackup
import com.siroha.core.database.dao.AppDao
import com.siroha.core.datastore.PreferenceKeys
import com.siroha.core.domain.model.DesktopSettings
import com.siroha.core.domain.model.LauncherSettings
import com.siroha.core.domain.model.StartMenuSettings
import com.siroha.core.domain.model.TaskbarAlignment
import com.siroha.core.domain.model.TaskbarSettings
import com.siroha.core.domain.model.ThemeMode
import com.siroha.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val appDao: AppDao
) : SettingsRepository {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    override fun observeSettings(): Flow<LauncherSettings> = dataStore.data.map { prefs ->
        LauncherSettings(
            themeMode = prefs[PreferenceKeys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.AUTO,
            dynamicColorEnabled = prefs[PreferenceKeys.DYNAMIC_COLOR_ENABLED] ?: true,
            accentColorArgb = prefs[PreferenceKeys.ACCENT_COLOR_ARGB],
            blurIntensityPercent = prefs[PreferenceKeys.BLUR_INTENSITY_PERCENT] ?: 60,
            cornerRadiusDp = prefs[PreferenceKeys.CORNER_RADIUS_DP] ?: 12,
            animationsEnabled = prefs[PreferenceKeys.ANIMATIONS_ENABLED] ?: true,
            appLockEnabled = prefs[PreferenceKeys.APP_LOCK_ENABLED] ?: false,
            hiddenAppsEnabled = prefs[PreferenceKeys.HIDDEN_APPS_ENABLED] ?: true,
            taskbar = TaskbarSettings(
                alignment = prefs[PreferenceKeys.TASKBAR_ALIGNMENT]
                    ?.let { runCatching { TaskbarAlignment.valueOf(it) }.getOrNull() }
                    ?: TaskbarAlignment.CENTER,
                heightDp = prefs[PreferenceKeys.TASKBAR_HEIGHT_DP] ?: 48,
                transparencyPercent = prefs[PreferenceKeys.TASKBAR_TRANSPARENCY_PERCENT] ?: 80,
                blurEnabled = prefs[PreferenceKeys.TASKBAR_BLUR_ENABLED] ?: true,
                autoHide = prefs[PreferenceKeys.TASKBAR_AUTO_HIDE] ?: false,
                isLocked = prefs[PreferenceKeys.TASKBAR_LOCKED] ?: false,
                cornerRadiusDp = prefs[PreferenceKeys.TASKBAR_CORNER_RADIUS_DP] ?: 18
            ),
            startMenu = StartMenuSettings(
                widthDp = prefs[PreferenceKeys.START_MENU_WIDTH_DP] ?: 640,
                heightDp = prefs[PreferenceKeys.START_MENU_HEIGHT_DP] ?: 720,
                pinnedRowCount = prefs[PreferenceKeys.START_MENU_PINNED_ROWS] ?: 4,
                showRecommended = prefs[PreferenceKeys.START_MENU_SHOW_RECOMMENDED] ?: true,
                animationSpeedMultiplier = prefs[PreferenceKeys.START_MENU_ANIMATION_SPEED] ?: 1.0f
            ),
            desktop = DesktopSettings(
                gridColumns = prefs[PreferenceKeys.DESKTOP_GRID_COLUMNS] ?: 5,
                gridRows = prefs[PreferenceKeys.DESKTOP_GRID_ROWS] ?: 6,
                iconSizeDp = prefs[PreferenceKeys.DESKTOP_ICON_SIZE_DP] ?: 56,
                showLabels = prefs[PreferenceKeys.DESKTOP_SHOW_LABELS] ?: true,
                isLayoutLocked = prefs[PreferenceKeys.DESKTOP_LAYOUT_LOCKED] ?: false,
                pageCount = prefs[PreferenceKeys.DESKTOP_PAGE_COUNT] ?: 1
            )
        )
    }

    override suspend fun updateSettings(transform: (LauncherSettings) -> LauncherSettings) {
        val current = observeSettings().first()
        val updated = transform(current)
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.THEME_MODE] = updated.themeMode.name
            prefs[PreferenceKeys.DYNAMIC_COLOR_ENABLED] = updated.dynamicColorEnabled
            updated.accentColorArgb?.let { prefs[PreferenceKeys.ACCENT_COLOR_ARGB] = it }
            prefs[PreferenceKeys.BLUR_INTENSITY_PERCENT] = updated.blurIntensityPercent
            prefs[PreferenceKeys.CORNER_RADIUS_DP] = updated.cornerRadiusDp
            prefs[PreferenceKeys.ANIMATIONS_ENABLED] = updated.animationsEnabled
            prefs[PreferenceKeys.APP_LOCK_ENABLED] = updated.appLockEnabled
            prefs[PreferenceKeys.HIDDEN_APPS_ENABLED] = updated.hiddenAppsEnabled

            prefs[PreferenceKeys.TASKBAR_ALIGNMENT] = updated.taskbar.alignment.name
            prefs[PreferenceKeys.TASKBAR_HEIGHT_DP] = updated.taskbar.heightDp
            prefs[PreferenceKeys.TASKBAR_TRANSPARENCY_PERCENT] = updated.taskbar.transparencyPercent
            prefs[PreferenceKeys.TASKBAR_BLUR_ENABLED] = updated.taskbar.blurEnabled
            prefs[PreferenceKeys.TASKBAR_AUTO_HIDE] = updated.taskbar.autoHide
            prefs[PreferenceKeys.TASKBAR_LOCKED] = updated.taskbar.isLocked
            prefs[PreferenceKeys.TASKBAR_CORNER_RADIUS_DP] = updated.taskbar.cornerRadiusDp

            prefs[PreferenceKeys.START_MENU_WIDTH_DP] = updated.startMenu.widthDp
            prefs[PreferenceKeys.START_MENU_HEIGHT_DP] = updated.startMenu.heightDp
            prefs[PreferenceKeys.START_MENU_PINNED_ROWS] = updated.startMenu.pinnedRowCount
            prefs[PreferenceKeys.START_MENU_SHOW_RECOMMENDED] = updated.startMenu.showRecommended
            prefs[PreferenceKeys.START_MENU_ANIMATION_SPEED] = updated.startMenu.animationSpeedMultiplier

            prefs[PreferenceKeys.DESKTOP_GRID_COLUMNS] = updated.desktop.gridColumns
            prefs[PreferenceKeys.DESKTOP_GRID_ROWS] = updated.desktop.gridRows
            prefs[PreferenceKeys.DESKTOP_ICON_SIZE_DP] = updated.desktop.iconSizeDp
            prefs[PreferenceKeys.DESKTOP_SHOW_LABELS] = updated.desktop.showLabels
            prefs[PreferenceKeys.DESKTOP_LAYOUT_LOCKED] = updated.desktop.isLayoutLocked
            prefs[PreferenceKeys.DESKTOP_PAGE_COUNT] = updated.desktop.pageCount
        }
    }

    override suspend fun resetToDefaults() {
        dataStore.edit { it.clear() }
    }

    override suspend fun exportBackup(): String {
        val settings = observeSettings().first()
        val allApps = appDao.observeAllIncludingHidden().first()

        val backup = LauncherBackup(
            settings = SettingsBackup(
                themeMode = settings.themeMode.name,
                dynamicColorEnabled = settings.dynamicColorEnabled,
                accentColorArgb = settings.accentColorArgb,
                blurIntensityPercent = settings.blurIntensityPercent,
                cornerRadiusDp = settings.cornerRadiusDp,
                animationsEnabled = settings.animationsEnabled,
                taskbarAlignment = settings.taskbar.alignment.name,
                taskbarHeightDp = settings.taskbar.heightDp,
                taskbarTransparencyPercent = settings.taskbar.transparencyPercent,
                taskbarBlurEnabled = settings.taskbar.blurEnabled,
                taskbarAutoHide = settings.taskbar.autoHide,
                taskbarLocked = settings.taskbar.isLocked,
                taskbarCornerRadiusDp = settings.taskbar.cornerRadiusDp,
                startMenuWidthDp = settings.startMenu.widthDp,
                startMenuHeightDp = settings.startMenu.heightDp,
                startMenuPinnedRows = settings.startMenu.pinnedRowCount,
                startMenuShowRecommended = settings.startMenu.showRecommended,
                startMenuAnimationSpeed = settings.startMenu.animationSpeedMultiplier,
                desktopGridColumns = settings.desktop.gridColumns,
                desktopGridRows = settings.desktop.gridRows,
                desktopIconSizeDp = settings.desktop.iconSizeDp,
                desktopShowLabels = settings.desktop.showLabels,
                desktopLayoutLocked = settings.desktop.isLayoutLocked,
                desktopPageCount = settings.desktop.pageCount,
                appLockEnabled = settings.appLockEnabled,
                hiddenAppsEnabled = settings.hiddenAppsEnabled
            ),
            pinnedTaskbarComponentKeys = allApps.filter { it.isPinnedToTaskbar }.map { it.componentKey },
            pinnedStartComponentKeys = allApps.filter { it.isPinnedToStart }.map { it.componentKey },
            hiddenComponentKeys = allApps.filter { it.isHidden }.map { it.componentKey }
        )

        return json.encodeToString(LauncherBackup.serializer(), backup)
    }

    override suspend fun importBackup(jsonString: String): Result<Unit> = runCatching {
        val backup = json.decodeFromString(LauncherBackup.serializer(), jsonString)

        updateSettings {
            LauncherSettings(
                themeMode = runCatching { ThemeMode.valueOf(backup.settings.themeMode) }.getOrDefault(ThemeMode.AUTO),
                dynamicColorEnabled = backup.settings.dynamicColorEnabled,
                accentColorArgb = backup.settings.accentColorArgb,
                blurIntensityPercent = backup.settings.blurIntensityPercent,
                cornerRadiusDp = backup.settings.cornerRadiusDp,
                animationsEnabled = backup.settings.animationsEnabled,
                appLockEnabled = backup.settings.appLockEnabled,
                hiddenAppsEnabled = backup.settings.hiddenAppsEnabled,
                taskbar = TaskbarSettings(
                    alignment = runCatching { TaskbarAlignment.valueOf(backup.settings.taskbarAlignment) }
                        .getOrDefault(TaskbarAlignment.CENTER),
                    heightDp = backup.settings.taskbarHeightDp,
                    transparencyPercent = backup.settings.taskbarTransparencyPercent,
                    blurEnabled = backup.settings.taskbarBlurEnabled,
                    autoHide = backup.settings.taskbarAutoHide,
                    isLocked = backup.settings.taskbarLocked,
                    cornerRadiusDp = backup.settings.taskbarCornerRadiusDp
                ),
                startMenu = StartMenuSettings(
                    widthDp = backup.settings.startMenuWidthDp,
                    heightDp = backup.settings.startMenuHeightDp,
                    pinnedRowCount = backup.settings.startMenuPinnedRows,
                    showRecommended = backup.settings.startMenuShowRecommended,
                    animationSpeedMultiplier = backup.settings.startMenuAnimationSpeed
                ),
                desktop = DesktopSettings(
                    gridColumns = backup.settings.desktopGridColumns,
                    gridRows = backup.settings.desktopGridRows,
                    iconSizeDp = backup.settings.desktopIconSizeDp,
                    showLabels = backup.settings.desktopShowLabels,
                    isLayoutLocked = backup.settings.desktopLayoutLocked,
                    pageCount = backup.settings.desktopPageCount
                )
            )
        }

        for (key in backup.pinnedTaskbarComponentKeys) appDao.setPinnedToTaskbar(key, true)
        for (key in backup.pinnedStartComponentKeys) appDao.setPinnedToStart(key, true)
        for (key in backup.hiddenComponentKeys) appDao.setHidden(key, true)
    }
}
