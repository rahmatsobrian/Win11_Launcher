package com.siroha.core.domain.repository

import android.graphics.Bitmap

interface IconRepository {
    /**
     * Returns a cached icon bitmap for the given app, loading and caching it
     * on first request. Returns null only if the app can no longer be
     * resolved via PackageManager (e.g. uninstalled between index refresh
     * and this call).
     */
    suspend fun getIcon(componentKey: String, packageName: String, activityClassName: String): Bitmap?

    suspend fun invalidate(componentKey: String)

    suspend fun clearCache()
}
