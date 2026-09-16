package com.siroha.feature.filemanager

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FileManagerViewModel @Inject constructor(
    private val repository: FileManagerRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val currentPath = MutableStateFlow(rootPathOrEmpty())
    private val entries = MutableStateFlow<List<FileEntry>>(emptyList())
    private val sortMode = MutableStateFlow(FileSortMode.NAME)
    private val viewMode = MutableStateFlow(FileViewMode.LIST)
    private val storageInfo = MutableStateFlow<StorageInfo?>(null)
    private val searchQuery = MutableStateFlow("")
    private val clipboardPath = MutableStateFlow<String?>(null)
    private val clipboardMode = MutableStateFlow<ClipboardMode?>(null)
    private val selectedEntries = MutableStateFlow<Set<String>>(emptySet())
    private val isMultiSelectMode = MutableStateFlow(false)
    private val showingProperties = MutableStateFlow(false)
    private val propertiesEntry = MutableStateFlow<FileEntry?>(null)
    private val propertiesInfo = MutableStateFlow<FileProperties?>(null)

    val uiState: StateFlow<FileManagerUiState> = combine(
        currentPath,
        entries,
        sortMode,
        viewMode,
        storageInfo,
        searchQuery,
        clipboardPath,
        clipboardMode,
        selectedEntries,
        isMultiSelectMode,
        showingProperties,
        propertiesEntry,
        propertiesInfo
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val path = values[0] as String
        val allEntries = values[1] as List<FileEntry>
        val sort = values[2] as FileSortMode
        val view = values[3] as FileViewMode
        val storage = values[4] as StorageInfo?
        val query = values[5] as String
        val clipPath = values[6] as String?
        val clipMode = values[7] as ClipboardMode?
        val selected = values[8] as Set<String>
        val multiSelect = values[9] as Boolean
        val propsShowing = values[10] as Boolean
        val propsEntry = values[11] as FileEntry?
        val propsInfo = values[12] as FileProperties?

        val filteredEntries = if (query.isNotBlank()) {
            allEntries.filter { it.name.contains(query, ignoreCase = true) }
        } else {
            allEntries
        }

        FileManagerUiState(
            currentPath = path,
            pathSegments = buildBreadcrumb(path),
            entries = sortEntries(filteredEntries, sort),
            quickAccess = repository.quickAccessEntries(),
            storageInfo = storage,
            hasFullStorageAccess = repository.hasFullStorageAccess,
            isLoading = false,
            sortMode = sort,
            viewMode = view,
            searchQuery = query,
            isSearching = query.isNotBlank(),
            clipboardPath = clipPath,
            clipboardMode = clipMode,
            selectedEntries = selected,
            isMultiSelectMode = multiSelect,
            showingProperties = propsShowing,
            propertiesEntry = propsEntry,
            propertiesInfo = propsInfo
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FileManagerUiState(hasFullStorageAccess = repository.hasFullStorageAccess)
    )

    init {
        loadEntries()
        viewModelScope.launch {
            storageInfo.value = repository.getStorageInfo()
        }
    }

    fun navigateTo(path: String) {
        val dir = File(path)
        if (!dir.exists() || !dir.isDirectory) return
        currentPath.value = dir.absolutePath
        clearSelection()
        loadEntries()
    }

    fun navigateUp(): Boolean {
        val parent = File(currentPath.value).parent ?: return false
        currentPath.value = parent
        clearSelection()
        loadEntries()
        return true
    }

    fun setSortMode(mode: FileSortMode) {
        sortMode.value = mode
    }

    fun setViewMode(mode: FileViewMode) {
        viewMode.value = mode
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun clearSearch() {
        searchQuery.value = ""
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            val result = repository.createFolder(currentPath.value, name)
            if (result.isSuccess) {
                Toast.makeText(context, "Folder created", Toast.LENGTH_SHORT).show()
                loadEntries()
            } else {
                Toast.makeText(context, "Failed to create folder", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun rename(entry: FileEntry, newName: String) {
        viewModelScope.launch {
            val result = repository.rename(entry.path, newName)
            if (result.isSuccess) {
                Toast.makeText(context, "Renamed to $newName", Toast.LENGTH_SHORT).show()
                loadEntries()
            } else {
                Toast.makeText(context, "Failed to rename", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun delete(entry: FileEntry) {
        viewModelScope.launch {
            val result = repository.delete(entry.path)
            if (result.isSuccess) {
                Toast.makeText(context, "Deleted: ${entry.name}", Toast.LENGTH_SHORT).show()
                loadEntries()
            } else {
                Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            val paths = selectedEntries.value.toList()
            paths.forEach { path -> repository.delete(path) }
            clearSelection()
            loadEntries()
        }
    }

    fun copyFile(entry: FileEntry) {
        clipboardPath.value = entry.path
        clipboardMode.value = ClipboardMode.COPY
        Toast.makeText(context, "Copied: ${entry.name}", Toast.LENGTH_SHORT).show()
    }

    fun cutFile(entry: FileEntry) {
        clipboardPath.value = entry.path
        clipboardMode.value = ClipboardMode.CUT
        Toast.makeText(context, "Cut: ${entry.name}", Toast.LENGTH_SHORT).show()
    }

    fun pasteFile() {
        val source = clipboardPath.value ?: return
        val destDir = currentPath.value
        val mode = clipboardMode.value ?: return

        viewModelScope.launch {
            val result = when (mode) {
                ClipboardMode.COPY -> repository.copy(source, destDir)
                ClipboardMode.CUT -> repository.move(source, destDir)
            }
            if (result.isSuccess) {
                Toast.makeText(context, "Pasted successfully", Toast.LENGTH_SHORT).show()
                clipboardPath.value = null
                clipboardMode.value = null
                loadEntries()
            } else {
                Toast.makeText(context, "Failed to paste", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun clearClipboard() {
        clipboardPath.value = null
        clipboardMode.value = null
    }

    fun toggleMultiSelectMode() {
        isMultiSelectMode.update { !it }
        if (!isMultiSelectMode.value) {
            selectedEntries.value = emptySet()
        }
    }

    fun toggleSelection(path: String) {
        selectedEntries.update { current ->
            if (path in current) current - path else current + path
        }
    }

    fun selectAll() {
        selectedEntries.value = entries.value.map { it.path }.toSet()
    }

    fun clearSelection() {
        selectedEntries.value = emptySet()
        isMultiSelectMode.value = false
    }

    fun showProperties(entry: FileEntry) {
        viewModelScope.launch {
            propertiesEntry.value = entry
            propertiesInfo.value = repository.getFileProperties(entry.path)
            showingProperties.value = true
        }
    }

    fun hideProperties() {
        showingProperties.value = false
        propertiesEntry.value = null
        propertiesInfo.value = null
    }

    fun requestFullStorageAccess() {
        repository.requestFullStorageAccess()
    }

    private fun loadEntries() {
        viewModelScope.launch {
            repository.listEntries(currentPath.value).collect { newEntries ->
                entries.value = newEntries
            }
        }
    }

    private fun sortEntries(entries: List<FileEntry>, mode: FileSortMode): List<FileEntry> {
        val (folders, files) = entries.partition { it.type == FileEntryType.FOLDER }
        val comparator: Comparator<FileEntry> = when (mode) {
            FileSortMode.NAME -> compareBy { it.name.lowercase() }
            FileSortMode.DATE_MODIFIED -> compareByDescending { it.lastModifiedMillis }
            FileSortMode.SIZE -> compareByDescending { it.sizeBytes }
            FileSortMode.TYPE -> compareBy { it.mimeType ?: "" }
        }
        return folders.sortedWith(comparator) + files.sortedWith(comparator)
    }

    private fun buildBreadcrumb(path: String): List<PathSegment> {
        if (path.isBlank()) return emptyList()
        val segments = mutableListOf<PathSegment>()
        var accumulated = ""
        path.trim('/').split('/').forEach { part ->
            if (part.isBlank()) return@forEach
            accumulated += "/$part"
            segments += PathSegment(label = part, path = accumulated)
        }
        return segments
    }

    private fun rootPathOrEmpty(): String =
        android.os.Environment.getExternalStorageDirectory()?.absolutePath.orEmpty()
}
