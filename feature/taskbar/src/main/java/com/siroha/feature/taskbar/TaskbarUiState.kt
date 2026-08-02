package com.siroha.feature.taskbar

import android.graphics.Bitmap
import com.siroha.core.domain.model.AppInfo
import com.siroha.core.domain.model.TaskbarAlignment

data class TaskbarUiState(
    val pinnedApps: List<AppInfo> = emptyList(),
    val runningApps: List<AppInfo> = emptyList(),
    val alignment: TaskbarAlignment = TaskbarAlignment.CENTER,
    val heightDp: Int = 48,
    val transparencyPercent: Int = 80,
    val blurEnabled: Boolean = true,
    val autoHide: Boolean = false,
    val isLocked: Boolean = false,
    val cornerRadiusDp: Int = 18,
    val currentTimeText: String = "",
    val currentDateText: String = "",
    val batteryPercent: Int = 100,
    val isCharging: Boolean = false,
    val isWifiConnected: Boolean = true,
    val isBluetoothEnabled: Boolean = false,
    val notificationCount: Int = 0,
    val isStartMenuOpen: Boolean = false,
    val iconBitmaps: Map<String, Bitmap> = emptyMap()
)
