package com.siroha.feature.taskbar.components

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.AirplanemodeInactive
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.VideoCameraFront
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

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
    val context = LocalContext.current
    var isFlashlightOn by remember { mutableStateOf(false) }
    var isAirplaneMode by remember { mutableStateOf(false) }
    var isAutoRotate by remember { mutableStateOf(false) }
    var isDnd by remember { mutableStateOf(false) }
    var isNightLight by remember { mutableStateOf(false) }
    var isLocationEnabled by remember { mutableStateOf(false) }
    var isBatterySaver by remember { mutableStateOf(false) }

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
                        .width(360.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable(enabled = false) {}
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Quick Settings",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickToggle(
                            icon = if (isWifiConnected) Icons.Filled.Wifi else Icons.Filled.WifiOff,
                            label = "Wi-Fi",
                            isActive = isWifiConnected,
                            onClick = onOpenWifiPanel,
                            modifier = Modifier.weight(1f)
                        )
                        QuickToggle(
                            icon = Icons.Filled.Bluetooth,
                            label = "Bluetooth",
                            isActive = isBluetoothEnabled,
                            onClick = onOpenBluetoothPanel,
                            modifier = Modifier.weight(1f)
                        )
                        QuickToggle(
                            icon = Icons.Filled.AirplanemodeActive,
                            label = "Airplane",
                            isActive = isAirplaneMode,
                            onClick = {
                                isAirplaneMode = !isAirplaneMode
                                toggleAirplaneMode(context, isAirplaneMode)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickToggle(
                            icon = if (isFlashlightOn) Icons.Filled.FlashlightOn else Icons.Filled.FlashlightOff,
                            label = "Flashlight",
                            isActive = isFlashlightOn,
                            onClick = {
                                isFlashlightOn = !isFlashlightOn
                                toggleFlashlight(context, isFlashlightOn)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        QuickToggle(
                            icon = Icons.Filled.ScreenRotation,
                            label = "Rotate",
                            isActive = isAutoRotate,
                            onClick = {
                                isAutoRotate = !isAutoRotate
                                toggleAutoRotate(context, isAutoRotate)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        QuickToggle(
                            icon = Icons.Filled.DoNotDisturb,
                            label = "DND",
                            isActive = isDnd,
                            onClick = {
                                isDnd = !isDnd
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickToggle(
                            icon = Icons.Filled.ModeNight,
                            label = "Night light",
                            isActive = isNightLight,
                            onClick = {
                                isNightLight = !isNightLight
                            },
                            modifier = Modifier.weight(1f)
                        )
                        QuickToggle(
                            icon = if (isLocationEnabled) Icons.Filled.LocationOn else Icons.Filled.LocationOff,
                            label = "Location",
                            isActive = isLocationEnabled,
                            onClick = {
                                isLocationEnabled = !isLocationEnabled
                            },
                            modifier = Modifier.weight(1f)
                        )
                        QuickToggle(
                            icon = Icons.Filled.PhoneAndroid,
                            label = "Battery",
                            isActive = isBatterySaver,
                            onClick = {
                                isBatterySaver = !isBatterySaver
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Brightness",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.BrightnessMedium,
                            contentDescription = "Brightness",
                            modifier = Modifier.size(20.dp)
                        )
                        Slider(
                            value = brightnessPercent.toFloat(),
                            onValueChange = { onBrightnessChange(it.toInt()) },
                            valueRange = 0f..100f,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = "$brightnessPercent%",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.width(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Shortcuts",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickShortcut(
                            icon = Icons.Filled.Alarm,
                            label = "Alarms",
                            onClick = { openAlarms(context) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickShortcut(
                            icon = Icons.Filled.CameraAlt,
                            label = "Camera",
                            onClick = { openCamera(context) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickShortcut(
                            icon = Icons.Filled.SettingsBrightness,
                            label = "Settings",
                            onClick = { openSettings(context) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickToggle(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp),
            maxLines = 1
        )
    }
}

@Composable
private fun QuickShortcut(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp),
            maxLines = 1
        )
    }
}

private fun toggleFlashlight(context: Context, enable: Boolean) {
    try {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        val cameraId = cameraManager?.cameraIdList?.firstOrNull() ?: return
        cameraManager.setTorchMode(cameraId, enable)
    } catch (_: Exception) { }
}

private fun toggleAirplaneMode(context: Context, enable: Boolean) {
    try {
        Settings.Global.putInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, if (enable) 1 else 0)
        val intent = Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED).apply {
            putExtra("state", enable)
        }
        context.sendBroadcast(intent)
    } catch (_: Exception) { }
}

private fun toggleAutoRotate(context: Context, enable: Boolean) {
    try {
        Settings.System.putInt(
            context.contentResolver,
            Settings.System.ACCELEROMETER_ROTATION,
            if (enable) 1 else 0
        )
    } catch (_: Exception) { }
}

private fun openAlarms(context: Context) {
    try {
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: Exception) { }
}

private fun openCamera(context: Context) {
    try {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: Exception) { }
}

private fun openSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: Exception) { }
}
