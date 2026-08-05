package com.siroha.feature.desktop.components

import android.graphics.Bitmap
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.siroha.core.domain.model.DesktopItem
import com.siroha.core.domain.model.GridPosition
import com.siroha.feature.widgets.components.WidgetHostContainer
import com.siroha.feature.widgets.host.LauncherWidgetHost

/**
 * Absolute-position grid: computes cell size from the available Box size
 * divided by column/row count, then offsets each item by row*cellHeight /
 * column*cellWidth. Chosen over LazyVerticalGrid because desktop items need
 * free 2D placement (an item can occupy any row/column, including gaps left
 * by moved/removed icons) rather than sequential flow layout.
 *
 * Drag is a long-press-then-drag gesture (matches Android's own launcher
 * convention: long-press enters "move mode", a plain tap opens the app).
 * Widgets are draggable the same way but are not tap-clickable (their
 * RemoteViews content owns tap handling) and span multiple cells rather
 * than a single icon-sized cell.
 */
@Composable
fun DesktopGrid(
    items: List<DesktopItem>,
    columns: Int,
    rows: Int,
    iconSizeDp: Int,
    showLabels: Boolean,
    iconBitmaps: Map<String, Bitmap>,
    appLabels: Map<String, String>,
    isLayoutLocked: Boolean,
    widgetHost: LauncherWidgetHost?,
    onItemClick: (DesktopItem) -> Unit,
    onItemLongClick: (DesktopItem) -> Unit,
    onItemMoved: (itemId: String, newPosition: GridPosition) -> Unit,
    modifier: Modifier = Modifier
) {
    var containerWidthPx by remember { mutableStateOf(0) }
    var containerHeightPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    // Tracks the item currently being dragged and its live pixel offset
    // (relative to its snapped grid origin) so recomposition only touches
    // the one dragged item rather than re-laying-out the whole grid.
    var draggedItemId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                containerWidthPx = it.width
                containerHeightPx = it.height
            }
    ) {
        if (containerWidthPx == 0 || containerHeightPx == 0) return@Box

        val cellWidthPx = containerWidthPx / columns
        val cellHeightPx = containerHeightPx / rows
        val cellWidthDp = with(density) { cellWidthPx.toDp() }
        val cellHeightDp = with(density) { cellHeightPx.toDp() }

        items.forEach { item ->
            val isDragging = item.id == draggedItemId
            val baseXOffset = cellWidthDp * item.position.column
            val baseYOffset = cellHeightDp * item.position.row

            val xOffset: Dp
            val yOffset: Dp
            if (isDragging) {
                xOffset = baseXOffset + with(density) { dragOffset.x.toDp() }
                yOffset = baseYOffset + with(density) { dragOffset.y.toDp() }
            } else {
                xOffset = baseXOffset
                yOffset = baseYOffset
            }

            val dragModifier = Modifier
                .offset(x = xOffset, y = yOffset)
                .size(width = cellWidthDp, height = cellHeightDp)
                .alpha(if (isDragging) 0.75f else 1f)
                .let { base ->
                    if (isLayoutLocked) {
                        base
                    } else {
                        base.pointerInput(item.id, cellWidthPx, cellHeightPx) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    draggedItemId = item.id
                                    dragOffset = Offset.Zero
                                    onItemLongClick(item)
                                },
                                onDrag = { change, delta ->
                                    change.consume()
                                    dragOffset += delta
                                },
                                onDragEnd = {
                                    val columnDelta = kotlin.math.round(dragOffset.x / cellWidthPx).toInt()
                                    val rowDelta = kotlin.math.round(dragOffset.y / cellHeightPx).toInt()

                                    val newColumn = (item.position.column + columnDelta).coerceIn(0, columns - 1)
                                    val newRow = (item.position.row + rowDelta).coerceIn(0, rows - 1)

                                    if (newColumn != item.position.column || newRow != item.position.row) {
                                        onItemMoved(
                                            item.id,
                                            GridPosition(page = item.position.page, row = newRow, column = newColumn)
                                        )
                                    }

                                    draggedItemId = null
                                    dragOffset = Offset.Zero
                                },
                                onDragCancel = {
                                    draggedItemId = null
                                    dragOffset = Offset.Zero
                                }
                            )
                        }
                    }
                }

            when (item) {
                is DesktopItem.AppShortcut -> {
                    DesktopIcon(
                        label = item.customLabel ?: appLabels[item.appComponentKey] ?: item.appComponentKey.substringBefore("/").substringAfterLast('.'),
                        showLabel = showLabels,
                        iconSizeDp = iconSizeDp,
                        bitmap = iconBitmaps[item.appComponentKey],
                        modifier = dragModifier,
                        onClick = { if (draggedItemId == null) onItemClick(item) }
                    )
                }
                is DesktopItem.Folder -> {
                    DesktopIcon(
                        label = item.name,
                        showLabel = showLabels,
                        iconSizeDp = iconSizeDp,
                        bitmap = null,
                        modifier = dragModifier,
                        onClick = { if (draggedItemId == null) onItemClick(item) }
                    )
                }
                is DesktopItem.Widget -> {
                    if (widgetHost != null) {
                        WidgetHostContainer(
                            appWidgetId = item.appWidgetId,
                            widgetHost = widgetHost,
                            isLocked = isLayoutLocked || item.isLocked,
                            modifier = dragModifier.size(
                                width = cellWidthDp * item.spanColumns,
                                height = cellHeightDp * item.spanRows
                            )
                        )
                    }
                }
            }
        }
    }
}
