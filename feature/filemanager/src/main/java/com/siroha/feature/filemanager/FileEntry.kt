package com.siroha.feature.filemanager

import java.io.File

enum class FileEntryType { FOLDER, FILE }

data class FileEntry(
    val name: String,
    val path: String,
    val type: FileEntryType,
    val sizeBytes: Long,
    val lastModifiedMillis: Long,
    val mimeType: String?
) {
    companion object {
        fun from(file: File): FileEntry = FileEntry(
            name = file.name,
            path = file.absolutePath,
            type = if (file.isDirectory) FileEntryType.FOLDER else FileEntryType.FILE,
            sizeBytes = if (file.isFile) file.length() else 0L,
            lastModifiedMillis = file.lastModified(),
            mimeType = if (file.isFile) guessMimeType(file.extension) else null
        )

        private fun guessMimeType(extension: String): String? =
            android.webkit.MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(extension.lowercase())
    }
}

/** A quick-access shortcut shown at the top of the file browser, mirroring Windows 11's "This PC" sidebar entries. */
data class QuickAccessEntry(
    val label: String,
    val path: String,
    val icon: FileManagerIcon
)

enum class FileManagerIcon { INTERNAL_STORAGE, DOWNLOADS, DOCUMENTS, PICTURES, MUSIC, VIDEOS, SD_CARD, FOLDER }
