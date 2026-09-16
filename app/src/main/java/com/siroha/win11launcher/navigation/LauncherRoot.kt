package com.siroha.win11launcher.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.siroha.feature.appdrawer.AppDrawerScreen
import com.siroha.feature.desktop.DesktopScreen
import com.siroha.feature.filemanager.FileManagerScreen
import com.siroha.feature.settings.AboutScreen
import com.siroha.feature.settings.DeveloperOptionsScreen
import com.siroha.feature.settings.SettingsScreen
import com.siroha.feature.settings.SettingsViewModel
import com.siroha.feature.startmenu.StartMenuOverlay
import com.siroha.feature.taskbar.TaskbarScreen
import com.siroha.feature.taskbar.components.QuickSettingsOverlay
import com.siroha.feature.taskbar.notifications.NotificationCenterOverlay
import com.siroha.feature.taskbar.system.SystemStatusProvider
import com.siroha.win11launcher.core.AppLauncher
import com.siroha.win11launcher.core.SystemControlHelper

private enum class OverlayScreen { NONE, START_MENU, APP_DRAWER, SETTINGS, QUICK_SETTINGS, NOTIFICATION_CENTER, FILE_MANAGER, CALCULATOR, SETTINGS_ABOUT, SETTINGS_DEVELOPER }

private const val INTERNAL_SETTINGS = "internal/settings/0"
private const val INTERNAL_FILE_MANAGER = "internal/filemanager/0"
private const val INTERNAL_CALCULATOR = "internal/calculator/0"

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

    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settings by settingsViewModel.settings.collectAsState()

    // Developer debug overlays
    var lastFrameTimeNanos by remember { mutableLongStateOf(System.nanoTime()) }
    var fps by remember { mutableIntStateOf(0) }
    var recompositionCount by remember { mutableIntStateOf(0) }
    val view = LocalView.current

    // FPS counter and recomposition tracking
    if (settings.developer.fpsCounterEnabled || settings.developer.benchmarksEnabled || settings.developer.recompositionCounterEnabled) {
        androidx.compose.runtime.SideEffect {
            recompositionCount++
            val now = System.nanoTime()
            val delta = now - lastFrameTimeNanos
            if (delta > 0) {
                fps = (1_000_000_000L / delta).toInt()
            }
            lastFrameTimeNanos = now
        }
    }

    fun openApp(componentKey: String) {
        when (componentKey) {
            INTERNAL_SETTINGS -> overlay = OverlayScreen.SETTINGS
            INTERNAL_FILE_MANAGER -> overlay = OverlayScreen.FILE_MANAGER
            INTERNAL_CALCULATOR -> overlay = OverlayScreen.CALCULATOR
            else -> {
                overlay = OverlayScreen.NONE
                appLauncher.launch(context, componentKey, coroutineScope)
            }
        }
    }

    BackHandler(enabled = overlay != OverlayScreen.NONE) {
        overlay = when (overlay) {
            OverlayScreen.SETTINGS_ABOUT -> OverlayScreen.SETTINGS
            OverlayScreen.SETTINGS_DEVELOPER -> OverlayScreen.SETTINGS
            else -> OverlayScreen.NONE
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Enable Compose layout boundaries if the developer option is on
        if (settings.developer.layoutBoundariesEnabled) {
            val context = LocalContext.current
            androidx.compose.runtime.DisposableEffect(Unit) {
                // Enable layout bounds via system property for debugging
                try {
                    android.os.SystemProperties.set("debug.layout", "true")
                } catch (_: Exception) { }
                onDispose {
                    try {
                        android.os.SystemProperties.set("debug.layout", "false")
                    } catch (_: Exception) { }
                }
            }
        }

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
                onNavigateToAbout = { overlay = OverlayScreen.SETTINGS_ABOUT },
                onNavigateToDeveloperOptions = { overlay = OverlayScreen.SETTINGS_DEVELOPER }
            )
        }

        AnimatedVisibility(
            visible = overlay == OverlayScreen.SETTINGS_ABOUT,
            enter = slideInVertically(tween(220)) { it } + fadeIn(tween(220)),
            exit = slideOutVertically(tween(180)) { it } + fadeOut(tween(180))
        ) {
            AboutScreen(onNavigateBack = { overlay = OverlayScreen.SETTINGS })
        }

        AnimatedVisibility(
            visible = overlay == OverlayScreen.SETTINGS_DEVELOPER,
            enter = slideInVertically(tween(220)) { it } + fadeIn(tween(220)),
            exit = slideOutVertically(tween(180)) { it } + fadeOut(tween(180))
        ) {
            DeveloperOptionsScreen(
                onNavigateBack = { overlay = OverlayScreen.SETTINGS },
                viewModel = settingsViewModel
            )
        }

        AnimatedVisibility(
            visible = overlay == OverlayScreen.FILE_MANAGER,
            enter = slideInVertically(tween(220)) { it } + fadeIn(tween(220)),
            exit = slideOutVertically(tween(180)) { it } + fadeOut(tween(180))
        ) {
            FileManagerScreen(
                onDismiss = { overlay = OverlayScreen.NONE },
                onOpenFile = { filePath ->
                    val file = java.io.File(filePath)
                    val uri = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, context.contentResolver.getType(uri))
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    runCatching {
                        context.startActivity(intent)
                    }
                    overlay = OverlayScreen.NONE
                }
            )
        }

        AnimatedVisibility(
            visible = overlay == OverlayScreen.CALCULATOR,
            enter = slideInVertically(tween(220)) { it } + fadeIn(tween(220)),
            exit = slideOutVertically(tween(180)) { it } + fadeOut(tween(180))
        ) {
            com.siroha.win11launcher.core.CalculatorScreen(
                onDismiss = { overlay = OverlayScreen.NONE }
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

        // Developer Debug Overlays
        if (settings.developer.fpsCounterEnabled) {
            Text(
                text = "FPS: $fps",
                style = MaterialTheme.typography.labelMedium,
                color = androidx.compose.ui.graphics.Color(0xFF00FF00),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(
                        color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        if (settings.developer.recompositionCounterEnabled) {
            Text(
                text = "Recomps: $recompositionCount",
                style = MaterialTheme.typography.labelMedium,
                color = androidx.compose.ui.graphics.Color(0xFF00BFFF),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 8.dp, top = if (settings.developer.fpsCounterEnabled) 36.dp else 8.dp)
                    .background(
                        color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        if (settings.developer.benchmarksEnabled) {
            Text(
                text = "Render: ${if (fps > 0) "${1000 / fps}ms" else "N/A"} | FPS: $fps | Recomps: $recompositionCount",
                style = MaterialTheme.typography.labelMedium,
                color = androidx.compose.ui.graphics.Color(0xFFFFD700),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(
                        color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
