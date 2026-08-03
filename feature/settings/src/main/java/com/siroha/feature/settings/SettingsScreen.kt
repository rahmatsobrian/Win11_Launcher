package com.siroha.feature.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.siroha.core.domain.model.TaskbarAlignment
import com.siroha.feature.settings.components.SettingsNavigationRow
import com.siroha.feature.settings.components.SettingsSliderRow
import com.siroha.feature.settings.components.SettingsSwitchRow

@Composable
fun SettingsScreen(
    onNavigateToAbout: () -> Unit,
    onNavigateToDeveloperOptions: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { SectionLabel("Appearance") }
        item {
            SettingsSwitchRow(
                title = "Dynamic color",
                subtitle = "Use wallpaper-based accent color (Android 12+)",
                checked = settings.dynamicColorEnabled,
                onCheckedChange = viewModel::setDynamicColorEnabled
            )
        }
        item {
            SettingsSliderRow(
                title = "Blur intensity",
                value = settings.blurIntensityPercent,
                valueRange = 0..100,
                onValueChange = viewModel::setBlurIntensity,
                valueLabel = { "$it%" }
            )
        }
        item {
            SettingsSliderRow(
                title = "Corner radius",
                value = settings.cornerRadiusDp,
                valueRange = 0..28,
                onValueChange = viewModel::setCornerRadius,
                valueLabel = { "${it}dp" }
            )
        }
        item {
            SettingsSwitchRow(
                title = "Animations",
                checked = settings.animationsEnabled,
                onCheckedChange = viewModel::setAnimationsEnabled
            )
        }
        item { HorizontalDivider() }

        item { SectionLabel("Taskbar") }
        item {
            SettingsSwitchRow(
                title = "Center-align icons",
                checked = settings.taskbar.alignment == TaskbarAlignment.CENTER,
                onCheckedChange = { centered ->
                    viewModel.updateTaskbar {
                        it.copy(alignment = if (centered) TaskbarAlignment.CENTER else TaskbarAlignment.LEFT)
                    }
                }
            )
        }
        item {
            SettingsSwitchRow(
                title = "Auto-hide taskbar",
                checked = settings.taskbar.autoHide,
                onCheckedChange = { auto -> viewModel.updateTaskbar { it.copy(autoHide = auto) } }
            )
        }
        item {
            SettingsSwitchRow(
                title = "Lock taskbar",
                checked = settings.taskbar.isLocked,
                onCheckedChange = { locked -> viewModel.updateTaskbar { it.copy(isLocked = locked) } }
            )
        }
        item {
            SettingsSliderRow(
                title = "Taskbar transparency",
                value = settings.taskbar.transparencyPercent,
                valueRange = 0..100,
                onValueChange = { v -> viewModel.updateTaskbar { it.copy(transparencyPercent = v) } },
                valueLabel = { "$it%" }
            )
        }
        item { HorizontalDivider() }

        item { SectionLabel("Start Menu") }
        item {
            SettingsSwitchRow(
                title = "Show recommended apps",
                checked = settings.startMenu.showRecommended,
                onCheckedChange = { show -> viewModel.updateStartMenu { it.copy(showRecommended = show) } }
            )
        }
        item {
            SettingsSliderRow(
                title = "Pinned rows",
                value = settings.startMenu.pinnedRowCount,
                valueRange = 2..6,
                onValueChange = { rows -> viewModel.updateStartMenu { it.copy(pinnedRowCount = rows) } }
            )
        }
        item { HorizontalDivider() }

        item { SectionLabel("Desktop") }
        item {
            SettingsSliderRow(
                title = "Grid columns",
                value = settings.desktop.gridColumns,
                valueRange = 3..8,
                onValueChange = { cols -> viewModel.updateDesktop { it.copy(gridColumns = cols) } }
            )
        }
        item {
            SettingsSliderRow(
                title = "Grid rows",
                value = settings.desktop.gridRows,
                valueRange = 3..10,
                onValueChange = { rows -> viewModel.updateDesktop { it.copy(gridRows = rows) } }
            )
        }
        item {
            SettingsSwitchRow(
                title = "Show icon labels",
                checked = settings.desktop.showLabels,
                onCheckedChange = { show -> viewModel.updateDesktop { it.copy(showLabels = show) } }
            )
        }
        item {
            SettingsSwitchRow(
                title = "Lock desktop layout",
                checked = settings.desktop.isLayoutLocked,
                onCheckedChange = { locked -> viewModel.updateDesktop { it.copy(isLayoutLocked = locked) } }
            )
        }
        item { HorizontalDivider() }

        item { SectionLabel("Security") }
        item {
            SettingsSwitchRow(
                title = "App Lock",
                subtitle = "Require biometric authentication to open locked apps",
                checked = settings.appLockEnabled,
                onCheckedChange = viewModel::setAppLockEnabled
            )
        }
        item { HorizontalDivider() }

        item { SectionLabel("Advanced") }
        item {
            SettingsNavigationRow(
                title = "Developer options",
                subtitle = "FPS counter, recomposition counter, benchmarks",
                onClick = onNavigateToDeveloperOptions
            )
        }
        item {
            SettingsNavigationRow(
                title = "About",
                subtitle = "Version, licenses, feedback",
                onClick = onNavigateToAbout
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 4.dp)
    )
}
