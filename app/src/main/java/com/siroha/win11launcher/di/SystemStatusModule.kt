package com.siroha.win11launcher.di

import com.siroha.feature.taskbar.notifications.NotificationCenterProvider
import com.siroha.feature.taskbar.system.RunningAppsProvider
import com.siroha.feature.taskbar.system.SystemStatusProvider
import com.siroha.win11launcher.core.NotificationCenterProviderImpl
import com.siroha.win11launcher.core.RunningAppsProviderImpl
import com.siroha.win11launcher.core.SystemStatusProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SystemStatusModule {

    @Binds
    @Singleton
    abstract fun bindSystemStatusProvider(impl: SystemStatusProviderImpl): SystemStatusProvider

    @Binds
    @Singleton
    abstract fun bindNotificationCenterProvider(impl: NotificationCenterProviderImpl): NotificationCenterProvider

    @Binds
    @Singleton
    abstract fun bindRunningAppsProvider(impl: RunningAppsProviderImpl): RunningAppsProvider
}
