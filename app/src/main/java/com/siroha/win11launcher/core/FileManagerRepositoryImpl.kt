package com.siroha.win11launcher.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import com.siroha.feature.filemanager.FileEntry
import com.siroha.feature.filemanager.FileManagerIcon
import com.siroha.feature.filemanager.FileManagerRepository
import com.siroha.feature.filemanager.QuickAccessEntry
import com.siroha.feature.filemanager.StorageInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileManagerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : FileManagerRepository {

    override val hasFullStorageAccess: Boolean
        get() = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R ||
            Environment.isExternalStorageManager()

    override fun listEntries(path: String): Flow<List<FileEntry>> = flow {
        val directory = File(path)
        val entries = runCatching {
            directory.listFiles()
                ?.filterNot { it.isHidden }
                ?.map { FileEntry.from(it) }
                ?: emptyList()
        }.getOrDefault(emptyList())
        emit(entries)
    }

    override fun quickAccessEntries(): List<QuickAccessEntry> {
        val root = Environment.getExternalStorageDirectory()
        return listOf(
            QuickAccessEntry("Internal storage", root.absolutePath, FileManagerIcon.INTERNAL_STORAGE),
            QuickAccessEntry(
                "Downloads",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath,
                FileManagerIcon.DOWNLOADS
            ),
            QuickAccessEntry(
                "Documents",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath,
                FileManagerIcon.DOCUMENTS
            ),
            QuickAccessEntry(
                "Pictures",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath,
                FileManagerIcon.PICTURES
            ),
            QuickAccessEntry(
                "Music",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath,
                FileManagerIcon.MUSIC
            ),
            QuickAccessEntry(
                "Movies",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).absolutePath,
                FileManagerIcon.VIDEOS
            )
        )
    }

    override suspend fun createFolder(parentPath: String, name: String): Result<Unit> = runCatching {
        val target = File(parentPath, name)
        if (target.exists()) error("A folder named \"$name\" already exists")
        if (!target.mkdir()) error("Could not create folder")
    }

    override suspend fun rename(path: String, newName: String): Result<Unit> = runCatching {
        val source = File(path)
        val target = File(source.parentFile, newName)
        if (target.exists()) error("\"$newName\" already exists")
        if (!source.renameTo(target)) error("Could not rename")
    }

    override suspend fun delete(path: String): Result<Unit> = runCatching {
        val target = File(path)
        val deleted = if (target.isDirectory) target.deleteRecursively() else target.delete()
        if (!deleted) error("Could not delete \"${target.name}\"")
    }

    override suspend fun getStorageInfo(): StorageInfo {
        val stat = StatFs(Environment.getExternalStorageDirectory().absolutePath)
        val totalBytes = stat.totalBytes
        val freeBytes = stat.availableBytes
        return StorageInfo(
            totalBytes = totalBytes,
            freeBytes = freeBytes,
            usedBytes = totalBytes - freeBytes
        )
    }

    override fun requestFullStorageAccess() {
        val intent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
