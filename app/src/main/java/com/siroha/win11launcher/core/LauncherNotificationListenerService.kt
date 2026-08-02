package com.siroha.win11launcher.core

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.siroha.feature.taskbar.notifications.LauncherNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * NotificationListenerService instances are created and destroyed by the
 * system, not by Hilt/DI — so state is exposed via a companion-object
 * singleton StateFlow that NotificationCenterProviderImpl reads from,
 * rather than trying to inject dependencies into this service directly.
 */
class LauncherNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        refreshFromActiveNotifications()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        refreshFromActiveNotifications()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        refreshFromActiveNotifications()
    }

    private fun refreshFromActiveNotifications() {
        val current = runCatching { activeNotifications }.getOrNull() ?: return
        val mapped = current
            .filter { it.isClearable || it.isOngoing.not() }
            .mapNotNull { sbn -> sbn.toLauncherNotification(packageManager) }
            .sortedByDescending { it.postTimeMillis }

        _notifications.value = mapped
    }

    fun dismissByKey(key: String) {
        runCatching { cancelNotification(key) }
    }

    fun dismissAll() {
        runCatching { cancelAllNotifications() }
    }

    companion object {
        private val _notifications = MutableStateFlow<List<LauncherNotification>>(emptyList())
        val notifications = _notifications.asStateFlow()

        /** Set by the service instance while connected; null while access is revoked/service not running. */
        @Volatile
        var activeInstance: LauncherNotificationListenerService? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        activeInstance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        if (activeInstance === this) {
            activeInstance = null
        }
        _notifications.value = emptyList()
    }
}

private fun StatusBarNotification.toLauncherNotification(
    packageManager: android.content.pm.PackageManager
): LauncherNotification? {
    val extras = notification.extras
    val title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString() ?: return null
    val text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString().orEmpty()

    val appLabel = runCatching {
        packageManager.getApplicationLabel(
            packageManager.getApplicationInfo(packageName, 0)
        ).toString()
    }.getOrDefault(packageName)

    return LauncherNotification(
        key = key,
        packageName = packageName,
        appLabel = appLabel,
        title = title,
        text = text,
        postTimeMillis = postTime,
        isClearable = isClearable
    )
}
