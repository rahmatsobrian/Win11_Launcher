package com.siroha.feature.settings

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.siroha.core.domain.model.ThemeMode
import com.siroha.core.domain.model.WallpaperMode
import com.siroha.feature.settings.components.SettingsNavigationRow
import com.siroha.feature.settings.components.SettingsSliderRow
import com.siroha.feature.settings.components.SettingsSwitchRow

@Composable
fun SettingsScreen(
    onNavigateToAbout: () -> Unit,
    onNavigateToDeveloperOptions: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    var showThemeDialog by remember { mutableStateOf(false) }
    var showWallpaperDialog by remember { mutableStateOf(false) }
    var showFontPreviewDialog by remember { mutableStateOf(false) }
    var showAccentColorDialog by remember { mutableStateOf(false) }

    val wallpaperLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.setWallpaper(it.toString())
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { SectionLabel("Theme & Personalization") }
        item {
            SettingsNavigationRow(
                title = "Theme mode",
                subtitle = when (settings.themeMode) {
                    ThemeMode.LIGHT -> "Light"
                    ThemeMode.DARK -> "Dark"
                    ThemeMode.AUTO -> "Follow system"
                },
                onClick = { showThemeDialog = true }
            )
        }
        item {
            SettingsNavigationRow(
                title = "Accent color",
                subtitle = if (settings.dynamicColorEnabled) "Dynamic (from wallpaper)" else "Custom blue",
                onClick = { showAccentColorDialog = true }
            )
        }
        item {
            SettingsSwitchRow(
                title = "Dynamic color",
                subtitle = "Use wallpaper-based accent color (Android 12+)",
                checked = settings.dynamicColorEnabled,
                onCheckedChange = viewModel::setDynamicColorEnabled
            )
        }
        item {
            SettingsNavigationRow(
                title = "Wallpaper",
                subtitle = when (settings.wallpaperMode) {
                    WallpaperMode.DEFAULT -> "Default Windows 11 wallpaper"
                    WallpaperMode.SOLID_COLOR -> "Solid color"
                    WallpaperMode.GALLERY -> "Pick from gallery"
                },
                onClick = { showWallpaperDialog = true }
            )
        }
        item { HorizontalDivider() }

        item { SectionLabel("Display") }
        item {
            SettingsSliderRow(
                title = "Font size",
                value = settings.fontSize,
                valueRange = 10..24,
                onValueChange = { viewModel.setFontSize(it) },
                valueLabel = { "${it}sp" }
            )
        }
        item {
            SettingsSliderRow(
                title = "Blur intensity",
                value = settings.blurIntensityPercent,
                valueRange = 0..100,
                onValueChange = viewModel::setBlurIntensity,
                valueLabel = { "$it%" }
            )
        }
        item {
            SettingsSliderRow(
                title = "Corner radius",
                value = settings.cornerRadiusDp,
                valueRange = 0..28,
                onValueChange = viewModel::setCornerRadius,
                valueLabel = { "${it}dp" }
            )
        }
        item {
            SettingsSwitchRow(
                title = "Animations",
                checked = settings.animationsEnabled,
                onCheckedChange = viewModel::setAnimationsEnabled
            )
        }
        item { HorizontalDivider() }

        item { SectionLabel("Taskbar") }
        item {
            SettingsSwitchRow(
                title = "Center-align icons",
                checked = settings.taskbar.alignment == com.siroha.core.domain.model.TaskbarAlignment.CENTER,
                onCheckedChange = { centered ->
                    viewModel.updateTaskbar {
                        it.copy(alignment = if (centered) com.siroha.core.domain.model.TaskbarAlignment.CENTER else com.siroha.core.domain.model.TaskbarAlignment.LEFT)
                    }
                }
            )
        }
        item {
            SettingsSwitchRow(
                title = "Auto-hide taskbar",
                checked = settings.taskbar.autoHide,
                onCheckedChange = { auto -> viewModel.updateTaskbar { it.copy(autoHide = auto) } }
            )
        }
        item {
            SettingsSwitchRow(
                title = "Lock taskbar",
                checked = settings.taskbar.isLocked,
                onCheckedChange = { locked -> viewModel.updateTaskbar { it.copy(isLocked = locked) } }
            )
        }
        item {
            SettingsSliderRow(
                title = "Taskbar transparency",
                value = settings.taskbar.transparencyPercent,
                valueRange = 0..100,
                onValueChange = { v -> viewModel.updateTaskbar { it.copy(transparencyPercent = v) } },
                valueLabel = { "$it%" }
            )
        }
        item {
            SettingsSliderRow(
                title = "Taskbar height",
                value = settings.taskbar.heightDp,
                valueRange = 36..64,
                onValueChange = { h -> viewModel.updateTaskbar { it.copy(heightDp = h) } },
                valueLabel = { "${it}dp" }
            )
        }
        item { HorizontalDivider() }

        item { SectionLabel("Start Menu") }
        item {
            SettingsSwitchRow(
                title = "Show recommended apps",
                checked = settings.startMenu.showRecommended,
                onCheckedChange = { show -> viewModel.updateStartMenu { it.copy(showRecommended = show) } }
            )
        }
        item {
            SettingsSliderRow(
                title = "Pinned rows",
                value = settings.startMenu.pinnedRowCount,
                valueRange = 2..6,
                onValueChange = { rows -> viewModel.updateStartMenu { it.copy(pinnedRowCount = rows) } }
            )
        }
        item {
            SettingsSliderRow(
                title = "Start Menu width",
                value = settings.startMenu.widthDp,
                valueRange = 400..800,
                onValueChange = { w -> viewModel.updateStartMenu { it.copy(widthDp = w) } },
                valueLabel = { "${it}dp" }
            )
        }
        item {
            SettingsSliderRow(
                title = "Start Menu height",
                value = settings.startMenu.heightDp,
                valueRange = 400..800,
                onValueChange = { h -> viewModel.updateStartMenu { it.copy(heightDp = h) } },
                valueLabel = { "${it}dp" }
            )
        }
        item { HorizontalDivider() }

        item { SectionLabel("Desktop") }
        item {
            SettingsSliderRow(
                title = "Grid columns",
                value = settings.desktop.gridColumns,
                valueRange = 3..8,
                onValueChange = { cols -> viewModel.updateDesktop { it.copy(gridColumns = cols) } }
            )
        }
        item {
            SettingsSliderRow(
                title = "Grid rows",
                value = settings.desktop.gridRows,
                valueRange = 3..10,
                onValueChange = { rows -> viewModel.updateDesktop { it.copy(gridRows = rows) } }
            )
        }
        item {
            SettingsSwitchRow(
                title = "Show icon labels",
                checked = settings.desktop.showLabels,
                onCheckedChange = { show -> viewModel.updateDesktop { it.copy(showLabels = show) } }
            )
        }
        item {
            SettingsSwitchRow(
                title = "Lock desktop layout",
                checked = settings.desktop.isLayoutLocked,
                onCheckedChange = { locked -> viewModel.updateDesktop { it.copy(isLayoutLocked = locked) } }
            )
        }
        item { HorizontalDivider() }

        item { SectionLabel("Backup & Restore") }
        item {
            SettingsNavigationRow(
                title = "Export backup",
                subtitle = "Copy launcher settings and layout to clipboard",
                onClick = {
                    viewModel.exportBackup { json ->
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("launcher_backup", json)
                        clipboard.setPrimaryClip(clip)
                        android.widget.Toast.makeText(context, "Backup copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
        item {
            SettingsNavigationRow(
                title = "Import backup",
                subtitle = "Restore settings and layout from clipboard",
                onClick = {
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = clipboard.primaryClip
                    val json = clip?.getItemAt(0)?.text?.toString()
                    if (json.isNullOrBlank()) {
                        android.widget.Toast.makeText(context, "No backup data in clipboard", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.importBackup(json) { success, message ->
                            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
        item { HorizontalDivider() }

        item { SectionLabel("Advanced") }
        item {
            SettingsNavigationRow(
                title = "Developer options",
                subtitle = "FPS counter, recomposition counter, layout boundaries",
                onClick = onNavigateToDeveloperOptions
            )
        }
        item {
            SettingsNavigationRow(
                title = "About",
                subtitle = "Version, licenses, feedback",
                onClick = onNavigateToAbout
            )
        }
    }

    if (showThemeDialog) {
        ThemeModeDialog(
            currentMode = settings.themeMode,
            onSelect = { mode ->
                viewModel.setThemeMode(mode)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showWallpaperDialog) {
        WallpaperDialog(
            currentMode = settings.wallpaperMode,
            onSelect = { mode ->
                when (mode) {
                    WallpaperMode.GALLERY -> {
                        wallpaperLauncher.launch("image/*")
                    }
                    else -> viewModel.setWallpaperMode(mode)
                }
                showWallpaperDialog = false
            },
            onDismiss = { showWallpaperDialog = false }
        )
    }

    if (showAccentColorDialog) {
        AccentColorDialog(
            onDismiss = { showAccentColorDialog = false }
        )
    }
}

@Composable
private fun ThemeModeDialog(
    currentMode: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        ThemeMode.LIGHT to "Light",
        ThemeMode.DARK to "Dark",
        ThemeMode.AUTO to "Follow system"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Theme mode") },
        text = {
            Column {
                options.forEach { (mode, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(mode) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    color = if (currentMode == mode) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                        ) {
                            if (currentMode == mode) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                        .align(Alignment.Center)
                                )
                            }
                        }
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun WallpaperDialog(
    currentMode: WallpaperMode,
    onSelect: (WallpaperMode) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        WallpaperMode.DEFAULT to "Default wallpaper",
        WallpaperMode.SOLID_COLOR to "Solid color",
        WallpaperMode.GALLERY to "Choose from gallery"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wallpaper") },
        text = {
            Column {
                options.forEach { (mode, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(mode) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (mode) {
                                WallpaperMode.DEFAULT -> Icons.Filled.Wallpaper
                                WallpaperMode.SOLID_COLOR -> Icons.Filled.ColorLens
                                WallpaperMode.GALLERY -> Icons.Filled.PhoneAndroid
                            },
                            contentDescription = null,
                            tint = if (currentMode == mode) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AccentColorDialog(onDismiss: () -> Unit) {
    val colors = listOf(
        Color(0xFF0078D4) to "Blue",
        Color(0xFF0099BC) to "Teal",
        Color(0xFF7A7574) to "Gray",
        Color(0xFF767676) to "Dark Gray",
        Color(0xFFE81123) to "Red",
        Color(0xFF0063B1) to "Indigo",
        Color(0xFF8764B8) to "Purple",
        Color(0xFF00B294) to "Green",
        Color(0xFFFF8C00) to "Orange",
        Color(0xFFC239B3) to "Pink"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Accent Color") },
        text = {
            Column {
                Text(
                    text = "Choose a color accent",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.take(5).forEach { (color, _) ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { onDismiss() }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.drop(5).forEach { (color, _) ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { onDismiss() }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 4.dp)
    )
}
