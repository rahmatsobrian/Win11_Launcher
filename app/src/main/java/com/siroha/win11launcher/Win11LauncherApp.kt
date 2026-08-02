package com.siroha.win11launcher

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application entry point. Annotated with @HiltAndroidApp so Hilt generates
 * the dependency graph root that all modules (core + feature) hang off of.
 * Also implements Configuration.Provider so WorkManager can construct
 * @HiltWorker workers (AppIndexSyncWorker) with injected dependencies
 * instead of requiring a no-arg constructor.
 */
@HiltAndroidApp
class Win11LauncherApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Intentionally minimal. Heavy init (icon cache warmup, usage stats
        // indexing, etc.) is deferred to WorkManager so cold start stays fast.
    }
}
