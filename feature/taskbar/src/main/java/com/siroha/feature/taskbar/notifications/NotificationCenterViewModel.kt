package com.siroha.feature.taskbar.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class NotificationCenterViewModel @Inject constructor(
    private val provider: NotificationCenterProvider
) : ViewModel() {

    val hasAccess: Boolean get() = provider.hasAccess

    val notifications: StateFlow<List<LauncherNotification>> = provider.observeNotifications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun dismiss(key: String) = provider.dismiss(key)

    fun clearAll() = provider.dismissAll()

    fun openAccessSettings() = provider.openNotificationAccessSettings()
}
