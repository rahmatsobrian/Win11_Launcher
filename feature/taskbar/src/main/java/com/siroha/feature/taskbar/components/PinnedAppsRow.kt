@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.siroha.feature.taskbar.components

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.siroha.core.domain.model.AppInfo
import com.siroha.designsystem.components.AppIcon

@Composable
fun PinnedAppsRow(
    apps: List<AppInfo>,
    iconBitmaps: Map<String, Bitmap>,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(apps, key = { it.componentKey }) { app ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .combinedClickable(
                        onClick = { onAppClick(app) },
                        onLongClick = { onAppLongClick(app) }
                    )
            ) {
                AppIcon(
                    label = app.label,
                    bitmap = iconBitmaps[app.componentKey],
                    size = 36.dp,
                    cornerRadius = 6.dp
                )
            }
        }
    }
}
