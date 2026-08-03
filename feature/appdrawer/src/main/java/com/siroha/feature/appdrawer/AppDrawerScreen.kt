package com.siroha.feature.appdrawer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.siroha.feature.appdrawer.components.AlphabetIndexSidebar
import com.siroha.feature.appdrawer.components.AppDrawerList
import com.siroha.feature.appdrawer.components.AppDrawerSearchResults
import kotlinx.coroutines.launch

@Composable
fun AppDrawerScreen(
    onOpenApp: (String) -> Unit,
    viewModel: AppDrawerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search apps") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        if (state.isSearching) {
            AppDrawerSearchResults(
                results = state.searchResults,
                iconBitmaps = state.iconBitmaps,
                onAppClick = { app -> onOpenApp(app.componentKey) },
                modifier = Modifier.weight(1f)
            )
        } else {
            Row(modifier = Modifier.weight(1f)) {
                AppDrawerList(
                    sections = state.sections,
                    iconBitmaps = state.iconBitmaps,
                    onAppClick = { app -> onOpenApp(app.componentKey) },
                    onHide = { app -> viewModel.hideApp(app.componentKey) },
                    onPinToTaskbar = { app -> viewModel.pinToTaskbar(app.componentKey) },
                    onPinToStart = { app -> viewModel.pinToStart(app.componentKey) },
                    listState = listState,
                    modifier = Modifier.weight(1f)
                )

                AlphabetIndexSidebar(
                    availableLetters = state.sections.map { it.letter },
                    onLetterClick = { letter ->
                        val sectionIndex = state.sections.indexOfFirst { it.letter == letter }
                        if (sectionIndex >= 0) {
                            coroutineScope.launch {
                                // +1 per preceding section accounts for that section's sticky header row
                                val flatIndex = state.sections.take(sectionIndex)
                                    .sumOf { it.apps.size + 1 }
                                listState.animateScrollToItem(flatIndex)
                            }
                        }
                    }
                )
            }
        }
    }
}
