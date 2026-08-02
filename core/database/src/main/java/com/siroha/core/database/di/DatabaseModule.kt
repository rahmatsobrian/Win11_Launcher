package com.siroha.core.database.di

import android.content.Context
import androidx.room.Room
import com.siroha.core.database.LauncherDatabase
import com.siroha.core.database.dao.AppDao
import com.siroha.core.database.dao.DesktopItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideLauncherDatabase(@ApplicationContext context: Context): LauncherDatabase {
        return Room.databaseBuilder(
            context,
            LauncherDatabase::class.java,
            LauncherDatabase.DATABASE_NAME
        )
            // Desktop layout/app index are derived/cacheable state, not
            // irreplaceable user data, so a destructive fallback keeps the
            // launcher bootable across schema changes instead of crash-looping.
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideAppDao(database: LauncherDatabase): AppDao = database.appDao()

    @Provides
    fun provideDesktopItemDao(database: LauncherDatabase): DesktopItemDao = database.desktopItemDao()
}
