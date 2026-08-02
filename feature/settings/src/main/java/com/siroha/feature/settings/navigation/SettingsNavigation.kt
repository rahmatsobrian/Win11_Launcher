package com.siroha.feature.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.siroha.feature.settings.SettingsScreen

const val SETTINGS_ROUTE = "settings"
const val SETTINGS_ABOUT_ROUTE = "settings/about"
const val SETTINGS_DEVELOPER_OPTIONS_ROUTE = "settings/developer_options"

fun NavGraphBuilder.settingsScreen(navController: NavController) {
    composable(SETTINGS_ROUTE) {
        SettingsScreen(
            onNavigateToAbout = { navController.navigate(SETTINGS_ABOUT_ROUTE) },
            onNavigateToDeveloperOptions = { navController.navigate(SETTINGS_DEVELOPER_OPTIONS_ROUTE) }
        )
    }
}
