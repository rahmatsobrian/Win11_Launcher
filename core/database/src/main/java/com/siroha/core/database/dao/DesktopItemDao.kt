package com.siroha.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.siroha.core.database.entity.DesktopItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DesktopItemDao {

    @Query("SELECT * FROM desktop_items WHERE page = :page")
    fun observeItemsForPage(page: Int): Flow<List<DesktopItemEntity>>

    @Query("SELECT COALESCE(MAX(page), 0) + 1 FROM desktop_items")
    fun observePageCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: DesktopItemEntity)

    @Update
    suspend fun update(item: DesktopItemEntity)

    @Delete
    suspend fun delete(item: DesktopItemEntity)

    @Query("DELETE FROM desktop_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM desktop_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): DesktopItemEntity?

    @Query("DELETE FROM desktop_items WHERE page = :page")
    suspend fun deletePage(page: Int)
}
