package com.siroha.win11launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.siroha.designsystem.theme.Win11LauncherTheme
import com.siroha.feature.taskbar.system.SystemStatusProvider
import com.siroha.feature.widgets.host.LauncherWidgetHost
import com.siroha.win11launcher.core.AppLauncher
import com.siroha.win11launcher.navigation.LauncherRoot
import dagger.hilt.android.AndroidEntryPoint
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycle.addObserver(launcherWidgetHost)
        setContent {
            Win11LauncherTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LauncherRoot(appLauncher = appLauncher, systemStatusProvider = systemStatusProvider)
                }
            }
        }
    }
}

