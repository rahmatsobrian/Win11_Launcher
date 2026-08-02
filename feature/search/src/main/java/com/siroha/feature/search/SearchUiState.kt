package com.siroha.feature.search

data class SearchUiState(
    val query: String = "",
    val results: SearchCategoryResults = SearchCategoryResults(),
    val recentQueries: List<String> = emptyList(),
    val isSearching: Boolean = false,
    val hasContactsPermission: Boolean = false
)
