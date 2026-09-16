package com.siroha.feature.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.siroha.core.domain.model.DeveloperSettings
import com.siroha.core.domain.model.DesktopSettings
import com.siroha.core.domain.model.LauncherSettings
import com.siroha.core.domain.model.StartMenuSettings
import com.siroha.core.domain.model.TaskbarSettings
import com.siroha.core.domain.model.ThemeMode
import com.siroha.core.domain.model.WallpaperMode
import com.siroha.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(application) {

    val settings: StateFlow<LauncherSettings> = settingsRepository.observeSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LauncherSettings()
        )

    fun setThemeMode(mode: ThemeMode) = update { it.copy(themeMode = mode) }

    fun setDynamicColorEnabled(enabled: Boolean) = update { it.copy(dynamicColorEnabled = enabled) }

    fun setBlurIntensity(percent: Int) = update { it.copy(blurIntensityPercent = percent.coerceIn(0, 100)) }

    fun setCornerRadius(dp: Int) = update { it.copy(cornerRadiusDp = dp.coerceIn(0, 28)) }

    fun setAnimationsEnabled(enabled: Boolean) = update { it.copy(animationsEnabled = enabled) }

    fun setFontSize(size: Int) = update { it.copy(fontSize = size.coerceIn(10, 24)) }

    fun setWallpaperMode(mode: WallpaperMode) = update { it.copy(wallpaperMode = mode) }

    fun setWallpaper(uri: String) = update { it.copy(wallpaperMode = WallpaperMode.GALLERY, wallpaperUri = uri) }

    fun setAppLockEnabled(enabled: Boolean) = update { it.copy(appLockEnabled = enabled) }

    fun toggleAppLockForApp(componentKey: String) = update {
        val current = it.lockedApps
        it.copy(lockedApps = if (componentKey in current) current - componentKey else current + componentKey)
    }

    fun setDeveloperOption(option: String, enabled: Boolean) = update {
        it.copy(developer = when (option) {
            "fps" -> it.developer.copy(fpsCounterEnabled = enabled)
            "recomposition" -> it.developer.copy(recompositionCounterEnabled = enabled)
            "layout" -> it.developer.copy(layoutBoundariesEnabled = enabled)
            "benchmarks" -> it.developer.copy(benchmarksEnabled = enabled)
            else -> it.developer
        })
    }

    fun updateTaskbar(transform: (TaskbarSettings) -> TaskbarSettings) =
        update { it.copy(taskbar = transform(it.taskbar)) }

    fun updateStartMenu(transform: (StartMenuSettings) -> StartMenuSettings) =
        update { it.copy(startMenu = transform(it.startMenu)) }

    fun updateDesktop(transform: (DesktopSettings) -> DesktopSettings) =
        update { it.copy(desktop = transform(it.desktop)) }

    fun resetToDefaults() {
        viewModelScope.launch { settingsRepository.resetToDefaults() }
    }

    fun exportBackup(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = settingsRepository.exportBackup()
            onResult(json)
        }
    }

    fun importBackup(json: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = settingsRepository.importBackup(json)
            onResult(result.isSuccess, if (result.isSuccess) "Backup restored successfully" else "Failed to restore backup")
        }
    }

    private fun update(transform: (LauncherSettings) -> LauncherSettings) {
        viewModelScope.launch {
            settingsRepository.updateSettings(transform)
        }
    }
}
