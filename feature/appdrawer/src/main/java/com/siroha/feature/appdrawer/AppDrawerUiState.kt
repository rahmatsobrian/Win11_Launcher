package com.siroha.feature.appdrawer

import android.graphics.Bitmap
import com.siroha.core.domain.model.AppInfo

enum class AppDrawerSortMode { ALPHABETICAL, RECENTLY_INSTALLED, FREQUENTLY_USED }

data class AppDrawerSection(
    val letter: String,
    val apps: List<AppInfo>
)

data class AppDrawerUiState(
    val sections: List<AppDrawerSection> = emptyList(),
    val sortMode: AppDrawerSortMode = AppDrawerSortMode.ALPHABETICAL,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val searchResults: List<AppInfo> = emptyList(),
    val isLoading: Boolean = true,
    val iconBitmaps: Map<String, Bitmap> = emptyMap()
)
