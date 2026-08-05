package com.siroha.win11launcher.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import com.siroha.feature.appdrawer.AppDrawerScreen
import com.siroha.feature.desktop.DesktopScreen
import com.siroha.feature.settings.SettingsScreen
import com.siroha.feature.startmenu.StartMenuOverlay
import com.siroha.feature.taskbar.TaskbarScreen
import com.siroha.feature.taskbar.components.QuickSettingsOverlay
import com.siroha.feature.taskbar.notifications.NotificationCenterOverlay
import com.siroha.feature.taskbar.system.SystemStatusProvider
import com.siroha.win11launcher.core.AppLauncher
import com.siroha.win11launcher.core.SystemControlHelper

private enum class OverlayScreen { NONE, START_MENU, APP_DRAWER, SETTINGS, QUICK_SETTINGS, NOTIFICATION_CENTER }

/**
 * The launcher root is not a conventional back-stack navigation graph:
 * Desktop + Taskbar are always-present layers (this *is* the home screen,
 * so there's no "previous screen" to navigate back to beneath them).
 * Every other screen is an overlay toggled by state rather than a pushed
 * route, which keeps the taskbar visible and interactive underneath them
 * exactly like Windows 11 — clicking the clock opens Quick Settings above
 * the taskbar, not a full navigation transition.
 */
@Composable
fun LauncherRoot(appLauncher: AppLauncher, systemStatusProvider: SystemStatusProvider) {
    var overlay by remember { mutableStateOf(OverlayScreen.NONE) }
    var brightnessPercent by remember { mutableIntStateOf(50) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val systemStatus by systemStatusProvider.observeStatus()
        .collectAsState(
            initial = com.siroha.feature.taskbar.system.SystemStatus(
                timeText = "",
                dateText = "",
                batteryPercent = 100,
                isCharging = false,
                isWifiConnected = false,
                isBluetoothEnabled = false,
                notificationCount = 0
            )
        )

    fun openApp(componentKey: String) {
        overlay = OverlayScreen.NONE
        appLauncher.launch(context, componentKey, coroutineScope)
    }

    // Only intercept back presses while an overlay is showing — with no
    // overlay open, back should fall through to the system default (which
    // for a HOME activity is a no-op, correctly keeping the user on the
    // desktop rather than this composable trying to "close" the launcher).
    BackHandler(enabled = overlay != OverlayScreen.NONE) {
        overlay = OverlayScreen.NONE
    }

    Box(modifier = Modifier.fillMaxSize()) {
        DesktopScreen(
            onOpenApp = { componentKey -> openApp(componentKey) },
            onOpenStartMenu = { overlay = OverlayScreen.START_MENU }
        )

        TaskbarScreen(
            isStartMenuOpen = overlay == OverlayScreen.START_MENU,
            onToggleStartMenu = {
                overlay = if (overlay == OverlayScreen.START_MENU) OverlayScreen.NONE else OverlayScreen.START_MENU
            },
            onOpenApp = { componentKey -> openApp(componentKey) },
            onOpenQuickSettings = { overlay = OverlayScreen.QUICK_SETTINGS },
            onOpenNotificationCenter = { overlay = OverlayScreen.NOTIFICATION_CENTER },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        StartMenuOverlay(
            isVisible = overlay == OverlayScreen.START_MENU,
            onDismiss = { overlay = OverlayScreen.NONE },
            onOpenApp = { componentKey -> openApp(componentKey) },
            onOpenAllApps = { overlay = OverlayScreen.APP_DRAWER }
        )

        AnimatedVisibility(
            visible = overlay == OverlayScreen.APP_DRAWER,
            enter = slideInVertically(tween(220)) { it } + fadeIn(tween(220)),
            exit = slideOutVertically(tween(180)) { it } + fadeOut(tween(180))
        ) {
            AppDrawerScreen(
                onOpenApp = { componentKey -> openApp(componentKey) },
                onDismiss = { overlay = OverlayScreen.NONE }
            )
        }

        AnimatedVisibility(
            visible = overlay == OverlayScreen.SETTINGS,
            enter = slideInVertically(tween(220)) { it } + fadeIn(tween(220)),
            exit = slideOutVertically(tween(180)) { it } + fadeOut(tween(180))
        ) {
            SettingsScreen(
                onNavigateToAbout = { /* pushed as a nested overlay by feature:settings' own state */ },
                onNavigateToDeveloperOptions = { /* same */ }
            )
        }

        QuickSettingsOverlay(
            isVisible = overlay == OverlayScreen.QUICK_SETTINGS,
            isWifiConnected = systemStatus.isWifiConnected,
            isBluetoothEnabled = systemStatus.isBluetoothEnabled,
            brightnessPercent = brightnessPercent,
            onBrightnessChange = { percent ->
                brightnessPercent = percent
                if (SystemControlHelper.canWriteSettings(context)) {
                    SystemControlHelper.setBrightnessPercent(context, percent)
                } else {
                    SystemControlHelper.requestWriteSettingsPermission(context)
                }
            },
            onOpenWifiPanel = { SystemControlHelper.openWifiPanel(context) },
            onOpenBluetoothPanel = { SystemControlHelper.openBluetoothSettings(context) },
            onDismiss = { overlay = OverlayScreen.NONE }
        )

        NotificationCenterOverlay(
            isVisible = overlay == OverlayScreen.NOTIFICATION_CENTER,
            onDismiss = { overlay = OverlayScreen.NONE }
        )
    }
}
