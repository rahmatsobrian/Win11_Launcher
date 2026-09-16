package com.siroha.win11launcher.core

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Process
import android.util.Log
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.siroha.core.domain.model.AppInfo
import com.siroha.core.domain.repository.InstalledAppsRepository
import com.siroha.core.domain.repository.SettingsRepository
import com.siroha.core.domain.usecase.LaunchAppUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AppLauncher"

@Singleton
class AppLauncher @Inject constructor(
    private val installedAppsRepository: InstalledAppsRepository,
    private val launchAppUseCase: LaunchAppUseCase,
    private val settingsRepository: SettingsRepository
) {

    fun launch(context: Context, componentKey: String, scope: CoroutineScope) {
        scope.launch {
            val settings = settingsRepository.observeSettings().first()
            val app = installedAppsRepository.getApp(componentKey)
            if (app == null) {
                Log.w(TAG, "No app found for componentKey=$componentKey")
                Toast.makeText(context, "App not found", Toast.LENGTH_SHORT).show()
                return@launch
            }

            if (settings.appLockEnabled && componentKey in settings.lockedApps) {
                val activity = context as? FragmentActivity
                if (activity != null) {
                    showBiometricPrompt(activity) {
                        scope.launch {
                            launchAppUseCase(componentKey)
                        }
                        launchIntent(context, app)
                    }
                } else {
                    Toast.makeText(context, "Authentication required", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            launchAppUseCase(componentKey)
            launchIntent(context, app)
        }
    }

    private fun showBiometricPrompt(activity: FragmentActivity, onSuccess: () -> Unit) {
        val executor = ContextCompat.getMainExecutor(activity)

        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(activity, "Authentication cancelled", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(activity, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("App Lock")
            .setSubtitle("Authenticate to open this app")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
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
