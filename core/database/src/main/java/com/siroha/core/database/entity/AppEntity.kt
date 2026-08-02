package com.siroha.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apps")
data class AppEntity(
    @PrimaryKey val componentKey: String,
    val packageName: String,
    val activityClassName: String,
    val label: String,
    val userHandleId: Int,
    val isSystemApp: Boolean,
    val installTimeMillis: Long,
    val isHidden: Boolean = false,
    val isPinnedToTaskbar: Boolean = false,
    val isPinnedToStart: Boolean = false,
    val category: String = "UNCATEGORIZED",
    val launchCount: Int = 0,
    val lastLaunchedMillis: Long = 0L
)
