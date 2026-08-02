package com.siroha.core.data.repository

import com.siroha.core.common.di.IoDispatcher
import com.siroha.core.data.mapper.toDomain
import com.siroha.core.data.mapper.toEntity
import com.siroha.core.database.dao.DesktopItemDao
import com.siroha.core.domain.model.DesktopItem
import com.siroha.core.domain.model.GridPosition
import com.siroha.core.domain.repository.DesktopRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DesktopRepositoryImpl @Inject constructor(
    private val desktopItemDao: DesktopItemDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : DesktopRepository {

    override fun observeDesktopItems(page: Int): Flow<List<DesktopItem>> =
        desktopItemDao.observeItemsForPage(page).map { entities -> entities.map { it.toDomain() } }

    override fun observePageCount(): Flow<Int> = desktopItemDao.observePageCount()

    override suspend fun addItem(item: DesktopItem) = withContext(ioDispatcher) {
        desktopItemDao.upsert(item.toEntity())
    }

    override suspend fun moveItem(itemId: String, newPosition: GridPosition) = withContext(ioDispatcher) {
        val existing = desktopItemDao.getById(itemId) ?: return@withContext
        desktopItemDao.update(
            existing.copy(page = newPosition.page, row = newPosition.row, column = newPosition.column)
        )
    }

    override suspend fun removeItem(itemId: String) = withContext(ioDispatcher) {
        desktopItemDao.deleteById(itemId)
    }

    override suspend fun renameItem(itemId: String, newLabel: String) = withContext(ioDispatcher) {
        val existing = desktopItemDao.getById(itemId) ?: return@withContext
        val updated = when (existing.type) {
            "FOLDER" -> existing.copy(folderName = newLabel)
            else -> existing.copy(customLabel = newLabel)
        }
        desktopItemDao.update(updated)
    }

    override suspend fun addPage(): Int = withContext(ioDispatcher) {
        // Page count is derived (MAX(page) + 1) rather than stored, so
        // "adding" a page is a logical no-op here — the ViewModel tracks the
        // new empty page index locally and it persists automatically the
        // moment the user drops the first item onto it.
        0
    }

    override suspend fun removePage(page: Int) = withContext(ioDispatcher) {
        desktopItemDao.deletePage(page)
    }

    override suspend fun createFolder(
        name: String,
        memberComponentKeys: List<String>
    ): DesktopItem.Folder = withContext(ioDispatcher) {
        val folder = DesktopItem.Folder(
            id = UUID.randomUUID().toString(),
            position = GridPosition(page = 0, row = 0, column = 0),
            name = name,
            itemComponentKeys = memberComponentKeys
        )
        desktopItemDao.upsert(folder.toEntity())
        folder
    }

    override suspend fun addToFolder(folderId: String, componentKey: String) = withContext(ioDispatcher) {
        val entity = desktopItemDao.getById(folderId) ?: return@withContext
        val currentKeys = entity.folderMemberKeysCsv?.split(",")?.filter { it.isNotBlank() }.orEmpty()
        if (componentKey in currentKeys) return@withContext
        desktopItemDao.update(
            entity.copy(folderMemberKeysCsv = (currentKeys + componentKey).joinToString(","))
        )
    }

    override suspend fun removeFromFolder(folderId: String, componentKey: String) = withContext(ioDispatcher) {
        val entity = desktopItemDao.getById(folderId) ?: return@withContext
        val currentKeys = entity.folderMemberKeysCsv?.split(",")?.filter { it.isNotBlank() }.orEmpty()
        desktopItemDao.update(
            entity.copy(folderMemberKeysCsv = (currentKeys - componentKey).joinToString(","))
        )
    }
}
