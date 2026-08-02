package com.siroha.core.domain.model

/** Position on a desktop page's grid. Page 0 is the leftmost/primary page. */
data class GridPosition(
    val page: Int,
    val row: Int,
    val column: Int
)

sealed class DesktopItem {
    abstract val id: String
    abstract val position: GridPosition

    data class AppShortcut(
        override val id: String,
        override val position: GridPosition,
        val appComponentKey: String,
        val customLabel: String? = null
    ) : DesktopItem()

    data class Folder(
        override val id: String,
        override val position: GridPosition,
        val name: String,
        val itemComponentKeys: List<String>
    ) : DesktopItem()

    data class Widget(
        override val id: String,
        override val position: GridPosition,
        val appWidgetId: Int,
        val spanColumns: Int,
        val spanRows: Int,
        val isLocked: Boolean = false
    ) : DesktopItem()
}
