package com.siroha.win11launcher.core

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Process
import android.util.Log
import android.widget.Toast
import com.siroha.core.domain.model.AppInfo
import com.siroha.core.domain.repository.InstalledAppsRepository
import com.siroha.core.domain.usecase.LaunchAppUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AppLauncher"

/**
 * Resolves a componentKey (packageName/activityClassName/userHandleId) back
 * into a launch Intent and fires it. Kept separate from the ViewModels in
 * feature modules because starting an Activity needs a Context that is
 * either an Activity or carries FLAG_ACTIVITY_NEW_TASK — a concern that
 * belongs at the app-module boundary, not inside feature/domain layers.
 */
@Singleton
class AppLauncher @Inject constructor(
    private val installedAppsRepository: InstalledAppsRepository,
    private val launchAppUseCase: LaunchAppUseCase
) {

    fun launch(context: Context, componentKey: String, scope: CoroutineScope) {
        scope.launch {
            val app = installedAppsRepository.getApp(componentKey)
            if (app == null) {
                Log.w(TAG, "No app found for componentKey=$componentKey")
                Toast.makeText(context, "App not found", Toast.LENGTH_SHORT).show()
                return@launch
            }
            launchAppUseCase(componentKey)
            launchIntent(context, app)
        }
    }

    private fun launchIntent(context: Context, app: AppInfo) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = android.content.ComponentName(app.packageName, app.activityClassName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            if (app.userHandleId == Process.myUserHandle().hashCode()) {
                context.startActivity(intent)
            } else {
                // Cross-profile (work-profile) launch requires LauncherApps,
                // which needs the actual UserHandle object rather than our
                // hashed componentKey representation — resolved by re-querying
                // LauncherApps.getActivityList for a matching UserHandle at
                // launch time rather than persisting the UserHandle itself
                // (UserHandle is not stable across reboots to serialize).
                launchViaLauncherApps(context, app)
            }
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "Activity not found for ${app.componentKey}", e)
            Toast.makeText(context, "Couldn't open ${app.label}", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception launching ${app.componentKey}", e)
            Toast.makeText(context, "Permission denied opening ${app.label}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchViaLauncherApps(context: Context, app: AppInfo) {
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? android.content.pm.LauncherApps
            ?: return

        val userManager = context.getSystemService(Context.USER_SERVICE) as? android.os.UserManager ?: return
        val targetUser = userManager.userProfiles.firstOrNull { it.hashCode() == app.userHandleId }

        if (targetUser != null) {
            launcherApps.startMainActivity(
                android.content.ComponentName(app.packageName, app.activityClassName),
                targetUser,
                null,
                null
            )
        } else {
            Log.w(TAG, "Could not resolve UserHandle for ${app.componentKey}")
        }
    }
}
