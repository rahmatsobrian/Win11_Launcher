package com.siroha.feature.search

/**
 * Settings and Contacts search need to query outside this module's
 * boundary (feature:settings' route table, and ContactsContract which
 * requires a runtime permission check). Implemented in the app module,
 * which is free to depend on every feature module, then injected here —
 * this avoids feature:search depending on feature:settings directly.
 */
interface SettingsSearchProvider {
    suspend fun search(query: String): List<SearchResultItem.SettingResult>
}

interface ContactsSearchProvider {
    suspend fun search(query: String): List<SearchResultItem.ContactResult>
    val hasPermission: Boolean
}

interface RecentQueriesProvider {
    suspend fun getRecentQueries(limit: Int = 5): List<String>
    suspend fun recordQuery(query: String)
    suspend fun clearRecentQueries()
}
