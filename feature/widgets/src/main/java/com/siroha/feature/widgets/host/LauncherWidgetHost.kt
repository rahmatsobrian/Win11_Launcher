package com.siroha.feature.widgets.host

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val LAUNCHER_WIDGET_HOST_ID = 0x57494e31 // "WIN1" as hex, arbitrary but stable

/**
 * Thin wrapper around AppWidgetHost. AppWidgetHost.startListening()/
 * stopListening() must be paired with the launcher Activity's lifecycle —
 * leaving it listening while backgrounded wastes battery on widget
 * providers that push frequent RemoteViews updates (clocks, weather).
 */
@Singleton
class LauncherWidgetHost @Inject constructor(
    @ApplicationContext context: Context
) : DefaultLifecycleObserver {

    val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)

    private val appWidgetHost = AppWidgetHost(context, LAUNCHER_WIDGET_HOST_ID)

    fun allocateAppWidgetId(): Int = appWidgetHost.allocateAppWidgetId()

    fun deleteAppWidgetId(appWidgetId: Int) = appWidgetHost.deleteAppWidgetId(appWidgetId)

    fun createView(context: Context, appWidgetId: Int, providerInfo: AppWidgetProviderInfo): AppWidgetHostView {
        return appWidgetHost.createView(context, appWidgetId, providerInfo)
    }

    fun getProviderInfo(appWidgetId: Int): AppWidgetProviderInfo? =
        appWidgetManager.getAppWidgetInfo(appWidgetId)

    fun installedProviders(): List<AppWidgetProviderInfo> =
        appWidgetManager.installedProviders

    override fun onStart(owner: LifecycleOwner) {
        appWidgetHost.startListening()
    }

    override fun onStop(owner: LifecycleOwner) {
        appWidgetHost.stopListening()
    }
}
