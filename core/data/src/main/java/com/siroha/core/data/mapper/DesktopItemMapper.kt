package com.siroha.core.data.mapper

import com.siroha.core.database.entity.DesktopItemEntity
import com.siroha.core.domain.model.DesktopItem
import com.siroha.core.domain.model.GridPosition

private const val TYPE_SHORTCUT = "SHORTCUT"
private const val TYPE_FOLDER = "FOLDER"
private const val TYPE_WIDGET = "WIDGET"

fun DesktopItemEntity.toDomain(): DesktopItem {
    val position = GridPosition(page = page, row = row, column = column)
    return when (type) {
        TYPE_SHORTCUT -> DesktopItem.AppShortcut(
            id = id,
            position = position,
            appComponentKey = appComponentKey.orEmpty(),
            customLabel = customLabel
        )
        TYPE_FOLDER -> DesktopItem.Folder(
            id = id,
            position = position,
            name = folderName.orEmpty(),
            itemComponentKeys = folderMemberKeysCsv
                ?.split(",")
                ?.filter { it.isNotBlank() }
                .orEmpty()
        )
        TYPE_WIDGET -> DesktopItem.Widget(
            id = id,
            position = position,
            appWidgetId = appWidgetId ?: -1,
            spanColumns = spanColumns ?: 1,
            spanRows = spanRows ?: 1,
            isLocked = isLocked
        )
        else -> error("Unknown desktop item type: $type")
    }
}

fun DesktopItem.toEntity(): DesktopItemEntity = when (this) {
    is DesktopItem.AppShortcut -> DesktopItemEntity(
        id = id,
        type = TYPE_SHORTCUT,
        page = position.page,
        row = position.row,
        column = position.column,
        appComponentKey = appComponentKey,
        customLabel = customLabel
    )
    is DesktopItem.Folder -> DesktopItemEntity(
        id = id,
        type = TYPE_FOLDER,
        page = position.page,
        row = position.row,
        column = position.column,
        folderName = name,
        folderMemberKeysCsv = itemComponentKeys.joinToString(",")
    )
    is DesktopItem.Widget -> DesktopItemEntity(
        id = id,
        type = TYPE_WIDGET,
        page = position.page,
        row = position.row,
        column = position.column,
        appWidgetId = appWidgetId,
        spanColumns = spanColumns,
        spanRows = spanRows,
        isLocked = isLocked
    )
}
