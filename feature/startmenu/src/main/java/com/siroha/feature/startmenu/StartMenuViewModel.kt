package com.siroha.feature.startmenu

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siroha.core.domain.model.AppInfo
import com.siroha.core.domain.repository.IconRepository
import com.siroha.core.domain.repository.InstalledAppsRepository
import com.siroha.core.domain.repository.SettingsRepository
import com.siroha.core.domain.usecase.SearchLauncherUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class StartMenuViewModel @Inject constructor(
    private val installedAppsRepository: InstalledAppsRepository,
    settingsRepository: SettingsRepository,
    private val searchLauncherUseCase: SearchLauncherUseCase,
    private val iconRepository: IconRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val iconBitmaps = MutableStateFlow<Map<String, Bitmap>>(emptyMap())

    private val searchResults = searchQuery
        .debounce(150)
        .distinctUntilChanged()
        .mapLatest { query -> if (query.isBlank()) emptyList() else searchLauncherUseCase(query).apps }

    private data class StartMenuPartialState(
        val query: String,
        val results: List<AppInfo>,
        val pinned: List<AppInfo>,
        val recommended: List<AppInfo>,
        val settings: com.siroha.core.domain.model.LauncherSettings
    )

    val uiState: StateFlow<StartMenuUiState> = combine(
        searchQuery,
        searchResults,
        installedAppsRepository.observePinnedStartApps(),
        installedAppsRepository.observeMostUsedApps(limit = 6),
        settingsRepository.observeSettings()
    ) { query, results, pinned, recommended, settings ->
        StartMenuPartialState(query, results, pinned, recommended, settings)
    }.combine(iconBitmaps) { partial, icons ->
        loadMissingIcons(partial.pinned + partial.recommended + partial.results)

        StartMenuUiState(
            searchQuery = partial.query,
            searchResults = partial.results,
            pinnedApps = partial.pinned,
            recommendedApps = partial.recommended,
            widthDp = partial.settings.startMenu.widthDp,
            heightDp = partial.settings.startMenu.heightDp,
            pinnedRowCount = partial.settings.startMenu.pinnedRowCount,
            showRecommended = partial.settings.startMenu.showRecommended,
            isSearching = partial.query.isNotBlank(),
            isLoading = false,
            iconBitmaps = icons
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StartMenuUiState()
    )

    private fun loadMissingIcons(apps: List<AppInfo>) {
        val missing = apps.distinctBy { it.componentKey }.filter { it.componentKey !in iconBitmaps.value }
        if (missing.isEmpty()) return

        viewModelScope.launch {
            kotlinx.coroutines.coroutineScope {
                missing.map { app ->
                    async {
                        val bitmap = iconRepository.getIcon(app.componentKey, app.packageName, app.activityClassName)
                        if (bitmap != null) {
                            iconBitmaps.update { it + (app.componentKey to bitmap) }
                        }
                    }
                }.awaitAll()
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.update { query }
    }

    fun clearSearch() {
        searchQuery.value = ""
    }

    fun unpinFromStart(componentKey: String) {
        viewModelScope.launch {
            installedAppsRepository.setPinnedToStart(componentKey, false)
        }
    }

    fun pinToTaskbar(componentKey: String) {
        viewModelScope.launch {
            installedAppsRepository.setPinnedToTaskbar(componentKey, true)
        }
    }
}
