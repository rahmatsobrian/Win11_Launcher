package com.siroha.feature.filemanager.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.siroha.feature.filemanager.FileManagerIcon
import com.siroha.feature.filemanager.QuickAccessEntry
import com.siroha.feature.filemanager.StorageInfo
import java.util.Locale

@Composable
fun ThisPcPanel(
    quickAccess: List<QuickAccessEntry>,
    storageInfo: StorageInfo?,
    onEntryClick: (QuickAccessEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Quick access",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        quickAccess.forEach { entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onEntryClick(entry) }
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = quickAccessIcon(entry.icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text(text = entry.label, style = MaterialTheme.typography.bodyLarge)
            }
        }

        if (storageInfo != null) {
            Text(
                text = "Storage",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )
            StorageUsageCard(storageInfo)
        }
    }
}

@Composable
private fun StorageUsageCard(storage: StorageInfo) {
    val usedFraction = if (storage.totalBytes > 0) {
        (storage.usedBytes.toFloat() / storage.totalBytes.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.SdStorage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                text = "Internal storage",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        LinearProgressIndicator(
            progress = { usedFraction },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
        Text(
            text = "${formatGb(storage.usedBytes)} used of ${formatGb(storage.totalBytes)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

private fun formatGb(bytes: Long): String =
    String.format(Locale.getDefault(), "%.1f GB", bytes / 1024.0 / 1024.0 / 1024.0)

private fun quickAccessIcon(icon: FileManagerIcon) = when (icon) {
    FileManagerIcon.INTERNAL_STORAGE -> Icons.Filled.SdStorage
    FileManagerIcon.DOWNLOADS -> Icons.Filled.Download
    FileManagerIcon.DOCUMENTS -> Icons.Filled.Folder
    FileManagerIcon.PICTURES -> Icons.Filled.Image
    FileManagerIcon.MUSIC -> Icons.Filled.LibraryMusic
    FileManagerIcon.VIDEOS -> Icons.Filled.Movie
    FileManagerIcon.SD_CARD -> Icons.Filled.SdStorage
    FileManagerIcon.FOLDER -> Icons.Filled.Folder
}
