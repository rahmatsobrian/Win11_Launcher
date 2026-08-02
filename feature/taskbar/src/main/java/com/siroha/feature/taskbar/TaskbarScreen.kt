package com.siroha.feature.taskbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.siroha.core.domain.model.AppInfo
import com.siroha.core.domain.model.TaskbarAlignment
import com.siroha.designsystem.components.AppContextMenu
import com.siroha.designsystem.components.ContextMenuAction
import com.siroha.designsystem.theme.LocalFluentTokens
import com.siroha.feature.taskbar.components.ClockDateTray
import com.siroha.feature.taskbar.components.PinnedAppsRow
import com.siroha.feature.taskbar.components.StartButton
import com.siroha.feature.taskbar.components.SystemTray

@Composable
fun TaskbarScreen(
    onOpenApp: (String) -> Unit,
    onOpenQuickSettings: () -> Unit,
    onOpenNotificationCenter: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskbarViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val tokens = LocalFluentTokens.current
    val context = LocalContext.current
    var contextMenuApp by remember { mutableStateOf<AppInfo?>(null) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(state.heightDp.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(state.cornerRadiusDp.dp))
            .background(tokens.taskbarChrome)
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (state.alignment == TaskbarAlignment.CENTER) {
                Arrangement.Center
            } else {
                Arrangement.Start
            }
        ) {
            StartButton(
                isActive = state.isStartMenuOpen,
                onClick = { viewModel.toggleStartMenu() }
            )

            Box(modifier = Modifier.padding(start = 8.dp)) {
                PinnedAppsRow(
                    apps = state.pinnedApps,
                    iconBitmaps = state.iconBitmaps,
                    onAppClick = { app -> onOpenApp(app.componentKey) },
                    onAppLongClick = { app -> contextMenuApp = app }
                )

                val menuApp = contextMenuApp
                AppContextMenu(
                    expanded = menuApp != null,
                    onDismiss = { contextMenuApp = null },
                    actions = if (menuApp != null) {
                        listOf(
                            ContextMenuAction(
                                label = "Unpin from taskbar",
                                icon = Icons.Filled.PushPin,
                                onClick = { viewModel.unpinFromTaskbar(menuApp.componentKey) }
                            ),
                            ContextMenuAction(
                                label = "App info",
                                icon = Icons.Filled.Info,
                                onClick = { openAppInfoSettings(context, menuApp.packageName) }
                            )
                        )
                    } else {
                        emptyList()
                    }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SystemTray(
                isWifiConnected = state.isWifiConnected,
                isBluetoothEnabled = state.isBluetoothEnabled,
                batteryPercent = state.batteryPercent,
                isCharging = state.isCharging,
                notificationCount = state.notificationCount,
                onClick = onOpenNotificationCenter
            )
            ClockDateTray(
                timeText = state.currentTimeText,
                dateText = state.currentDateText,
                onClick = onOpenQuickSettings,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

private fun openAppInfoSettings(context: android.content.Context, packageName: String) {
    val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = android.net.Uri.fromParts("package", packageName, null)
        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}
