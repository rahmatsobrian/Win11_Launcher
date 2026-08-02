package com.siroha.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siroha.core.domain.model.DesktopSettings
import com.siroha.core.domain.model.LauncherSettings
import com.siroha.core.domain.model.StartMenuSettings
import com.siroha.core.domain.model.TaskbarSettings
import com.siroha.core.domain.model.ThemeMode
import com.siroha.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

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

    fun setAppLockEnabled(enabled: Boolean) = update { it.copy(appLockEnabled = enabled) }

    fun updateTaskbar(transform: (TaskbarSettings) -> TaskbarSettings) =
        update { it.copy(taskbar = transform(it.taskbar)) }

    fun updateStartMenu(transform: (StartMenuSettings) -> StartMenuSettings) =
        update { it.copy(startMenu = transform(it.startMenu)) }

    fun updateDesktop(transform: (DesktopSettings) -> DesktopSettings) =
        update { it.copy(desktop = transform(it.desktop)) }

    fun resetToDefaults() {
        viewModelScope.launch { settingsRepository.resetToDefaults() }
    }

    suspend fun exportBackup(): String = settingsRepository.exportBackup()

    suspend fun importBackup(json: String): Result<Unit> = settingsRepository.importBackup(json)

    private fun update(transform: (LauncherSettings) -> LauncherSettings) {
        viewModelScope.launch {
            settingsRepository.updateSettings(transform)
        }
    }
}
