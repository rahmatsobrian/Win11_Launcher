package com.siroha.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.siroha.core.database.entity.AppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    @Query("SELECT * FROM apps WHERE isHidden = 0 ORDER BY label COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps ORDER BY label COLLATE NOCASE ASC")
    fun observeAllIncludingHidden(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE isPinnedToTaskbar = 1 ORDER BY label COLLATE NOCASE ASC")
    fun observePinnedTaskbar(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE isPinnedToStart = 1 ORDER BY label COLLATE NOCASE ASC")
    fun observePinnedStart(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE isHidden = 0 ORDER BY launchCount DESC LIMIT :limit")
    fun observeMostUsed(limit: Int): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE isHidden = 0 AND lastLaunchedMillis > 0 ORDER BY lastLaunchedMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE componentKey = :componentKey LIMIT 1")
    suspend fun getByComponentKey(componentKey: String): AppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(apps: List<AppEntity>)

    @Update
    suspend fun update(app: AppEntity)

    @Query("DELETE FROM apps WHERE componentKey NOT IN (:validComponentKeys)")
    suspend fun deleteStale(validComponentKeys: List<String>)

    @Query("UPDATE apps SET isHidden = :hidden WHERE componentKey = :componentKey")
    suspend fun setHidden(componentKey: String, hidden: Boolean)

    @Query("UPDATE apps SET isPinnedToTaskbar = :pinned WHERE componentKey = :componentKey")
    suspend fun setPinnedToTaskbar(componentKey: String, pinned: Boolean)

    @Query("UPDATE apps SET isPinnedToStart = :pinned WHERE componentKey = :componentKey")
    suspend fun setPinnedToStart(componentKey: String, pinned: Boolean)

    @Query(
        """
        UPDATE apps SET
            launchCount = launchCount + 1,
            lastLaunchedMillis = :timestamp
        WHERE componentKey = :componentKey
        """
    )
    suspend fun recordLaunch(componentKey: String, timestamp: Long)
}
