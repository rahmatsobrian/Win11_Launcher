package com.siroha.feature.filemanager

data class FileManagerUiState(
    val currentPath: String = "",
    val pathSegments: List<PathSegment> = emptyList(),
    val entries: List<FileEntry> = emptyList(),
    val quickAccess: List<QuickAccessEntry> = emptyList(),
    val storageInfo: StorageInfo? = null,
    val hasFullStorageAccess: Boolean = false,
    val isLoading: Boolean = true,
    val sortMode: FileSortMode = FileSortMode.NAME,
    val selectedEntry: FileEntry? = null
)

data class PathSegment(val label: String, val path: String)

enum class FileSortMode { NAME, DATE_MODIFIED, SIZE, TYPE }
