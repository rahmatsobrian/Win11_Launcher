package com.siroha.feature.filemanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FileManagerViewModel @Inject constructor(
    private val repository: FileManagerRepository
) : ViewModel() {

    private val currentPath = MutableStateFlow(rootPathOrEmpty())
    private val sortMode = MutableStateFlow(FileSortMode.NAME)
    private val storageInfo = MutableStateFlow<StorageInfo?>(null)

    init {
        viewModelScope.launch {
            storageInfo.value = repository.getStorageInfo()
        }
    }

    val uiState: StateFlow<FileManagerUiState> = combine(
        currentPath,
        currentPath.flatMapLatest { path -> repository.listEntries(path) },
        sortMode,
        storageInfo
    ) { path, entries, sort, storage ->
        FileManagerUiState(
            currentPath = path,
            pathSegments = buildBreadcrumb(path),
            entries = sortEntries(entries, sort),
            quickAccess = repository.quickAccessEntries(),
            storageInfo = storage,
            hasFullStorageAccess = repository.hasFullStorageAccess,
            isLoading = false,
            sortMode = sort
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FileManagerUiState(hasFullStorageAccess = repository.hasFullStorageAccess)
    )

    fun navigateTo(path: String) {
        currentPath.value = path
    }

    fun navigateUp(): Boolean {
        val parent = java.io.File(currentPath.value).parent ?: return false
        currentPath.value = parent
        return true
    }

    fun setSortMode(mode: FileSortMode) {
        sortMode.update { mode }
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            repository.createFolder(currentPath.value, name)
        }
    }

    fun rename(entry: FileEntry, newName: String) {
        viewModelScope.launch {
            repository.rename(entry.path, newName)
        }
    }

    fun delete(entry: FileEntry) {
        viewModelScope.launch {
            repository.delete(entry.path)
        }
    }

    fun requestFullStorageAccess() {
        repository.requestFullStorageAccess()
    }

    private fun sortEntries(entries: List<FileEntry>, mode: FileSortMode): List<FileEntry> {
        // Folders always list before files, matching Windows Explorer's
        // default grouping — the chosen sort mode only orders within each group.
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
