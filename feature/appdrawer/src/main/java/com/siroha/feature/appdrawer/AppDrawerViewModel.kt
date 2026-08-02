package com.siroha.feature.appdrawer

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siroha.core.domain.model.AppInfo
import com.siroha.core.domain.repository.IconRepository
import com.siroha.core.domain.repository.InstalledAppsRepository
import com.siroha.core.domain.usecase.SearchLauncherUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
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
class AppDrawerViewModel @Inject constructor(
    private val installedAppsRepository: InstalledAppsRepository,
    private val searchLauncherUseCase: SearchLauncherUseCase,
    private val iconRepository: IconRepository
) : ViewModel() {

    private val sortMode = MutableStateFlow(AppDrawerSortMode.ALPHABETICAL)
    private val searchQuery = MutableStateFlow("")
    private val iconBitmaps = MutableStateFlow<Map<String, Bitmap>>(emptyMap())

    private val searchResults = searchQuery
        .debounce(150)
        .distinctUntilChanged()
        .mapLatest { query -> if (query.isBlank()) emptyList() else searchLauncherUseCase(query).apps }

    private data class DrawerPartialState(
        val allApps: List<AppInfo>,
        val mostUsed: List<AppInfo>,
        val sort: AppDrawerSortMode,
        val query: String,
        val results: List<AppInfo>
    )

    val uiState: StateFlow<AppDrawerUiState> = combine(
        installedAppsRepository.observeInstalledApps(),
        installedAppsRepository.observeMostUsedApps(limit = 100),
        sortMode,
        searchQuery,
        searchResults
    ) { allApps, mostUsed, sort, query, results ->
        DrawerPartialState(allApps, mostUsed, sort, query, results)
    }.combine(iconBitmaps) { partial, icons ->
        val sections = when (partial.sort) {
            AppDrawerSortMode.ALPHABETICAL -> buildAlphabeticalSections(partial.allApps)
            AppDrawerSortMode.RECENTLY_INSTALLED -> listOf(
                AppDrawerSection("Recently Installed", partial.allApps.sortedByDescending { it.installTimeMillis })
            )
            AppDrawerSortMode.FREQUENTLY_USED -> listOf(
                AppDrawerSection("Frequently Used", partial.mostUsed)
            )
        }

        loadMissingIcons(partial.allApps)

        AppDrawerUiState(
            sections = sections,
            sortMode = partial.sort,
            searchQuery = partial.query,
            isSearching = partial.query.isNotBlank(),
            searchResults = partial.results,
            isLoading = false,
            iconBitmaps = icons
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppDrawerUiState()
    )

    private fun loadMissingIcons(apps: List<AppInfo>) {
        val missing = apps.filter { it.componentKey !in iconBitmaps.value }
        if (missing.isEmpty()) return

        viewModelScope.launch {
            missing.forEach { app ->
                val bitmap = iconRepository.getIcon(app.componentKey, app.packageName, app.activityClassName)
                if (bitmap != null) {
                    iconBitmaps.update { it + (app.componentKey to bitmap) }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.update { query }
    }

    fun setSortMode(mode: AppDrawerSortMode) {
        sortMode.update { mode }
    }

    fun hideApp(componentKey: String) {
        viewModelScope.launch {
            installedAppsRepository.setHidden(componentKey, true)
        }
    }

    fun pinToTaskbar(componentKey: String) {
        viewModelScope.launch {
            installedAppsRepository.setPinnedToTaskbar(componentKey, true)
        }
    }

    fun pinToStart(componentKey: String) {
        viewModelScope.launch {
            installedAppsRepository.setPinnedToStart(componentKey, true)
        }
    }

    private fun buildAlphabeticalSections(apps: List<AppInfo>): List<AppDrawerSection> {
        return apps
            .sortedBy { it.label.lowercase() }
            .groupBy { app ->
                val firstChar = app.label.firstOrNull()?.uppercaseChar() ?: '#'
                if (firstChar.isLetter()) firstChar.toString() else "#"
            }
            .toSortedMap()
            .map { (letter, sectionApps) -> AppDrawerSection(letter, sectionApps) }
    }
}
