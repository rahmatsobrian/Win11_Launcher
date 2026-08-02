package com.siroha.win11launcher.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/**
 * Triggered on device boot. Enqueues a lightweight WorkManager job that
 * revalidates the installed-app index and desktop layout cache, since
 * package UIDs and widget host state can shift across reboots.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val request = OneTimeWorkRequestBuilder<AppIndexSyncWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
