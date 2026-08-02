package com.siroha.feature.widgets.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * LauncherWidgetHost is constructed via @Inject constructor + @Singleton,
 * so Hilt already knows how to provide it — no explicit @Provides needed.
 * This module exists as the attachment point for lifecycle wiring
 * documentation and for future @Binds if an interface is extracted.
 */
@Module
@InstallIn(SingletonComponent::class)
object WidgetHostModule
