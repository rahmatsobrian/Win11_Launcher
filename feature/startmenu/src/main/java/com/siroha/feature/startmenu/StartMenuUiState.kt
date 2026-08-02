package com.siroha.feature.startmenu

import android.graphics.Bitmap
import com.siroha.core.domain.model.AppInfo

data class StartMenuUiState(
    val searchQuery: String = "",
    val searchResults: List<AppInfo> = emptyList(),
    val pinnedApps: List<AppInfo> = emptyList(),
    val recommendedApps: List<AppInfo> = emptyList(),
    val widthDp: Int = 640,
    val heightDp: Int = 720,
    val pinnedRowCount: Int = 4,
    val showRecommended: Boolean = true,
    val isSearching: Boolean = false,
    val isLoading: Boolean = true,
    val iconBitmaps: Map<String, Bitmap> = emptyMap()
)
