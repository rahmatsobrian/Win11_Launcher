package com.siroha.core.domain.repository

import com.siroha.core.domain.model.AppInfo
import kotlinx.coroutines.flow.Flow

interface InstalledAppsRepository {

    /** Reactive stream of all launchable apps, kept in sync with PackageManager. */
    fun observeInstalledApps(): Flow<List<AppInfo>>

    fun observePinnedTaskbarApps(): Flow<List<AppInfo>>

    fun observePinnedStartApps(): Flow<List<AppInfo>>

    suspend fun getApp(componentKey: String): AppInfo?

    /** Forces a rebuild of the index from PackageManager (used after boot/package changes). */
    suspend fun refreshInstalledApps()

    suspend fun setHidden(componentKey: String, hidden: Boolean)

    suspend fun setPinnedToTaskbar(componentKey: String, pinned: Boolean)

    suspend fun setPinnedToStart(componentKey: String, pinned: Boolean)

    suspend fun recordAppLaunch(componentKey: String)

    fun observeMostUsedApps(limit: Int): Flow<List<AppInfo>>

    fun observeRecentApps(limit: Int): Flow<List<AppInfo>>
}
