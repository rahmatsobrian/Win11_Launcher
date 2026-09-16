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
    val viewMode: FileViewMode = FileViewMode.LIST,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val clipboardPath: String? = null,
    val clipboardMode: ClipboardMode? = null,
    val selectedEntries: Set<String> = emptySet(),
    val isMultiSelectMode: Boolean = false,
    val showingProperties: Boolean = false,
    val propertiesEntry: FileEntry? = null,
    val propertiesInfo: FileProperties? = null
)

data class PathSegment(val label: String, val path: String)

enum class FileSortMode { NAME, DATE_MODIFIED, SIZE, TYPE }

enum class FileViewMode { LIST, GRID }

enum class ClipboardMode { COPY, CUT }
