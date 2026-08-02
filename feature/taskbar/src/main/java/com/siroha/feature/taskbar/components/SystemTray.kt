package com.siroha.feature.taskbar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun SystemTray(
    isWifiConnected: Boolean,
    isBluetoothEnabled: Boolean,
    batteryPercent: Int,
    isCharging: Boolean,
    notificationCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isWifiConnected) Icons.Filled.Wifi else Icons.Filled.WifiOff,
            contentDescription = if (isWifiConnected) "Wi-Fi connected" else "Wi-Fi disconnected",
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(16.dp)
        )

        if (isBluetoothEnabled) {
            Icon(
                imageVector = Icons.Filled.Bluetooth,
                contentDescription = "Bluetooth enabled",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(16.dp).padding(start = 6.dp)
            )
        }

        Icon(
            imageVector = if (isCharging) Icons.Filled.BatteryChargingFull else Icons.Filled.BatteryFull,
            contentDescription = "Battery $batteryPercent%",
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(16.dp).padding(start = 6.dp)
        )
        Text(
            text = "$batteryPercent%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 2.dp)
        )

        if (notificationCount > 0) {
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error)
            )
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = "$notificationCount notifications",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(16.dp).padding(start = 2.dp)
            )
        }
    }
}
