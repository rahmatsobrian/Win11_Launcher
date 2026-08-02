package com.siroha.core.domain.repository

import com.siroha.core.domain.model.DesktopItem
import kotlinx.coroutines.flow.Flow

interface DesktopRepository {

    fun observeDesktopItems(page: Int): Flow<List<DesktopItem>>

    fun observePageCount(): Flow<Int>

    suspend fun addItem(item: DesktopItem)

    suspend fun moveItem(itemId: String, newPosition: com.siroha.core.domain.model.GridPosition)

    suspend fun removeItem(itemId: String)

    suspend fun renameItem(itemId: String, newLabel: String)

    suspend fun addPage(): Int

    suspend fun removePage(page: Int)

    suspend fun createFolder(name: String, memberComponentKeys: List<String>): DesktopItem.Folder

    suspend fun addToFolder(folderId: String, componentKey: String)

    suspend fun removeFromFolder(folderId: String, componentKey: String)
}
