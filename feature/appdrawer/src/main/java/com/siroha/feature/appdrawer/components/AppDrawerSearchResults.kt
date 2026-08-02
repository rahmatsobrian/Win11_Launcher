package com.siroha.feature.appdrawer.components

import android.graphics.Bitmap
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.siroha.core.domain.model.AppInfo
import com.siroha.designsystem.components.AppIcon

@Composable
fun AppDrawerSearchResults(
    results: List<AppInfo>,
    iconBitmaps: Map<String, Bitmap>,
    onAppClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    if (results.isEmpty()) {
        Box(modifier = modifier.fillMaxSize().padding(24.dp)) {
            Text(
                text = "No apps found",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
        return
    }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(results, key = { it.componentKey }) { app ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAppClick(app) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIcon(
                    label = app.label,
                    bitmap = iconBitmaps[app.componentKey],
                    size = 36.dp,
                    cornerRadius = 8.dp
                )
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}
