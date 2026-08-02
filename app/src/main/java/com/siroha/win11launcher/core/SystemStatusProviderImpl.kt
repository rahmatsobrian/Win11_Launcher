package com.siroha.win11launcher.core

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.BatteryManager
import com.siroha.feature.taskbar.system.SystemStatus
import com.siroha.feature.taskbar.system.SystemStatusProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemStatusProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SystemStatusProvider {

    override fun observeStatus(): Flow<SystemStatus> = combine(
        observeClock(),
        observeBatteryStatus(),
        observeWifiConnectivity(),
        observeBluetoothState()
    ) { clock, battery, wifi, bluetooth ->
        SystemStatus(
            timeText = clock.first,
            dateText = clock.second,
            batteryPercent = battery.first,
            isCharging = battery.second,
            isWifiConnected = wifi,
            isBluetoothEnabled = bluetooth,
            notificationCount = 0 // wired once NotificationListenerService access is granted by the user
        )
    }

    /** Emits a fresh time/date pair once per minute; second-level precision isn't needed for a taskbar clock. */
    private fun observeClock(): Flow<Pair<String, String>> = flow {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        while (true) {
            val now = Calendar.getInstance().time
            emit(timeFormat.format(now) to dateFormat.format(now))
            val secondsUntilNextMinute = 60 - Calendar.getInstance().get(Calendar.SECOND)
            delay(secondsUntilNextMinute * 1000L)
        }
    }

    private fun observeBatteryStatus(): Flow<Pair<Int, Boolean>> = callbackFlow {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent == null) return
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val percent = if (level >= 0 && scale > 0) (level * 100 / scale) else 100
                val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
                trySend(percent to charging)
            }
        }
        context.registerReceiver(receiver, filter)
        awaitClose { context.unregisterReceiver(receiver) }
    }

    private fun observeWifiConnectivity(): Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                trySend(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
            }

            override fun onLost(network: Network) {
                trySend(false)
            }
        }

        val request = android.net.NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)
        // Seed an initial value so the UI isn't stuck showing "disconnected"
        // until the first callback fires.
        trySend(isCurrentlyWifiConnected(connectivityManager))
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }

    private fun isCurrentlyWifiConnected(connectivityManager: ConnectivityManager): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private fun observeBluetoothState(): Flow<Boolean> = callbackFlow {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            trySend(false)
            awaitClose { }
            return@callbackFlow
        }

        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                trySend(adapter.isEnabled)
            }
        }
        context.registerReceiver(receiver, filter)
        trySend(adapter.isEnabled)
        awaitClose { context.unregisterReceiver(receiver) }
    }
}
