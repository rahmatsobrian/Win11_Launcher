package com.siroha.win11launcher.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/**
 * Wraps Settings.System.SCREEN_BRIGHTNESS writes and the system Wi-Fi/
 * Bluetooth panel intents used by Quick Settings. Brightness writes
 * silently no-op without the WRITE_SETTINGS special permission granted via
 * Settings.ACTION_MANAGE_WRITE_SETTINGS (not a runtime dialog permission),
 * so canWriteSettings should be checked before calling setBrightnessPercent.
 */
object SystemControlHelper {

    fun canWriteSettings(context: Context): Boolean = Settings.System.canWrite(context)

    fun requestWriteSettingsPermission(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun getBrightnessPercent(context: Context): Int {
        val raw = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128)
        return (raw * 100 / 255).coerceIn(0, 100)
    }

    fun setBrightnessPercent(context: Context, percent: Int) {
        if (!canWriteSettings(context)) return
        val raw = (percent.coerceIn(0, 100) * 255 / 100)
        Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, raw)
    }

    fun openWifiPanel(context: Context) {
        val intent = Intent(Settings.Panel.ACTION_WIFI).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openBluetoothSettings(context: Context) {
        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
