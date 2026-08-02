package com.siroha.feature.desktop.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.siroha.feature.desktop.DesktopScreen

const val DESKTOP_ROUTE = "desktop"

fun NavGraphBuilder.desktopScreen(navController: NavController) {
    composable(DESKTOP_ROUTE) {
        DesktopScreen(
            onOpenApp = { componentKey ->
                // Actual startActivity() call lives in the app module's
                // AppLauncher helper, invoked via a callback threaded down
                // from MainActivity — kept out of this feature module since
                // launching activities needs an Activity Context.
                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.set("launch_component_key", componentKey)
            },
            onOpenStartMenu = {
                navController.navigate("start_menu")
            }
        )
    }
}
