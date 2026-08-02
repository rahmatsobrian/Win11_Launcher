package com.siroha.win11launcher.core

import android.content.Context
import android.content.pm.PackageManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.siroha.core.domain.repository.InstalledAppsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Rebuilds the installed-app index in Room by diffing against
 * PackageManager's current launcher-intent query results.
 */
@HiltWorker
class AppIndexSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val installedAppsRepository: InstalledAppsRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            installedAppsRepository.refreshInstalledApps()
            Result.success()
        } catch (e: PackageManager.NameNotFoundException) {
            Result.retry()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
