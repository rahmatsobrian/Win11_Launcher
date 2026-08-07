package com.siroha.feature.filemanager

import kotlinx.coroutines.flow.Flow

/**
 * Backed by java.io.File against paths Android's Scoped Storage model
 * (API 29+) still allows direct File access to: the app's own external
 * files dir, and — with MANAGE_EXTERNAL_STORAGE granted — the shared
 * storage root. Without that special permission, browsing outside the
 * app's sandbox will return empty/inaccessible results rather than a
 * crash; hasFullStorageAccess tells the UI which mode it's in so it can
 * show a permission prompt instead of a silently empty folder.
 */
interface FileManagerRepository {
    val hasFullStorageAccess: Boolean

    fun listEntries(path: String): Flow<List<FileEntry>>

    fun quickAccessEntries(): List<QuickAccessEntry>

    suspend fun createFolder(parentPath: String, name: String): Result<Unit>

    suspend fun rename(path: String, newName: String): Result<Unit>

    suspend fun delete(path: String): Result<Unit>

    suspend fun getStorageInfo(): StorageInfo

    fun requestFullStorageAccess()
}

data class StorageInfo(
    val totalBytes: Long,
    val freeBytes: Long,
    val usedBytes: Long
)
