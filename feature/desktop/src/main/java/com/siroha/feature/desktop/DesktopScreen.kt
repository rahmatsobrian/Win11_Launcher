package com.siroha.feature.desktop

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.siroha.core.domain.model.DesktopItem
import com.siroha.core.domain.model.GridPosition
import com.siroha.designsystem.components.AppContextMenu
import com.siroha.designsystem.components.ContextMenuAction
import com.siroha.feature.desktop.components.DesktopGrid
import com.siroha.feature.widgets.WidgetPickerOverlay

@Composable
fun DesktopScreen(
    onOpenApp: (String) -> Unit,
    onOpenStartMenu: () -> Unit,
    viewModel: DesktopViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var contextMenuItem by remember { mutableStateOf<DesktopItem?>(null) }
    var isWidgetPickerVisible by remember { mutableStateOf(false) }
    var pendingWidget by remember { mutableStateOf<Pair<Int, AppWidgetProviderInfo>?>(null) }

    // Allocating an appWidgetId does not by itself grant this app
    // permission to bind that widget — AppWidgetManager requires either
    // bindAppWidgetIdIfAllowed() to succeed silently, or (if it returns
    // false) an explicit user confirmation via ACTION_APPWIDGET_BIND. This
    // launcher handles that second, user-facing path.
    val bindWidgetLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val widget = pendingWidget
        pendingWidget = null
        if (result.resultCode == android.app.Activity.RESULT_OK && widget != null) {
            completeWidgetPlacement(viewModel, widget.first, widget.second, state)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(state.currentPage, state.pageCount) {
                var accumulatedDrag = 0f
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val threshold = 120f
                        when {
                            accumulatedDrag < -threshold && state.currentPage < state.pageCount - 1 ->
                                viewModel.goToPage(state.currentPage + 1)
                            accumulatedDrag > threshold && state.currentPage > 0 ->
                                viewModel.goToPage(state.currentPage - 1)
                        }
                        accumulatedDrag = 0f
                    }
                ) { _, dragAmount ->
                    accumulatedDrag += dragAmount
                }
            }
    ) {
        DesktopGrid(
            items = state.items,
            columns = state.gridColumns,
            rows = state.gridRows,
            iconSizeDp = state.iconSizeDp,
            showLabels = state.showLabels,
            iconBitmaps = state.iconBitmaps,
            appLabels = state.appLabels,
            isLayoutLocked = state.isLayoutLocked,
            widgetHost = viewModel.widgetHost,
            onItemClick = { item -> handleItemClick(item, onOpenApp) },
            onItemLongClick = { item -> contextMenuItem = item },
            onItemMoved = { itemId, newPosition -> viewModel.moveItem(itemId, newPosition) },
            modifier = Modifier.padding(bottom = 56.dp) // reserve space for taskbar
        )

        if (state.pageCount > 1) {
            PageIndicator(
                pageCount = state.pageCount,
                currentPage = state.currentPage,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 64.dp)
            )
        }

        if (!state.isLayoutLocked) {
            FloatingActionButton(
                onClick = { isWidgetPickerVisible = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 72.dp, end = 16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add widget")
            }
        }

        val menuItem = contextMenuItem
        AppContextMenu(
            expanded = menuItem != null,
            onDismiss = { contextMenuItem = null },
            actions = buildDesktopContextActions(
                item = menuItem,
                context = context,
                onRemove = { id -> viewModel.removeItem(id) }
            )
        )

        WidgetPickerOverlay(
            isVisible = isWidgetPickerVisible,
            widgetHost = viewModel.widgetHost,
            onDismiss = { isWidgetPickerVisible = false },
            onWidgetSelected = { appWidgetId, providerInfo ->
                isWidgetPickerVisible = false

                val appWidgetManager = AppWidgetManager.getInstance(context)
                val alreadyBound = appWidgetManager.bindAppWidgetIdIfAllowed(
                    appWidgetId,
                    providerInfo.provider
                )

                if (alreadyBound) {
                    completeWidgetPlacement(viewModel, appWidgetId, providerInfo, state)
                } else {
                    // The system requires explicit user confirmation for this
                    // provider — launch the system bind dialog and finish
                    // placement only if the user approves it.
                    pendingWidget = appWidgetId to providerInfo
                    val bindIntent = android.content.Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, providerInfo.provider)
                    }
                    bindWidgetLauncher.launch(bindIntent)
                }
            }
        )
    }
}

private fun completeWidgetPlacement(
    viewModel: DesktopViewModel,
    appWidgetId: Int,
    providerInfo: AppWidgetProviderInfo,
    state: DesktopUiState
) {
    val spanColumns = (providerInfo.minWidth / 100).coerceIn(1, state.gridColumns)
    val spanRows = (providerInfo.minHeight / 100).coerceIn(1, state.gridRows)
    viewModel.addWidget(
        appWidgetId = appWidgetId,
        spanColumns = spanColumns,
        spanRows = spanRows,
        position = GridPosition(page = state.currentPage, row = 0, column = 0)
    )
}

private fun buildDesktopContextActions(
    item: DesktopItem?,
    context: android.content.Context,
    onRemove: (String) -> Unit
): List<ContextMenuAction> {
    if (item == null) return emptyList()

    val actions = mutableListOf(
        ContextMenuAction(
            label = "Remove from desktop",
            icon = Icons.Filled.Delete,
            isDestructive = true,
            onClick = { onRemove(item.id) }
        )
    )

    if (item is DesktopItem.AppShortcut) {
        val packageName = item.appComponentKey.substringBefore("/")
        if (packageName != "internal") {
            actions += ContextMenuAction(
                label = "App info",
                icon = Icons.Filled.Info,
                onClick = {
                    val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = android.net.Uri.fromParts("package", packageName, null)
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            )
        }
    }

    return actions
}

private fun handleItemClick(item: DesktopItem, onOpenApp: (String) -> Unit) {
    when (item) {
        is DesktopItem.AppShortcut -> onOpenApp(item.appComponentKey)
        is DesktopItem.Folder -> Unit // folder open state handled by a future FolderOverlay component
        is DesktopItem.Widget -> Unit // widgets are interacted with directly, not "opened"
    }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(if (isActive) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                    )
            )
        }
    }
}
