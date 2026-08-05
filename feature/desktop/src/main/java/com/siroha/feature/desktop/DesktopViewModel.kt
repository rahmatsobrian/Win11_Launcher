package com.siroha.feature.desktop

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siroha.core.domain.model.DesktopItem
import com.siroha.core.domain.model.GridPosition
import com.siroha.core.domain.repository.DesktopRepository
import com.siroha.core.domain.repository.IconRepository
import com.siroha.core.domain.repository.InstalledAppsRepository
import com.siroha.core.domain.repository.SettingsRepository
import com.siroha.feature.widgets.host.LauncherWidgetHost
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SEED_APP_COUNT = 12
private const val SEED_GRID_COLUMNS = 5

@HiltViewModel
class DesktopViewModel @Inject constructor(
    private val desktopRepository: DesktopRepository,
    private val settingsRepository: SettingsRepository,
    private val installedAppsRepository: InstalledAppsRepository,
    private val iconRepository: IconRepository,
    val widgetHost: LauncherWidgetHost
) : ViewModel() {

    private val currentPage = MutableStateFlow(0)
    private val isEditMode = MutableStateFlow(false)
    private val iconBitmaps = MutableStateFlow<Map<String, Bitmap>>(emptyMap())
    private val appLabels = MutableStateFlow<Map<String, String>>(emptyMap())

    init {
        seedDesktopIfEmpty()
    }

    /**
     * A brand-new install has an empty Room table for desktop_items, which
     * would otherwise render as a blank screen with no indication anything
     * is wrong — there's no visible affordance yet pointing the user to
     * "open the app drawer and long-press to add icons". Seeding a handful
     * of already-installed apps on first run gives the user something to
     * interact with immediately, matching how stock launchers ship with a
     * pre-populated home screen out of the box.
     */
    private fun seedDesktopIfEmpty() {
        viewModelScope.launch {
            val existingItems = desktopRepository.observeDesktopItems(0).first()
            if (existingItems.isNotEmpty()) return@launch

            val installedApps = installedAppsRepository.observeInstalledApps().first()
            val seedApps = installedApps
                .filterNot { it.isSystemApp }
                .ifEmpty { installedApps }
                .take(SEED_APP_COUNT)

            seedApps.forEachIndexed { index, app ->
                desktopRepository.addItem(
                    DesktopItem.AppShortcut(
                        id = java.util.UUID.randomUUID().toString(),
                        position = GridPosition(page = 0, row = index / SEED_GRID_COLUMNS, column = index % SEED_GRID_COLUMNS),
                        appComponentKey = app.componentKey
                    )
                )
            }
        }
    }

    val uiState: StateFlow<DesktopUiState> = combine(
        currentPage,
        currentPage.flatMapLatest { page -> desktopRepository.observeDesktopItems(page) },
        desktopRepository.observePageCount(),
        settingsRepository.observeSettings(),
        isEditMode
    ) { page, items, pageCount, settings, editMode ->
        DesktopPartialState(page, items, pageCount, settings, editMode)
    }.combine(iconBitmaps) { partial, icons ->
        partial to icons
    }.combine(appLabels) { (partial, icons), labels ->
        loadMissingIconsAndLabels(partial.items)

        DesktopUiState(
            currentPage = partial.page,
            pageCount = maxOf(partial.pageCount, partial.settings.desktop.pageCount, 1),
            items = partial.items,
            isEditMode = partial.editMode,
            isLayoutLocked = partial.settings.desktop.isLayoutLocked,
            gridColumns = partial.settings.desktop.gridColumns,
            gridRows = partial.settings.desktop.gridRows,
            iconSizeDp = partial.settings.desktop.iconSizeDp,
            showLabels = partial.settings.desktop.showLabels,
            isLoading = false,
            iconBitmaps = icons,
            appLabels = labels
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DesktopUiState()
    )

    private data class DesktopPartialState(
        val page: Int,
        val items: List<DesktopItem>,
        val pageCount: Int,
        val settings: com.siroha.core.domain.model.LauncherSettings,
        val editMode: Boolean
    )

    private fun loadMissingIconsAndLabels(items: List<DesktopItem>) {
        val shortcutKeys = items.filterIsInstance<DesktopItem.AppShortcut>().map { it.appComponentKey }
        val missingIcons = shortcutKeys.filter { it !in iconBitmaps.value }
        val missingLabels = shortcutKeys.filter { it !in appLabels.value }
        if (missingIcons.isEmpty() && missingLabels.isEmpty()) return

        viewModelScope.launch {
            val keysToResolve = (missingIcons + missingLabels).distinct()
            kotlinx.coroutines.coroutineScope {
                keysToResolve.map { componentKey ->
                    async {
                        val app = installedAppsRepository.getApp(componentKey) ?: return@async

                        if (componentKey in missingLabels) {
                            appLabels.update { it + (componentKey to app.label) }
                        }

                        if (componentKey in missingIcons) {
                            val bitmap = iconRepository.getIcon(componentKey, app.packageName, app.activityClassName)
                            if (bitmap != null) {
                                iconBitmaps.update { it + (componentKey to bitmap) }
                            }
                        }
                    }
                }.awaitAll()
            }
        }
    }

    fun goToPage(page: Int) {
        currentPage.update { page.coerceAtLeast(0) }
    }

    fun toggleEditMode() {
        isEditMode.update { !it }
    }

    fun moveItem(itemId: String, newPosition: GridPosition) {
        if (uiState.value.isLayoutLocked) return
        viewModelScope.launch {
            desktopRepository.moveItem(itemId, newPosition)
        }
    }

    fun removeItem(itemId: String) {
        viewModelScope.launch {
            desktopRepository.removeItem(itemId)
        }
    }

    fun renameItem(itemId: String, newLabel: String) {
        viewModelScope.launch {
            desktopRepository.renameItem(itemId, newLabel)
        }
    }

    fun addShortcut(componentKey: String, position: GridPosition) {
        viewModelScope.launch {
            desktopRepository.addItem(
                DesktopItem.AppShortcut(
                    id = java.util.UUID.randomUUID().toString(),
                    position = position,
                    appComponentKey = componentKey
                )
            )
        }
    }

    fun createFolder(name: String, memberComponentKeys: List<String>) {
        viewModelScope.launch {
            desktopRepository.createFolder(name, memberComponentKeys)
        }
    }

    fun addPage() {
        viewModelScope.launch {
            val newPage = uiState.value.pageCount
            currentPage.value = newPage
        }
    }

    fun addWidget(appWidgetId: Int, spanColumns: Int, spanRows: Int, position: GridPosition) {
        viewModelScope.launch {
            desktopRepository.addItem(
                DesktopItem.Widget(
                    id = java.util.UUID.randomUUID().toString(),
                    position = position,
                    appWidgetId = appWidgetId,
                    spanColumns = spanColumns,
                    spanRows = spanRows
                )
            )
        }
    }
}
