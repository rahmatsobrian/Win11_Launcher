package com.siroha.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siroha.core.domain.model.AppInfo
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
class SearchViewModel @Inject constructor(
    private val searchLauncherUseCase: SearchLauncherUseCase,
    private val settingsSearchProvider: SettingsSearchProvider,
    private val contactsSearchProvider: ContactsSearchProvider,
    private val recentQueriesProvider: RecentQueriesProvider
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val recentQueries = MutableStateFlow<List<String>>(emptyList())

    init {
        viewModelScope.launch {
            recentQueries.value = recentQueriesProvider.getRecentQueries()
        }
    }

    private val results = query
        .debounce(150)
        .distinctUntilChanged()
        .mapLatest { q -> performSearch(q) }

    val uiState: StateFlow<SearchUiState> = combine(
        query,
        results,
        recentQueries
    ) { q, categoryResults, recents ->
        SearchUiState(
            query = q,
            results = categoryResults,
            recentQueries = recents,
            isSearching = q.isNotBlank(),
            hasContactsPermission = contactsSearchProvider.hasPermission
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SearchUiState()
    )

    fun onQueryChange(newQuery: String) {
        query.update { newQuery }
    }

    fun onSearchSubmit() {
        val currentQuery = query.value
        if (currentQuery.isBlank()) return
        viewModelScope.launch {
            recentQueriesProvider.recordQuery(currentQuery)
            recentQueries.value = recentQueriesProvider.getRecentQueries()
        }
    }

    fun clearRecentQueries() {
        viewModelScope.launch {
            recentQueriesProvider.clearRecentQueries()
            recentQueries.value = emptyList()
        }
    }

    private suspend fun performSearch(q: String): SearchCategoryResults {
        if (q.isBlank()) return SearchCategoryResults()

        val appResults = searchLauncherUseCase(q).apps.map { app: AppInfo ->
            SearchResultItem.AppResult(
                id = app.componentKey,
                title = app.label,
                subtitle = app.packageName,
                componentKey = app.componentKey
            )
        }

        val settingsResults = settingsSearchProvider.search(q)
        val contactResults = if (contactsSearchProvider.hasPermission) {
            contactsSearchProvider.search(q)
        } else {
            emptyList()
        }

        return SearchCategoryResults(
            apps = appResults,
            settings = settingsResults,
            contacts = contactResults
        )
    }
}
