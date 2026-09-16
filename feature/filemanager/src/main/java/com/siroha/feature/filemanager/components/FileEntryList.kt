@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.siroha.feature.filemanager.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    selectedEntries: Set<String> = emptySet(),
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(entries, key = { it.path }) { entry ->
            FileEntryRow(
                entry = entry,
                isSelected = entry.path in selectedEntries,
                onClick = { onEntryClick(entry) },
                onLongClick = { onEntryLongClick(entry) }
            )
        }
    }
}

@Composable
private fun FileEntryRow(
    entry: FileEntry,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val bgColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = bgColor,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 1.dp)
            .clip(RoundedCornerShape(4.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = iconFor(entry),
                contentDescription = null,
                tint = if (entry.type == FileEntryType.FOLDER) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                },
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = entry.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = dateFor(entry),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.width(120.dp)
            )
            Text(
                text = typeFor(entry),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.width(80.dp)
            )
            if (entry.type == FileEntryType.FILE) {
                Text(
                    text = formatFileSize(entry.sizeBytes),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.width(70.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(70.dp))
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp).size(16.dp)
                )
            }
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

private fun dateFor(entry: FileEntry): String {
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return dateFormat.format(Date(entry.lastModifiedMillis))
}

private fun typeFor(entry: FileEntry): String {
    return if (entry.type == FileEntryType.FOLDER) {
        "File folder"
    } else {
        entry.mimeType?.substringAfter("/")?.uppercase() ?: entry.name.substringAfterLast('.', "File")
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (ln(bytes.toDouble()) / ln(1024.0)).toInt().coerceIn(0, units.lastIndex)
    val value = bytes / 1024.0.pow(digitGroups)
    return String.format(Locale.getDefault(), "%.1f %s", value, units[digitGroups])
}
