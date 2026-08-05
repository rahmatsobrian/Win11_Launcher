package com.siroha.feature.appdrawer.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.siroha.feature.appdrawer.AppDrawerScreen

const val APP_DRAWER_ROUTE = "app_drawer"

fun NavGraphBuilder.appDrawerScreen(navController: NavController) {
    composable(APP_DRAWER_ROUTE) {
        AppDrawerScreen(
            onOpenApp = { componentKey ->
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("launch_component_key", componentKey)
                navController.popBackStack()
            },
            onDismiss = { navController.popBackStack() }
        )
    }
}
