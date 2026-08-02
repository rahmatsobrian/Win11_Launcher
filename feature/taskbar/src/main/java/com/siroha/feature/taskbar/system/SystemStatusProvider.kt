package com.siroha.feature.taskbar.system

import kotlinx.coroutines.flow.Flow

data class SystemStatus(
    val timeText: String,
    val dateText: String,
    val batteryPercent: Int,
    val isCharging: Boolean,
    val isWifiConnected: Boolean,
    val isBluetoothEnabled: Boolean,
    val notificationCount: Int
)

/**
 * Abstraction over BroadcastReceivers (battery, wifi, bluetooth) and a
 * ticking clock. Implemented in the app module (needs a Context registered
 * receiver), injected here as an interface so this feature module and its
 * tests don't depend on Android system services directly.
 */
interface SystemStatusProvider {
    fun observeStatus(): Flow<SystemStatus>
}
