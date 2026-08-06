package com.siroha.win11launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
        applyImmersiveFullscreen()
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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Re-hide system bars whenever the window regains focus (e.g. after
        // returning from an app the user launched, or dismissing a system
        // dialog) — without this, the status/navigation bars the user
        // swiped in to peek at stay visible indefinitely once this Activity
        // is back in the foreground.
        if (hasFocus) {
            applyImmersiveFullscreen()
        }
    }

    /**
     * Hides both the status bar and navigation bar behind swipe-in gesture
     * (BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE) rather than a hard kiosk
     * lock — the user can still briefly reveal system bars by swiping from
     * a screen edge (e.g. to check a carrier notification icon or pull
     * down the real notification shade), and they auto-hide again shortly
     * after. A true immersive-sticky mode without any escape would make
     * this launcher unable to reach system-level UI at all, which is worse
     * than Windows 11's own always-visible taskbar despite the visual
     * mismatch — the taskbar built into this app already covers clock,
     * battery, wifi, and notifications, so the system bars are redundant
     * chrome most of the time anyway.
     */
    private fun applyImmersiveFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

