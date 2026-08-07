package com.siroha.feature.filemanager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.siroha.designsystem.components.AppContextMenu
import com.siroha.designsystem.components.ContextMenuAction
import com.siroha.feature.filemanager.components.BreadcrumbBar
import com.siroha.feature.filemanager.components.FileEntryList
import com.siroha.feature.filemanager.components.ThisPcPanel

@Composable
fun FileManagerScreen(
    onDismiss: () -> Unit = {},
    viewModel: FileManagerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var contextMenuEntry by remember { mutableStateOf<FileEntry?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "File Explorer",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 8.dp).weight(1f)
            )
            IconButton(onClick = { viewModel.createFolder("New folder") }) {
                Icon(Icons.Filled.Add, contentDescription = "New folder")
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, contentDescription = "Close")
            }
        }
        HorizontalDivider()

        if (!state.hasFullStorageAccess) {
            StorageAccessPrompt(onGrant = { viewModel.requestFullStorageAccess() })
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                ThisPcPanel(
                    quickAccess = state.quickAccess,
                    storageInfo = state.storageInfo,
                    onEntryClick = { entry -> viewModel.navigateTo(entry.path) },
                    modifier = Modifier.width(240.dp)
                )
                HorizontalDivider()

                Column(modifier = Modifier.fillMaxSize()) {
                    BreadcrumbBar(
                        segments = state.pathSegments,
                        onSegmentClick = { segment -> viewModel.navigateTo(segment.path) },
                        onHomeClick = {
                            state.quickAccess.firstOrNull { it.icon == FileManagerIcon.INTERNAL_STORAGE }
                                ?.let { viewModel.navigateTo(it.path) }
                        }
                    )
                    HorizontalDivider()

                    FileEntryList(
                        entries = state.entries,
                        onEntryClick = { entry ->
                            if (entry.type == FileEntryType.FOLDER) {
                                viewModel.navigateTo(entry.path)
                            }
                            // Opening files (not folders) requires resolving a
                            // viewer Intent via PackageManager — left as a
                            // follow-up since it needs an Activity context
                            // this feature module doesn't have direct access to.
                        },
                        onEntryLongClick = { entry -> contextMenuEntry = entry },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        val menuEntry = contextMenuEntry
        AppContextMenu(
            expanded = menuEntry != null,
            onDismiss = { contextMenuEntry = null },
            actions = if (menuEntry != null) {
                listOf(
                    ContextMenuAction(
                        label = "Rename",
                        icon = Icons.Filled.DriveFileRenameOutline,
                        onClick = { /* rename dialog is a follow-up; needs text-input UI */ }
                    ),
                    ContextMenuAction(
                        label = "Delete",
                        icon = Icons.Filled.Delete,
                        isDestructive = true,
                        onClick = { viewModel.delete(menuEntry) }
                    )
                )
            } else {
                emptyList()
            }
        )
    }
}

@Composable
private fun StorageAccessPrompt(onGrant: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "File Explorer needs storage access to browse your files",
                style = MaterialTheme.typography.bodyLarge
            )
            Button(onClick = onGrant, modifier = Modifier.padding(top = 16.dp)) {
                Text("Grant access")
            }
        }
    }
}
