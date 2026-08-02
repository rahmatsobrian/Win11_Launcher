package com.siroha.win11launcher.di

import com.siroha.feature.search.ContactsSearchProvider
import com.siroha.feature.search.RecentQueriesProvider
import com.siroha.feature.search.SettingsSearchProvider
import com.siroha.win11launcher.core.ContactsSearchProviderImpl
import com.siroha.win11launcher.core.RecentQueriesProviderImpl
import com.siroha.win11launcher.core.SettingsSearchProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SearchProvidersModule {

    @Binds
    @Singleton
    abstract fun bindSettingsSearchProvider(impl: SettingsSearchProviderImpl): SettingsSearchProvider

    @Binds
    @Singleton
    abstract fun bindContactsSearchProvider(impl: ContactsSearchProviderImpl): ContactsSearchProvider

    @Binds
    @Singleton
    abstract fun bindRecentQueriesProvider(impl: RecentQueriesProviderImpl): RecentQueriesProvider
}
