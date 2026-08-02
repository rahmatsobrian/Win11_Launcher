package com.siroha.core.data.di

import com.siroha.core.data.repository.DesktopRepositoryImpl
import com.siroha.core.data.repository.IconRepositoryImpl
import com.siroha.core.data.repository.InstalledAppsRepositoryImpl
import com.siroha.core.data.repository.SettingsRepositoryImpl
import com.siroha.core.domain.repository.DesktopRepository
import com.siroha.core.domain.repository.IconRepository
import com.siroha.core.domain.repository.InstalledAppsRepository
import com.siroha.core.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindInstalledAppsRepository(
        impl: InstalledAppsRepositoryImpl
    ): InstalledAppsRepository

    @Binds
    @Singleton
    abstract fun bindDesktopRepository(
        impl: DesktopRepositoryImpl
    ): DesktopRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindIconRepository(
        impl: IconRepositoryImpl
    ): IconRepository
}
