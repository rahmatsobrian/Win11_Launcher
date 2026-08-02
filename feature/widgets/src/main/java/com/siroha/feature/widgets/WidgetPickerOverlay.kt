package com.siroha.feature.widgets

import android.appwidget.AppWidgetProviderInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.siroha.feature.widgets.components.WidgetPickerList
import com.siroha.feature.widgets.host.LauncherWidgetHost

/**
 * Full-screen widget picker. Selecting a provider allocates a widget ID via
 * the host, and — if the provider needs user configuration (e.g. a clock
 * widget asking for a timezone) — the caller is expected to launch
 * AppWidgetManager.ACTION_APPWIDGET_CONFIGURE before treating placement as
 * final; onWidgetSelected receives both the id and provider info so the
 * caller (Desktop) can make that decision.
 */
@Composable
fun WidgetPickerOverlay(
    isVisible: Boolean,
    widgetHost: LauncherWidgetHost,
    onDismiss: () -> Unit,
    onWidgetSelected: (appWidgetId: Int, providerInfo: AppWidgetProviderInfo) -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(tween(220)) { it } + fadeIn(tween(220)),
        exit = slideOutVertically(tween(180)) { it } + fadeOut(tween(180))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                Text(text = "Add a widget", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            WidgetPickerList(
                providers = widgetHost.installedProviders(),
                onProviderSelected = { provider ->
                    val appWidgetId = widgetHost.allocateAppWidgetId()
                    onWidgetSelected(appWidgetId, provider)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
