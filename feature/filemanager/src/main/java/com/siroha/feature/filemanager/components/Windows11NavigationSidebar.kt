package com.siroha.feature.filemanager.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.siroha.feature.filemanager.FileManagerIcon
import com.siroha.feature.filemanager.QuickAccessEntry
import com.siroha.feature.filemanager.StorageInfo
import java.util.Locale

@Composable
fun Windows11NavigationSidebar(
    quickAccess: List<QuickAccessEntry>,
    currentPath: String,
    storageInfo: StorageInfo?,
    onEntryClick: (QuickAccessEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {
        // Quick Access section
        SidebarSectionHeader("Quick access")
        quickAccess.forEach { entry ->
            SidebarItem(
                icon = quickAccessIcon(entry.icon),
                label = entry.label,
                isSelected = currentPath.startsWith(entry.path),
                onClick = { onEntryClick(entry) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // This PC section
        SidebarSectionHeader("This PC")
        SidebarItem(
            icon = Icons.Filled.DesktopWindows,
            label = "Desktop",
            isSelected = false,
            onClick = { }
        )
        SidebarItem(
            icon = Icons.Filled.Download,
            label = "Downloads",
            isSelected = false,
            onClick = { }
        )
        SidebarItem(
            icon = Icons.Filled.Folder,
            label = "Documents",
            isSelected = false,
            onClick = { }
        )
        SidebarItem(
            icon = Icons.Filled.Image,
            label = "Pictures",
            isSelected = false,
            onClick = { }
        )
        SidebarItem(
            icon = Icons.Filled.MusicNote,
            label = "Music",
            isSelected = false,
            onClick = { }
        )
        SidebarItem(
            icon = Icons.Filled.Movie,
            label = "Videos",
            isSelected = false,
            onClick = { }
        )

        // Storage
        if (storageInfo != null) {
            Spacer(modifier = Modifier.height(16.dp))
            SidebarSectionHeader("Devices and drives")
            StorageDriveCard(storageInfo)
        }
    }
}

@Composable
private fun SidebarSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@Composable
private fun SidebarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 1.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            },
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            }
        )
    }
}

@Composable
private fun StorageDriveCard(storage: StorageInfo) {
    val usedFraction = if (storage.totalBytes > 0) {
        (storage.usedBytes.toFloat() / storage.totalBytes.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Storage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Local Disk (C:)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { usedFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (usedFraction > 0.9f) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${formatGb(storage.freeBytes)} free of ${formatGb(storage.totalBytes)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

private fun formatGb(bytes: Long): String =
    String.format(Locale.getDefault(), "%.1f GB", bytes / 1024.0 / 1024.0 / 1024.0)

private fun quickAccessIcon(icon: FileManagerIcon) = when (icon) {
    FileManagerIcon.INTERNAL_STORAGE -> Icons.Filled.SdStorage
    FileManagerIcon.DOWNLOADS -> Icons.Filled.Download
    FileManagerIcon.DOCUMENTS -> Icons.Filled.Folder
    FileManagerIcon.PICTURES -> Icons.Filled.Image
    FileManagerIcon.MUSIC -> Icons.Filled.MusicNote
    FileManagerIcon.VIDEOS -> Icons.Filled.Movie
    FileManagerIcon.SD_CARD -> Icons.Filled.SdStorage
    FileManagerIcon.FOLDER -> Icons.Filled.Folder
}
