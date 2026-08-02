package com.siroha.core.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.LruCache
import com.siroha.core.common.di.IoDispatcher
import com.siroha.core.domain.repository.IconRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Icon bitmaps are decoded once per app and kept in a bounded in-memory LRU
 * (sized as a fraction of available app memory) rather than a disk cache —
 * launcher icon sets are small enough (typically well under 300 apps) that
 * memory residency after first load is cheap, and it avoids the complexity
 * of disk cache invalidation on app updates/icon pack changes.
 */
@Singleton
class IconRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : IconRepository {

    private val cache: LruCache<String, Bitmap> = object : LruCache<String, Bitmap>(cacheSizeKb()) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
    }

    private fun cacheSizeKb(): Int {
        val maxMemoryKb = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        return maxMemoryKb / 8 // use up to 1/8th of available heap for icons
    }

    // Prevents two concurrent callers for the same never-cached icon from
    // both decoding it (wasted work + potential double allocation spike).
    private val loadMutex = Mutex()

    override suspend fun getIcon(
        componentKey: String,
        packageName: String,
        activityClassName: String
    ): Bitmap? = withContext(ioDispatcher) {
        cache.get(componentKey)?.let { return@withContext it }

        loadMutex.withLock {
            // Re-check after acquiring the lock in case another coroutine
            // finished loading it while this one was waiting.
            cache.get(componentKey)?.let { return@withLock it }

            val drawable = runCatching {
                context.packageManager.getActivityIcon(
                    android.content.ComponentName(packageName, activityClassName)
                )
            }.getOrNull() ?: return@withLock null

            val bitmap = drawable.toBitmap()
            cache.put(componentKey, bitmap)
            bitmap
        }
    }

    override suspend fun invalidate(componentKey: String) = withContext(ioDispatcher) {
        cache.remove(componentKey)
        Unit
    }

    override suspend fun clearCache() = withContext(ioDispatcher) {
        cache.evictAll()
    }

    private fun Drawable.toBitmap(): Bitmap {
        val width = intrinsicWidth.coerceAtLeast(1)
        val height = intrinsicHeight.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        return bitmap
    }
}
