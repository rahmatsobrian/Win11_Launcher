package com.siroha.feature.filemanager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.siroha.feature.filemanager.components.*

@Composable
fun FileManagerScreen(
    onBack: () -> Unit,
    viewModel: FileManagerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<FileEntry?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<FileEntry?>(null) }

    if (!uiState.hasFullStorageAccess) {
        StoragePermissionScreen(onRequestAccess = { viewModel.requestFullStorageAccess() })
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // ── Title Bar ──
        Windows11TitleBar(onBack = onBack)

        // ── Toolbar ──
        Windows11Toolbar(
            canGoBack = uiState.pathSegments.size > 1,
            canPaste = uiState.clipboardPath != null,
            isMultiSelectMode = uiState.isMultiSelectMode,
            selectedCount = uiState.selectedEntries.size,
            onBack = { viewModel.navigateUp() },
            onNewFolder = { showNewFolderDialog = true },
            onCut = {
                val entry = uiState.entries.find { it.path in uiState.selectedEntries }
                entry?.let { viewModel.cutFile(it) }
            },
            onCopy = {
                val entry = uiState.entries.find { it.path in uiState.selectedEntries }
                entry?.let { viewModel.copyFile(it) }
            },
            onPaste = { viewModel.pasteFile() },
            onRename = {
                val entry = uiState.entries.find { it.path in uiState.selectedEntries }
                entry?.let { showRenameDialog = it }
            },
            onDelete = {
                if (uiState.isMultiSelectMode) {
                    viewModel.deleteSelected()
                } else {
                    val entry = uiState.entries.find { it.path in uiState.selectedEntries }
                    entry?.let { showDeleteConfirm = it }
                }
            },
            onSelectAll = { viewModel.selectAll() },
            onToggleMultiSelect = { viewModel.toggleMultiSelectMode() },
            onSort = { showSortDialog = true },
            onView = {
                val newMode = if (uiState.viewMode == FileViewMode.LIST) FileViewMode.GRID else FileViewMode.LIST
                viewModel.setViewMode(newMode)
            },
            onClearSelection = { viewModel.clearSelection() }
        )

        // ── Address Bar ──
        Windows11AddressBar(
            segments = uiState.pathSegments,
            searchQuery = uiState.searchQuery,
            onSegmentClick = { segment -> viewModel.navigateTo(segment.path) },
            onHomeClick = {
                val root = android.os.Environment.getExternalStorageDirectory().absolutePath
                viewModel.navigateTo(root)
            },
            onSearchChange = { viewModel.onSearchQueryChange(it) },
            onClearSearch = { viewModel.clearSearch() }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // ── Main Content ──
        Row(modifier = Modifier.fillMaxSize().weight(1f)) {
            // ── Navigation Sidebar ──
            Windows11NavigationSidebar(
                quickAccess = uiState.quickAccess,
                currentPath = uiState.currentPath,
                storageInfo = uiState.storageInfo,
                onEntryClick = { entry -> viewModel.navigateTo(entry.path) },
                modifier = Modifier.width(220.dp).fillMaxHeight()
            )

            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── File List ──
            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                if (uiState.entries.isEmpty() && !uiState.isLoading) {
                    EmptyFolderPlaceholder()
                } else {
                    when (uiState.viewMode) {
                        FileViewMode.LIST -> {
                            FileEntryList(
                                entries = uiState.entries,
                                onEntryClick = { entry ->
                                    if (entry.type == FileEntryType.FOLDER) {
                                        viewModel.navigateTo(entry.path)
                                    } else {
                                        viewModel.toggleSelection(entry.path)
                                    }
                                },
                                onEntryLongClick = { entry ->
                                    viewModel.toggleSelection(entry.path)
                                },
                                selectedEntries = uiState.selectedEntries,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        FileViewMode.GRID -> {
                            FileEntryGrid(
                                entries = uiState.entries,
                                onEntryClick = { entry ->
                                    if (entry.type == FileEntryType.FOLDER) {
                                        viewModel.navigateTo(entry.path)
                                    } else {
                                        viewModel.toggleSelection(entry.path)
                                    }
                                },
                                onEntryLongClick = { entry ->
                                    viewModel.toggleSelection(entry.path)
                                },
                                selectedEntries = uiState.selectedEntries,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        // ── Status Bar ──
        Windows11StatusBar(
            itemCount = uiState.entries.size,
            selectedCount = uiState.selectedEntries.size,
            currentPath = uiState.currentPath
        )
    }

    // ── Dialogs ──
    if (showNewFolderDialog) {
        NewFolderDialog(
            onDismiss = { showNewFolderDialog = false },
            onConfirm = { name ->
                viewModel.createFolder(name)
                showNewFolderDialog = false
            }
        )
    }

    if (showSortDialog) {
        SortDialog(
            currentMode = uiState.sortMode,
            onDismiss = { showSortDialog = false },
            onConfirm = { mode ->
                viewModel.setSortMode(mode)
                showSortDialog = false
            }
        )
    }

    showRenameDialog?.let { entry ->
        RenameDialog(
            currentName = entry.name,
            onDismiss = { showRenameDialog = null },
            onConfirm = { newName ->
                viewModel.rename(entry, newName)
                showRenameDialog = null
            }
        )
    }

    showDeleteConfirm?.let { entry ->
        DeleteConfirmDialog(
            entryName = entry.name,
            onDismiss = { showDeleteConfirm = null },
            onConfirm = {
                viewModel.delete(entry)
                showDeleteConfirm = null
            }
        )
    }

    if (uiState.showingProperties) {
        PropertiesDialog(
            entry = uiState.propertiesEntry,
            properties = uiState.propertiesInfo,
            onDismiss = { viewModel.hideProperties() }
        )
    }
}

@Composable
private fun Windows11TitleBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Folder,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "File Explorer",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Filled.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun Windows11Toolbar(
    canGoBack: Boolean,
    canPaste: Boolean,
    isMultiSelectMode: Boolean,
    selectedCount: Int,
    onBack: () -> Unit,
    onNewFolder: () -> Unit,
    onCut: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onSelectAll: () -> Unit,
    onToggleMultiSelect: () -> Unit,
    onSort: () -> Unit,
    onView: () -> Unit,
    onClearSelection: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Navigation
        IconButton(onClick = onBack, enabled = canGoBack, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.width(4.dp))

        // File operations
        ToolbarButton(
            icon = Icons.Filled.CreateNewFolder,
            label = "New",
            onClick = onNewFolder
        )

        if (isMultiSelectMode && selectedCount > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            ToolbarButton(icon = Icons.Filled.ContentCut, label = "Cut", onClick = onCut)
            Spacer(modifier = Modifier.width(4.dp))
            ToolbarButton(icon = Icons.Filled.ContentCopy, label = "Copy", onClick = onCopy)
            Spacer(modifier = Modifier.width(4.dp))
            ToolbarButton(icon = Icons.Filled.Delete, label = "Delete", onClick = onDelete)
            if (selectedCount == 1) {
                Spacer(modifier = Modifier.width(4.dp))
                ToolbarButton(icon = Icons.Filled.DriveFileRenameOutline, label = "Rename", onClick = onRename)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (canPaste) {
            ToolbarButton(icon = Icons.Filled.ContentPaste, label = "Paste", onClick = onPaste)
            Spacer(modifier = Modifier.width(8.dp))
        }

        // View & Sort
        ToolbarButton(
            icon = if (isMultiSelectMode) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
            label = if (isMultiSelectMode) "Done" else "Select",
            onClick = onToggleMultiSelect
        )
        Spacer(modifier = Modifier.width(4.dp))
        ToolbarButton(icon = Icons.Filled.Sort, label = "Sort", onClick = onSort)
        Spacer(modifier = Modifier.width(4.dp))
        ToolbarButton(icon = Icons.Filled.ViewList, label = "View", onClick = onView)
    }
}

@Composable
private fun ToolbarButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(16.dp),
                tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }
    }
}

@Composable
private fun Windows11AddressBar(
    segments: List<PathSegment>,
    searchQuery: String,
    onSegmentClick: (PathSegment) -> Unit,
    onHomeClick: () -> Unit,
    onSearchChange: (String) -> Unit,
    onClearSearch: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Breadcrumb path
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.weight(1f).height(32.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onHomeClick, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Home, contentDescription = "Root", modifier = Modifier.size(14.dp))
                }
                segments.forEachIndexed { index, segment ->
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        text = segment.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (index == segments.lastIndex) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onSegmentClick(segment) }
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Search box
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.width(200.dp).height(32.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                androidx.compose.foundation.text.BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    "Search",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = onClearSearch, modifier = Modifier.size(16.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear", modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFolderPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.FolderOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "This folder is empty",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun Windows11StatusBar(
    itemCount: Int,
    selectedCount: Int,
    currentPath: String
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth().height(28.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedCount > 0) {
                    "$selectedCount item${if (selectedCount != 1) "s" else ""} selected"
                } else {
                    "$itemCount item${if (itemCount != 1) "s" else ""}"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = currentPath,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 300.dp)
            )
        }
    }
}

@Composable
private fun VerticalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
private fun StoragePermissionScreen(onRequestAccess: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.FolderOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Storage Access Required",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "File Explorer needs access to your device storage to browse files and folders.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestAccess) {
            Icon(Icons.Filled.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Grant Access")
        }
    }
}
