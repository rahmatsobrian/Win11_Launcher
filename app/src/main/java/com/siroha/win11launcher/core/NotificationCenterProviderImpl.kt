package com.siroha.win11launcher.core

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.siroha.feature.taskbar.notifications.LauncherNotification
import com.siroha.feature.taskbar.notifications.NotificationCenterProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationCenterProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : NotificationCenterProvider {

    override val hasAccess: Boolean
        get() = NotificationManagerCompat.getEnabledListenerPackages(context)
            .contains(context.packageName)

    override fun observeNotifications(): Flow<List<LauncherNotification>> =
        LauncherNotificationListenerService.notifications

    override fun dismiss(key: String) {
        LauncherNotificationListenerService.activeInstance?.dismissByKey(key)
    }

    override fun dismissAll() {
        LauncherNotificationListenerService.activeInstance?.dismissAll()
    }

    override fun openNotificationAccessSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
