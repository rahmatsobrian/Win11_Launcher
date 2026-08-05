package com.siroha.feature.taskbar

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siroha.core.domain.model.AppInfo
import com.siroha.core.domain.repository.IconRepository
import com.siroha.core.domain.repository.InstalledAppsRepository
import com.siroha.core.domain.repository.SettingsRepository
import com.siroha.feature.taskbar.system.RunningAppsProvider
import com.siroha.feature.taskbar.system.SystemStatus
import com.siroha.feature.taskbar.system.SystemStatusProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskbarViewModel @Inject constructor(
    private val installedAppsRepository: InstalledAppsRepository,
    settingsRepository: SettingsRepository,
    systemStatusProvider: SystemStatusProvider,
    runningAppsProvider: RunningAppsProvider,
    private val iconRepository: IconRepository
) : ViewModel() {

    private val iconBitmaps = MutableStateFlow<Map<String, Bitmap>>(emptyMap())

    // Step 1: apps-related data (all apps needed to resolve running
    // packageNames back into full AppInfo, plus pinned apps).
    private data class AppsPartial(
        val allApps: List<AppInfo>,
        val pinnedApps: List<AppInfo>,
        val runningPackageNames: List<String>
    )

    private val appsPartial = combine(
        installedAppsRepository.observeInstalledApps(),
        installedAppsRepository.observePinnedTaskbarApps(),
        runningAppsProvider.observeRecentForegroundApps()
    ) { allApps, pinnedApps, runningPackageNames ->
        AppsPartial(allApps, pinnedApps, runningPackageNames)
    }

    // Step 2: system chrome data (settings + live system status).
    private data class ChromePartial(
        val settings: com.siroha.core.domain.model.LauncherSettings,
        val status: SystemStatus
    )

    private val chromePartial = combine(
        settingsRepository.observeSettings(),
        systemStatusProvider.observeStatus()
    ) { settings, status -> ChromePartial(settings, status) }

    val uiState: StateFlow<TaskbarUiState> = combine(
        appsPartial,
        chromePartial,
        iconBitmaps
    ) { apps, chrome, icons ->
        val runningApps = apps.allApps.filter { it.packageName in apps.runningPackageNames }
        loadMissingIcons(apps.pinnedApps + runningApps)

        TaskbarUiState(
            pinnedApps = apps.pinnedApps,
            runningApps = runningApps,
            alignment = chrome.settings.taskbar.alignment,
            heightDp = chrome.settings.taskbar.heightDp,
            transparencyPercent = chrome.settings.taskbar.transparencyPercent,
            blurEnabled = chrome.settings.taskbar.blurEnabled,
            autoHide = chrome.settings.taskbar.autoHide,
            isLocked = chrome.settings.taskbar.isLocked,
            cornerRadiusDp = chrome.settings.taskbar.cornerRadiusDp,
            currentTimeText = chrome.status.timeText,
            currentDateText = chrome.status.dateText,
            batteryPercent = chrome.status.batteryPercent,
            isCharging = chrome.status.isCharging,
            isWifiConnected = chrome.status.isWifiConnected,
            isBluetoothEnabled = chrome.status.isBluetoothEnabled,
            notificationCount = chrome.status.notificationCount,
            iconBitmaps = icons
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TaskbarUiState()
    )

    private fun loadMissingIcons(apps: List<AppInfo>) {
        val missing = apps.distinctBy { it.componentKey }.filter { it.componentKey !in iconBitmaps.value }
        if (missing.isEmpty()) return

        viewModelScope.launch {
            kotlinx.coroutines.coroutineScope {
                missing.map { app ->
                    async {
                        val bitmap = iconRepository.getIcon(app.componentKey, app.packageName, app.activityClassName)
                        if (bitmap != null) {
                            iconBitmaps.update { it + (app.componentKey to bitmap) }
                        }
                    }
                }.awaitAll()
            }
        }
    }

    fun unpinFromTaskbar(componentKey: String) {
        viewModelScope.launch {
            installedAppsRepository.setPinnedToTaskbar(componentKey, false)
        }
    }
}
