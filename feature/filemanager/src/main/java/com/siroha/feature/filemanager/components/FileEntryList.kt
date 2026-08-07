package com.siroha.feature.filemanager.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.siroha.feature.filemanager.FileEntry
import com.siroha.feature.filemanager.FileEntryType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ln
import kotlin.math.pow

@Composable
fun FileEntryList(
    entries: List<FileEntry>,
    onEntryClick: (FileEntry) -> Unit,
    onEntryLongClick: (FileEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(entries, key = { it.path }) { entry ->
            FileEntryRow(
                entry = entry,
                onClick = { onEntryClick(entry) },
                onLongClick = { onEntryLongClick(entry) }
            )
        }
    }
}

@Composable
private fun FileEntryRow(
    entry: FileEntry,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = iconFor(entry),
            contentDescription = null,
            tint = if (entry.type == FileEntryType.FOLDER) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            },
            modifier = Modifier.padding(end = 16.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitleFor(entry),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

private fun iconFor(entry: FileEntry) = when {
    entry.type == FileEntryType.FOLDER -> Icons.Filled.Folder
    entry.mimeType?.startsWith("image/") == true -> Icons.Filled.Image
    entry.mimeType?.startsWith("audio/") == true -> Icons.Filled.MusicNote
    entry.mimeType?.startsWith("video/") == true -> Icons.Filled.Movie
    else -> Icons.Filled.Description
}

private fun subtitleFor(entry: FileEntry): String {
    val dateFormat = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault())
    val dateText = dateFormat.format(Date(entry.lastModifiedMillis))
    return if (entry.type == FileEntryType.FOLDER) {
        dateText
    } else {
        "${formatFileSize(entry.sizeBytes)} · $dateText"
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (ln(bytes.toDouble()) / ln(1024.0)).toInt().coerceIn(0, units.lastIndex)
    val value = bytes / 1024.0.pow(digitGroups)
    return String.format(Locale.getDefault(), "%.1f %s", value, units[digitGroups])
}
