package com.siroha.feature.widgets.components

import android.appwidget.AppWidgetProviderInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun WidgetPickerList(
    providers: List<AppWidgetProviderInfo>,
    onProviderSelected: (AppWidgetProviderInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val packageManager = context.packageManager

    LazyColumn(modifier = modifier) {
        items(providers, key = { it.provider.flattenToString() }) { provider ->
            val label = runCatching { provider.loadLabel(packageManager) }
                .getOrDefault(provider.provider.packageName)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onProviderSelected(provider) }
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(text = label, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = provider.provider.packageName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
