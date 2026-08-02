package com.siroha.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "desktop_items")
data class DesktopItemEntity(
    @PrimaryKey val id: String,
    val type: String, // "SHORTCUT" | "FOLDER" | "WIDGET"
    val page: Int,
    val row: Int,
    val column: Int,

    // Shortcut fields
    val appComponentKey: String? = null,
    val customLabel: String? = null,

    // Folder fields
    val folderName: String? = null,
    /** Comma-separated component keys; fine for the expected small folder sizes. */
    val folderMemberKeysCsv: String? = null,

    // Widget fields
    val appWidgetId: Int? = null,
    val spanColumns: Int? = null,
    val spanRows: Int? = null,
    val isLocked: Boolean = false
)
