package com.siroha.feature.startmenu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.siroha.core.domain.model.AppInfo
import com.siroha.designsystem.theme.LocalFluentTokens
import com.siroha.feature.startmenu.components.PinnedAppsGrid
import com.siroha.feature.startmenu.components.RecommendedAppsSection
import com.siroha.feature.startmenu.components.SearchResultsList
import com.siroha.feature.startmenu.components.StartMenuSearchBar

@Composable
fun StartMenuOverlay(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onOpenApp: (String) -> Unit,
    viewModel: StartMenuViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val tokens = LocalFluentTokens.current

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(150)),
        exit = fadeOut(tween(150))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.25f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = scaleIn(tween(200), initialScale = 0.92f) + fadeIn(tween(200)),
                exit = scaleOut(tween(150), targetScale = 0.92f) + fadeOut(tween(150))
            ) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 60.dp)
                        .width(state.widthDp.dp)
                        .height(state.heightDp.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(tokens.taskbarChrome)
                        .clickable(enabled = false) {} // absorb clicks so they don't dismiss via the scrim behind
                ) {
                    StartMenuContent(
                        state = state,
                        onQueryChange = viewModel::onSearchQueryChange,
                        onAppClick = { app -> onOpenApp(app.componentKey) },
                        onUnpin = { app -> viewModel.unpinFromStart(app.componentKey) },
                        onPinToTaskbar = { app -> viewModel.pinToTaskbar(app.componentKey) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StartMenuContent(
    state: StartMenuUiState,
    onQueryChange: (String) -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onUnpin: (AppInfo) -> Unit,
    onPinToTaskbar: (AppInfo) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        StartMenuSearchBar(query = state.searchQuery, onQueryChange = onQueryChange)

        if (state.isSearching) {
            SearchResultsList(
                results = state.searchResults,
                iconBitmaps = state.iconBitmaps,
                onAppClick = onAppClick,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            PinnedAppsGrid(
                apps = state.pinnedApps,
                iconBitmaps = state.iconBitmaps,
                onAppClick = onAppClick,
                onUnpin = onUnpin,
                onPinToTaskbar = onPinToTaskbar
            )
            if (state.showRecommended) {
                RecommendedAppsSection(
                    apps = state.recommendedApps,
                    iconBitmaps = state.iconBitmaps,
                    onAppClick = onAppClick
                )
            }
        }
    }
}
