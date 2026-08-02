package com.siroha.feature.taskbar.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Windows 11-style Quick Settings flyout, anchored above the taskbar's
 * system tray. Wifi/Bluetooth toggles open the system panel rather than
 * flipping radios directly — apps below Android 10 cannot toggle Wi-Fi
 * programmatically (Settings.Panel intent is the documented replacement),
 * so this stays consistent across every supported OS version instead of
 * having a toggle that silently no-ops on newer Android.
 */
@Composable
fun QuickSettingsOverlay(
    isVisible: Boolean,
    isWifiConnected: Boolean,
    isBluetoothEnabled: Boolean,
    brightnessPercent: Int,
    onBrightnessChange: (Int) -> Unit,
    onOpenWifiPanel: () -> Unit,
    onOpenBluetoothPanel: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(150)),
        exit = fadeOut(tween(150)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.15f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomEnd
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(tween(200)) { it / 4 } + fadeIn(tween(200)),
                exit = slideOutVertically(tween(150)) { it / 4 } + fadeOut(tween(150))
            ) {
                Column(
                    modifier = Modifier
                        .padding(bottom = 64.dp, end = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable(enabled = false) {}
                        .padding(16.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuickToggle(
                            icon = Icons.Filled.Wifi,
                            label = "Wi-Fi",
                            isActive = isWifiConnected,
                            onClick = onOpenWifiPanel
                        )
                        QuickToggle(
                            icon = Icons.Filled.Bluetooth,
                            label = "Bluetooth",
                            isActive = isBluetoothEnabled,
                            onClick = onOpenBluetoothPanel
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.BrightnessMedium, contentDescription = "Brightness")
                        Slider(
                            value = brightnessPercent.toFloat(),
                            onValueChange = { onBrightnessChange(it.toInt()) },
                            valueRange = 0f..100f,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickToggle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
        Text(text = label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
    }
}
