package com.siroha.win11launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.siroha.core.domain.repository.InstalledAppsRepository
import com.siroha.designsystem.theme.Win11LauncherTheme
import com.siroha.feature.taskbar.system.SystemStatusProvider
import com.siroha.feature.widgets.host.LauncherWidgetHost
import com.siroha.win11launcher.core.AppLauncher
import com.siroha.win11launcher.navigation.LauncherRoot
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Single activity that hosts the entire launcher UI. Because this Activity
 * is declared with category HOME/LAUNCHER/DEFAULT, the system treats it as
 * a home-screen replacement candidate. It deliberately avoids finishing on
 * back-press when at the desktop root (see LauncherRoot's overlay-based
 * navigation, which never pops past Desktop).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appLauncher: AppLauncher

    @Inject
    lateinit var systemStatusProvider: SystemStatusProvider

    @Inject
    lateinit var launcherWidgetHost: LauncherWidgetHost

    @Inject
    lateinit var installedAppsRepository: InstalledAppsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycle.addObserver(launcherWidgetHost)

        // The installed-apps table is otherwise only refreshed by
        // BootCompletedReceiver (i.e. after a device reboot). On first
        // install, or if this activity is opened without ever having
        // rebooted, that table is empty — leaving the App Drawer and any
        // desktop icon population with nothing to show. Refreshing here
        // guarantees fresh data every time the launcher UI actually opens,
        // not just after boot.
        lifecycleScope.launch {
            installedAppsRepository.refreshInstalledApps()
        }

        setContent {
            Win11LauncherTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LauncherRoot(appLauncher = appLauncher, systemStatusProvider = systemStatusProvider)
                }
            }
        }
    }
}

