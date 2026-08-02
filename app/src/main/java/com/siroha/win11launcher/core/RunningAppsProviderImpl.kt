package com.siroha.win11launcher.core

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import com.siroha.feature.taskbar.system.RunningAppsProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

private const val POLL_INTERVAL_MS = 3_000L

@Singleton
class RunningAppsProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : RunningAppsProvider {

    override val hasUsageAccess: Boolean
        get() {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
            return mode == AppOpsManager.MODE_ALLOWED
        }

    /**
     * Polls rather than subscribing to a push API because UsageStatsManager
     * has no listener/callback surface — only pull-based queryEvents. A
     * 3-second interval balances taskbar freshness against battery/CPU
     * cost of repeatedly querying the events database.
     */
    override fun observeRecentForegroundApps(withinMinutes: Int): Flow<List<String>> = flow {
        while (true) {
            if (!hasUsageAccess) {
                emit(emptyList())
            } else {
                emit(queryRecentForegroundPackages(withinMinutes))
            }
            delay(POLL_INTERVAL_MS)
        }
    }

    private fun queryRecentForegroundPackages(withinMinutes: Int): List<String> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (withinMinutes * 60_000L)

        val events = usageStatsManager.queryEvents(startTime, endTime)
        // Tracks each package's last-seen lifecycle event and the order it
        // was first seen, so the result reflects "currently foregrounded or
        // recently backgrounded" apps in most-recent-first order.
        val lastEventByPackage = LinkedHashMap<String, Int>()
        val event = UsageEvents.Event()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND,
                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    lastEventByPackage[event.packageName] = event.eventType
                }
            }
        }

        return lastEventByPackage.entries
            .filter { it.value == UsageEvents.Event.MOVE_TO_FOREGROUND }
            .map { it.key }
    }

    override fun openUsageAccessSettings() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
