package com.siroha.win11launcher.di

import com.siroha.feature.filemanager.FileManagerRepository
import com.siroha.win11launcher.core.FileManagerRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FileManagerModule {

    @Binds
    @Singleton
    abstract fun bindFileManagerRepository(impl: FileManagerRepositoryImpl): FileManagerRepository
}
