package com.siroha.feature.taskbar.notifications

import kotlinx.coroutines.flow.Flow

data class LauncherNotification(
    val key: String,
    val packageName: String,
    val appLabel: String,
    val title: String,
    val text: String,
    val postTimeMillis: Long,
    val isClearable: Boolean
)

/**
 * Backed by NotificationListenerService, which requires the user to
 * manually grant "Notification access" in system Settings — there is no
 * runtime permission dialog for it. hasAccess reflects whether that grant
 * is currently active so the UI can prompt the user to enable it instead
 * of silently showing an empty notification center.
 */
interface NotificationCenterProvider {
    val hasAccess: Boolean
    fun observeNotifications(): Flow<List<LauncherNotification>>
    fun dismiss(key: String)
    fun dismissAll()
    fun openNotificationAccessSettings()
}
