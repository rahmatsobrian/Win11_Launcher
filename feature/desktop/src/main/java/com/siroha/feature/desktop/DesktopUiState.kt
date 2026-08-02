package com.siroha.feature.desktop

import android.graphics.Bitmap
import com.siroha.core.domain.model.DesktopItem

data class DesktopUiState(
    val currentPage: Int = 0,
    val pageCount: Int = 1,
    val items: List<DesktopItem> = emptyList(),
    val isEditMode: Boolean = false,
    val isLayoutLocked: Boolean = false,
    val gridColumns: Int = 5,
    val gridRows: Int = 6,
    val iconSizeDp: Int = 56,
    val showLabels: Boolean = true,
    val isLoading: Boolean = true,
    val iconBitmaps: Map<String, Bitmap> = emptyMap()
)
