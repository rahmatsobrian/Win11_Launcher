package com.siroha.core.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Process
import com.siroha.core.common.di.IoDispatcher
import com.siroha.core.data.mapper.toDomain
import com.siroha.core.data.mapper.toEntity
import com.siroha.core.database.dao.AppDao
import com.siroha.core.database.entity.AppEntity
import com.siroha.core.domain.model.AppInfo
import com.siroha.core.domain.repository.InstalledAppsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstalledAppsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDao: AppDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : InstalledAppsRepository {

    override fun observeInstalledApps(): Flow<List<AppInfo>> =
        appDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observePinnedTaskbarApps(): Flow<List<AppInfo>> =
        appDao.observePinnedTaskbar().map { entities -> entities.map { it.toDomain() } }

    override fun observePinnedStartApps(): Flow<List<AppInfo>> =
        appDao.observePinnedStart().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getApp(componentKey: String): AppInfo? = withContext(ioDispatcher) {
        appDao.getByComponentKey(componentKey)?.toDomain()
    }

    override suspend fun refreshInstalledApps() = withContext(ioDispatcher) {
        val packageManager = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolvedActivities = packageManager.queryIntentActivities(
            launcherIntent,
            PackageManager.MATCH_ALL
        )

        val currentUserHandleId = Process.myUserHandle().hashCode()

        val freshEntities = resolvedActivities.mapNotNull { resolveInfo ->
            val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
            val label = runCatching { resolveInfo.loadLabel(packageManager).toString() }
                .getOrDefault(activityInfo.packageName)

            val isSystemApp = (activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

            val installTime = runCatching {
                packageManager.getPackageInfo(activityInfo.packageName, 0).firstInstallTime
            }.getOrDefault(System.currentTimeMillis())

            AppInfo(
                packageName = activityInfo.packageName,
                activityClassName = activityInfo.name,
                label = label,
                userHandleId = currentUserHandleId,
                isSystemApp = isSystemApp,
                installTimeMillis = installTime
            )
        }

        // Preserve user-set flags (hidden/pinned/launch stats) for apps that
        // already exist in the DB by merging rather than blind-overwriting.
        val existingByKey = freshEntities.associateBy { it.componentKey }.keys
            .mapNotNull { key -> appDao.getByComponentKey(key) }
            .associateBy { it.componentKey }

        val mergedEntities: List<AppEntity> = freshEntities.map { fresh ->
            val existing = existingByKey[fresh.componentKey]
            if (existing != null) {
                existing.copy(
                    label = fresh.label,
                    isSystemApp = fresh.isSystemApp,
                    installTimeMillis = fresh.installTimeMillis
                )
            } else {
                fresh.toEntity()
            }
        }

        appDao.upsertAll(mergedEntities)
        appDao.deleteStale(mergedEntities.map { it.componentKey })
    }

    override suspend fun setHidden(componentKey: String, hidden: Boolean) = withContext(ioDispatcher) {
        appDao.setHidden(componentKey, hidden)
    }

    override suspend fun setPinnedToTaskbar(componentKey: String, pinned: Boolean) = withContext(ioDispatcher) {
        appDao.setPinnedToTaskbar(componentKey, pinned)
    }

    override suspend fun setPinnedToStart(componentKey: String, pinned: Boolean) = withContext(ioDispatcher) {
        appDao.setPinnedToStart(componentKey, pinned)
    }

    override suspend fun recordAppLaunch(componentKey: String) = withContext(ioDispatcher) {
        appDao.recordLaunch(componentKey, System.currentTimeMillis())
    }

    override fun observeMostUsedApps(limit: Int): Flow<List<AppInfo>> =
        appDao.observeMostUsed(limit).map { entities -> entities.map { it.toDomain() } }

    override fun observeRecentApps(limit: Int): Flow<List<AppInfo>> =
        appDao.observeRecent(limit).map { entities -> entities.map { it.toDomain() } }

    companion object {
        val STALE_LAUNCH_WINDOW_MS = TimeUnit.DAYS.toMillis(30)
    }
}
