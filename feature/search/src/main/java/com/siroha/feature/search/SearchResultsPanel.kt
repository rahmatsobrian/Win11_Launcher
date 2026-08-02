package com.siroha.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SearchResultsPanel(
    onOpenApp: (String) -> Unit,
    onOpenSettingsRoute: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = modifier.fillMaxWidth()) {
        if (!state.isSearching) {
            RecentQueriesSection(
                queries = state.recentQueries,
                onQueryClick = viewModel::onQueryChange,
                onClear = viewModel::clearRecentQueries
            )
            return@Column
        }

        if (state.results.apps.isNotEmpty()) {
            SectionHeader("Apps")
            LazyColumn {
                items(state.results.apps, key = { it.id }) { result ->
                    ResultRow(title = result.title, subtitle = result.subtitle) {
                        onOpenApp(result.componentKey)
                    }
                }
            }
        }

        if (state.results.settings.isNotEmpty()) {
            SectionHeader("Settings")
            LazyColumn {
                items(state.results.settings, key = { it.id }) { result ->
                    ResultRow(title = result.title, subtitle = result.subtitle) {
                        onOpenSettingsRoute(result.settingsRoute)
                    }
                }
            }
        }

        if (state.hasContactsPermission && state.results.contacts.isNotEmpty()) {
            SectionHeader("Contacts")
            LazyColumn {
                items(state.results.contacts, key = { it.id }) { result ->
                    ResultRow(title = result.title, subtitle = result.subtitle) {
                        // Contact tap action (dial/open) is wired at the app-module
                        // level since it requires an Intent + Activity context.
                    }
                }
            }
        }

        if (state.results.isEmpty) {
            Text(
                text = "No results for \"${state.query}\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}

@Composable
private fun RecentQueriesSection(
    queries: List<String>,
    onQueryClick: (String) -> Unit,
    onClear: () -> Unit
) {
    if (queries.isEmpty()) return
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Recent searches",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        queries.forEach { q ->
            Text(
                text = q,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .clickable { onQueryClick(q) }
                    .padding(vertical = 6.dp)
            )
        }
        TextButton(onClick = onClear) {
            Text("Clear recent searches")
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
    )
}

@Composable
private fun ResultRow(title: String, subtitle: String?, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}
