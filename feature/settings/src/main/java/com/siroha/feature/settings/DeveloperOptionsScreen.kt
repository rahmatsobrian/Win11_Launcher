package com.siroha.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.siroha.feature.settings.components.SettingsSwitchRow

@Composable
fun DeveloperOptionsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Developer Options",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        HorizontalDivider()

        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            SettingsSwitchRow(
                title = "FPS Counter",
                subtitle = "Display frames-per-second overlay on screen",
                checked = settings.developer.fpsCounterEnabled,
                onCheckedChange = { viewModel.setDeveloperOption("fps", it) }
            )
            SettingsSwitchRow(
                title = "Recomposition Counter",
                subtitle = "Show Compose recomposition counts per screen",
                checked = settings.developer.recompositionCounterEnabled,
                onCheckedChange = { viewModel.setDeveloperOption("recomposition", it) }
            )
            SettingsSwitchRow(
                title = "Layout Boundaries",
                subtitle = "Highlight Compose layout boundaries",
                checked = settings.developer.layoutBoundariesEnabled,
                onCheckedChange = { viewModel.setDeveloperOption("layout", it) }
            )
            SettingsSwitchRow(
                title = "Performance Benchmarks",
                subtitle = "Show startup time and frame timing info",
                checked = settings.developer.benchmarksEnabled,
                onCheckedChange = { viewModel.setDeveloperOption("benchmarks", it) }
            )
        }
    }
}
