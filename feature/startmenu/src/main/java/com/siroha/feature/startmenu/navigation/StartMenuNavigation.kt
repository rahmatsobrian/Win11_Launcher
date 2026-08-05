package com.siroha.feature.startmenu.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.siroha.feature.startmenu.StartMenuOverlay

const val START_MENU_ROUTE = "start_menu"

/**
 * Registered as a normal NavHost destination so it participates in the
 * back stack (back press closes it), but visually renders as a bottom-
 * anchored overlay via StartMenuOverlay rather than a full-screen page.
 */
fun NavGraphBuilder.startMenuScreen(navController: NavController) {
    composable(START_MENU_ROUTE) {
        StartMenuOverlay(
            isVisible = true,
            onDismiss = { navController.popBackStack() },
            onOpenApp = { componentKey ->
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("launch_component_key", componentKey)
                navController.popBackStack()
            },
            onOpenAllApps = { navController.navigate("app_drawer") }
        )
    }
}
