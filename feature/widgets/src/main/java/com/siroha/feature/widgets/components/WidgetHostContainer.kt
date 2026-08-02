package com.siroha.feature.widgets.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.siroha.feature.widgets.host.LauncherWidgetHost

/**
 * Hosts a single AppWidgetHostView inside Compose. AppWidgetHostView must be
 * created imperatively (createView requires a live AppWidgetProviderInfo
 * lookup), so this is an AndroidView interop boundary rather than a pure
 * Composable — attempts to "recompose" the widget instead just recreate the
 * host view, which is intentional since widget content is provider-owned
 * RemoteViews the launcher doesn't control.
 */
@Composable
fun WidgetHostContainer(
    appWidgetId: Int,
    widgetHost: LauncherWidgetHost,
    isLocked: Boolean,
    modifier: Modifier = Modifier
) {
    val baseModifier = modifier
        .fillMaxSize()
        .clip(RoundedCornerShape(12.dp))
        .background(MaterialTheme.colorScheme.surface)

    val decoratedModifier = if (!isLocked) {
        baseModifier.border(width = 1.dp, color = MaterialTheme.colorScheme.outline)
    } else {
        baseModifier
    }

    Box(modifier = decoratedModifier) {
        val providerInfo = widgetHost.getProviderInfo(appWidgetId)
        if (providerInfo != null) {
            AndroidView(
                factory = { ctx ->
                    widgetHost.createView(ctx, appWidgetId, providerInfo).apply {
                        setAppWidget(appWidgetId, providerInfo)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
