package com.siroha.core.data.di

import com.siroha.core.common.di.DefaultDispatcher
import com.siroha.core.common.di.IoDispatcher
import com.siroha.core.common.di.MainDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @Provides
    @IoDispatcher
    fun provideIoDispatcher() = Dispatchers.IO

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher() = Dispatchers.Default

    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): MainCoroutineDispatcher = Dispatchers.Main
}
