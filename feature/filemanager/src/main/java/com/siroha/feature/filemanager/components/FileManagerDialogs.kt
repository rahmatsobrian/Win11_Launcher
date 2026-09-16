package com.siroha.feature.filemanager.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.vector.ImageVector
import com.siroha.feature.filemanager.FileEntry
import com.siroha.feature.filemanager.FileEntryType
import com.siroha.feature.filemanager.FileProperties
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ln
import kotlin.math.pow

@Composable
fun DeleteConfirmDialog(
    entryName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("Delete \"$entryName\"?") },
        text = { Text("This item will be permanently deleted.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun PropertiesDialog(
    entry: FileEntry?,
    properties: FileProperties?,
    onDismiss: () -> Unit
) {
    val name = properties?.name ?: entry?.name ?: "Unknown"
    val type = properties?.type ?: if (entry?.type == FileEntryType.FOLDER) "Folder" else "File"
    val size = properties?.sizeBytes?.let { formatFileSize(it) } ?: entry?.sizeBytes?.let { formatFileSize(it) } ?: "Unknown"
    val modified = (properties?.lastModified ?: entry?.lastModifiedMillis)?.let {
        SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()).format(Date(it))
    } ?: "Unknown"
    val path = properties?.path ?: entry?.path ?: "Unknown"
    val readable = properties?.isReadable?.let { if (it) "Yes" else "No" } ?: "Unknown"
    val writable = properties?.isWritable?.let { if (it) "Yes" else "No" } ?: "Unknown"

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Info, contentDescription = null) },
        title = { Text("Properties") },
        text = {
            PropertyRow("Name", name)
            PropertyRow("Type", type)
            PropertyRow("Size", size)
            PropertyRow("Modified", modified)
            PropertyRow("Path", path)
            PropertyRow("Readable", readable)
            PropertyRow("Writable", writable)
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    )
}

@Composable
private fun PropertyRow(label: String, value: String) {
    androidx.compose.foundation.layout.Column(
        modifier = androidx.compose.ui.Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (ln(bytes.toDouble()) / ln(1024.0)).toInt().coerceIn(0, units.lastIndex)
    val value = bytes / 1024.0.pow(digitGroups)
    return String.format(Locale.getDefault(), "%.1f %s", value, units[digitGroups])
}
