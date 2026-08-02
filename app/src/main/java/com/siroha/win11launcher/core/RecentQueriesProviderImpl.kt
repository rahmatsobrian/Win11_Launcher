package com.siroha.win11launcher.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.siroha.feature.search.RecentQueriesProvider
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val RECENT_QUERIES_KEY = stringPreferencesKey("recent_search_queries")
private const val MAX_STORED_QUERIES = 20
private const val DELIMITER = "\u001F" // unit separator — won't collide with real query text

@Singleton
class RecentQueriesProviderImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : RecentQueriesProvider {

    override suspend fun getRecentQueries(limit: Int): List<String> {
        val stored = dataStore.data.first()[RECENT_QUERIES_KEY].orEmpty()
        if (stored.isEmpty()) return emptyList()
        return stored.split(DELIMITER).filter { it.isNotBlank() }.take(limit)
    }

    override suspend fun recordQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return

        dataStore.edit { prefs ->
            val current = prefs[RECENT_QUERIES_KEY].orEmpty()
                .split(DELIMITER)
                .filter { it.isNotBlank() && !it.equals(trimmed, ignoreCase = true) }

            val updated = (listOf(trimmed) + current).take(MAX_STORED_QUERIES)
            prefs[RECENT_QUERIES_KEY] = updated.joinToString(DELIMITER)
        }
    }

    override suspend fun clearRecentQueries() {
        dataStore.edit { prefs -> prefs.remove(RECENT_QUERIES_KEY) }
    }
}
