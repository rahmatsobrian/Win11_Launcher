package com.siroha.feature.taskbar.system

import kotlinx.coroutines.flow.Flow

/**
 * Android has no public API to list "running apps" the way desktop OSes do
 * (ActivityManager.getRunningTasks has been restricted to the calling app
 * only since Lollipop, for privacy). UsageStatsManager's recent foreground
 * events are the closest available proxy — a bounded list of apps used
 * within the last few minutes — which is what this models rather than a
 * literal live process list.
 */
interface RunningAppsProvider {
    val hasUsageAccess: Boolean
    fun observeRecentForegroundApps(withinMinutes: Int = 30): Flow<List<String>> // componentKeys / packageNames
    fun openUsageAccessSettings()
}
