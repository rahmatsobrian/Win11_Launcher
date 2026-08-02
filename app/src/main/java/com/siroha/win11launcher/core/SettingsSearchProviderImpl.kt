package com.siroha.win11launcher.core

import com.siroha.feature.search.SearchResultItem
import com.siroha.feature.search.SettingsSearchProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Backed by a static index rather than reflecting over SettingsScreen's
 * Composables, since the settings surface here is small and fixed. If the
 * settings tree grows substantially, replace this with a generated index
 * (e.g. an annotation-processed registry) rather than hand-maintaining it.
 */
@Singleton
class SettingsSearchProviderImpl @Inject constructor() : SettingsSearchProvider {

    private val index: List<SearchResultItem.SettingResult> = listOf(
        entry("theme_mode", "Theme", "Light, dark, or auto"),
        entry("dynamic_color", "Dynamic color", "Wallpaper-based accent color"),
        entry("blur_intensity", "Blur intensity", "Acrylic blur strength"),
        entry("corner_radius", "Corner radius", "Rounded corner amount"),
        entry("animations", "Animations", "Enable or disable motion"),
        entry("taskbar_alignment", "Taskbar alignment", "Center or left-aligned icons"),
        entry("taskbar_autohide", "Auto-hide taskbar", "Hide taskbar when not in use"),
        entry("taskbar_lock", "Lock taskbar", "Prevent taskbar layout changes"),
        entry("taskbar_transparency", "Taskbar transparency", "Acrylic transparency level"),
        entry("start_menu_recommended", "Recommended apps", "Show recommended section in Start"),
        entry("start_menu_pinned_rows", "Pinned rows", "Number of pinned app rows in Start"),
        entry("desktop_grid", "Grid size", "Desktop columns and rows"),
        entry("desktop_labels", "Icon labels", "Show or hide desktop icon labels"),
        entry("desktop_lock", "Lock desktop layout", "Prevent desktop icon changes"),
        entry("app_lock", "App Lock", "Biometric lock for chosen apps"),
        entry("developer_options", "Developer options", "FPS counter, benchmarks, debug tools"),
        entry("about", "About", "Version, licenses, feedback")
    )

    override suspend fun search(query: String): List<SearchResultItem.SettingResult> {
        if (query.isBlank()) return emptyList()
        val normalized = query.trim().lowercase()
        return index.filter { it.title.lowercase().contains(normalized) }
    }

    private fun entry(id: String, title: String, subtitle: String) =
        SearchResultItem.SettingResult(id = id, title = title, subtitle = subtitle, settingsRoute = "settings")
}
