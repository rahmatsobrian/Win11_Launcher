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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DeveloperOptionsScreen(
    onNavigateBack: () -> Unit
) {
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

        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Debug Tools",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            DeveloperOptionItem(
                title = "FPS Counter",
                subtitle = "Display frames-per-second overlay",
                comingSoon = true
            )

            DeveloperOptionItem(
                title = "Recomposition Counter",
                subtitle = "Show Compose recomposition counts per screen",
                comingSoon = true
            )

            DeveloperOptionItem(
                title = "Layout Boundaries",
                subtitle = "Highlight Compose layout boundaries",
                comingSoon = true
            )

            DeveloperOptionItem(
                title = "Performance Benchmarks",
                subtitle = "Run startup and rendering benchmarks",
                comingSoon = true
            )
        }
    }
}

@Composable
private fun DeveloperOptionItem(title: String, subtitle: String, comingSoon: Boolean = false) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            if (comingSoon) {
                Text(
                    text = "Coming soon",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            }
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
