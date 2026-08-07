package com.siroha.feature.taskbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
    isStartMenuOpen: Boolean,
    onToggleStartMenu: () -> Unit,
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

    // Flush against the bottom edge with no outer margin or rounded
    // corners — matching the real Windows 11 taskbar, which is a solid bar
    // spanning the full screen width rather than a floating rounded pill.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(state.heightDp.dp)
            .background(tokens.taskbarChrome)
    ) {
        if (state.alignment == TaskbarAlignment.CENTER) {
            // Windows 11's centered taskbar treats the Start button and
            // pinned/running apps as a single visually grouped unit that
            // sits together in the middle — not Start pinned to one edge
            // with icons centered independently in the remaining space.
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TaskbarStartAndApps(
                    state = state,
                    isStartMenuOpen = isStartMenuOpen,
                    onToggleStartMenu = onToggleStartMenu,
                    onOpenApp = onOpenApp,
                    contextMenuApp = contextMenuApp,
                    onContextMenuAppChange = { contextMenuApp = it },
                    onUnpin = { viewModel.unpinFromTaskbar(it) },
                    context = context
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TaskbarStartAndApps(
                    state = state,
                    isStartMenuOpen = isStartMenuOpen,
                    onToggleStartMenu = onToggleStartMenu,
                    onOpenApp = onOpenApp,
                    contextMenuApp = contextMenuApp,
                    onContextMenuAppChange = { contextMenuApp = it },
                    onUnpin = { viewModel.unpinFromTaskbar(it) },
                    context = context
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
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

@Composable
private fun TaskbarStartAndApps(
    state: TaskbarUiState,
    isStartMenuOpen: Boolean,
    onToggleStartMenu: () -> Unit,
    onOpenApp: (String) -> Unit,
    contextMenuApp: AppInfo?,
    onContextMenuAppChange: (AppInfo?) -> Unit,
    onUnpin: (String) -> Unit,
    context: android.content.Context
) {
    StartButton(
        isActive = isStartMenuOpen,
        onClick = onToggleStartMenu
    )

    Box(modifier = Modifier.padding(start = 4.dp)) {
        PinnedAppsRow(
            apps = state.pinnedApps,
            iconBitmaps = state.iconBitmaps,
            onAppClick = { app -> onOpenApp(app.componentKey) },
            onAppLongClick = { app -> onContextMenuAppChange(app) }
        )

        val menuApp = contextMenuApp
        AppContextMenu(
            expanded = menuApp != null,
            onDismiss = { onContextMenuAppChange(null) },
            actions = if (menuApp != null) {
                buildList {
                    add(
                        ContextMenuAction(
                            label = "Unpin from taskbar",
                            icon = Icons.Filled.PushPin,
                            onClick = { onUnpin(menuApp.componentKey) }
                        )
                    )
                    if (menuApp.packageName != "internal") {
                        add(
                            ContextMenuAction(
                                label = "App info",
                                icon = Icons.Filled.Info,
                                onClick = { openAppInfoSettings(context, menuApp.packageName) }
                            )
                        )
                    }
                }
            } else {
                emptyList()
            }
        )
    }
}

private fun openAppInfoSettings(context: android.content.Context, packageName: String) {
    val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = android.net.Uri.fromParts("package", packageName, null)
        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}
