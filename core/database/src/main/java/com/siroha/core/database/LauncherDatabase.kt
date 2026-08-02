package com.siroha.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.siroha.core.database.dao.AppDao
import com.siroha.core.database.dao.DesktopItemDao
import com.siroha.core.database.entity.AppEntity
import com.siroha.core.database.entity.DesktopItemEntity

@Database(
    entities = [AppEntity::class, DesktopItemEntity::class],
    version = 1,
    exportSchema = true
)
abstract class LauncherDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    abstract fun desktopItemDao(): DesktopItemDao

    companion object {
        const val DATABASE_NAME = "win11_launcher.db"
    }
}
