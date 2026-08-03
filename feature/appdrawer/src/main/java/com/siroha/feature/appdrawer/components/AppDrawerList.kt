@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.siroha.feature.appdrawer.components

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.stickyHeader
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.siroha.core.domain.model.AppInfo
import com.siroha.designsystem.components.AppContextMenu
import com.siroha.designsystem.components.AppIcon
import com.siroha.designsystem.components.ContextMenuAction
import com.siroha.feature.appdrawer.AppDrawerSection

@Composable
fun AppDrawerList(
    sections: List<AppDrawerSection>,
    iconBitmaps: Map<String, Bitmap>,
    onAppClick: (AppInfo) -> Unit,
    onHide: (AppInfo) -> Unit,
    onPinToTaskbar: (AppInfo) -> Unit,
    onPinToStart: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    LazyColumn(modifier = modifier, state = listState) {
        sections.forEach { section ->
            stickyHeader(key = "header_${section.letter}") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = section.letter,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            items(section.apps, key = { it.componentKey }) { app ->
                AppDrawerRow(
                    app = app,
                    bitmap = iconBitmaps[app.componentKey],
                    onClick = { onAppClick(app) },
                    onHide = { onHide(app) },
                    onPinToTaskbar = { onPinToTaskbar(app) },
                    onPinToStart = { onPinToStart(app) }
                )
            }
        }
    }
}

@Composable
private fun AppDrawerRow(
    app: AppInfo,
    bitmap: Bitmap?,
    onClick: () -> Unit,
    onHide: () -> Unit,
    onPinToTaskbar: () -> Unit,
    onPinToStart: () -> Unit
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = { showMenu = true })
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(label = app.label, bitmap = bitmap, size = 36.dp, cornerRadius = 8.dp)
            Text(
                text = app.label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        AppContextMenu(
            expanded = showMenu,
            onDismiss = { showMenu = false },
            actions = listOf(
                ContextMenuAction(
                    label = "Pin to Start",
                    icon = Icons.Filled.PushPin,
                    onClick = onPinToStart
                ),
                ContextMenuAction(
                    label = "Pin to taskbar",
                    icon = Icons.Filled.PushPin,
                    onClick = onPinToTaskbar
                ),
                ContextMenuAction(
                    label = "Hide app",
                    icon = Icons.Filled.VisibilityOff,
                    onClick = onHide
                ),
                ContextMenuAction(
                    label = "App info",
                    icon = Icons.Filled.Info,
                    onClick = {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.fromParts("package", app.packageName, null)
                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                )
            )
        )
    }
}
