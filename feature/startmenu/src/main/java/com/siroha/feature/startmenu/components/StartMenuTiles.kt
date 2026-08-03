@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.siroha.feature.startmenu.components

import android.graphics.Bitmap
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.siroha.core.domain.model.AppInfo
import com.siroha.designsystem.components.AppContextMenu
import com.siroha.designsystem.components.AppIcon
import com.siroha.designsystem.components.ContextMenuAction

@Composable
fun PinnedAppsGrid(
    apps: List<AppInfo>,
    iconBitmaps: Map<String, Bitmap>,
    onAppClick: (AppInfo) -> Unit,
    onUnpin: (AppInfo) -> Unit,
    onPinToTaskbar: (AppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "Pinned",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(apps, key = { it.componentKey }) { app ->
                StartMenuTile(
                    app = app,
                    bitmap = iconBitmaps[app.componentKey],
                    onClick = { onAppClick(app) },
                    onUnpin = { onUnpin(app) },
                    onPinToTaskbar = { onPinToTaskbar(app) }
                )
            }
        }
    }
}

@Composable
fun RecommendedAppsSection(
    apps: List<AppInfo>,
    iconBitmaps: Map<String, Bitmap>,
    onAppClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
        Text(
            text = "Recommended",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(apps, key = { it.componentKey }) { app ->
                StartMenuTile(
                    app = app,
                    bitmap = iconBitmaps[app.componentKey],
                    onClick = { onAppClick(app) },
                    onUnpin = null,
                    onPinToTaskbar = null,
                    compact = true
                )
            }
        }
    }
}

@Composable
private fun StartMenuTile(
    app: AppInfo,
    bitmap: Bitmap?,
    onClick: () -> Unit,
    onUnpin: (() -> Unit)?,
    onPinToTaskbar: (() -> Unit)?,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = if (onUnpin != null || onPinToTaskbar != null) {
                        { showMenu = true }
                    } else {
                        null
                    }
                )
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppIcon(
                label = app.label,
                bitmap = bitmap,
                size = if (compact) 32.dp else 44.dp,
                cornerRadius = 8.dp
            )
            Text(
                text = app.label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        val actions = mutableListOf<ContextMenuAction>()
        if (onUnpin != null) {
            actions.add(ContextMenuAction(label = "Unpin from Start", icon = Icons.Outlined.PushPin, onClick = onUnpin))
        }
        if (onPinToTaskbar != null) {
            actions.add(ContextMenuAction(label = "Pin to taskbar", icon = Icons.Filled.PushPin, onClick = onPinToTaskbar))
        }

        AppContextMenu(
            expanded = showMenu,
            onDismiss = { showMenu = false },
            actions = actions
        )
    }
}
