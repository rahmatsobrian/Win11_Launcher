package com.siroha.core.domain.usecase

import com.siroha.core.domain.model.DesktopItem
import com.siroha.core.domain.model.DesktopSettings
import com.siroha.core.domain.model.GridPosition
import com.siroha.core.domain.repository.DesktopRepository
import com.siroha.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject

/**
 * Places an app shortcut on the desktop at the first unoccupied grid cell,
 * scanning left-to-right, top-to-bottom starting from page 0. If every cell
 * on every existing page is full, it places the shortcut at (0,0) of a new
 * page rather than silently failing — a launcher that can add pages but
 * refuses to add an icon "because the grid is full" would be a confusing
 * dead end for the user.
 */
class AddAppToDesktopUseCase @Inject constructor(
    private val desktopRepository: DesktopRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(appComponentKey: String) {
        val settings: DesktopSettings = settingsRepository.observeSettings().first().desktop
        val pageCount = maxOf(desktopRepository.observePageCount().first(), settings.pageCount, 1)

        for (page in 0 until pageCount) {
            val occupied = desktopRepository.observeDesktopItems(page).first()
                .map { it.position.row to it.position.column }
                .toSet()

            for (row in 0 until settings.gridRows) {
                for (column in 0 until settings.gridColumns) {
                    if ((row to column) !in occupied) {
                        addShortcut(appComponentKey, GridPosition(page, row, column))
                        return
                    }
                }
            }
        }

        // Every existing page is full — place on a fresh page instead of
        // silently dropping the request.
        addShortcut(appComponentKey, GridPosition(page = pageCount, row = 0, column = 0))
    }

    private suspend fun addShortcut(appComponentKey: String, position: GridPosition) {
        desktopRepository.addItem(
            DesktopItem.AppShortcut(
                id = UUID.randomUUID().toString(),
                position = position,
                appComponentKey = appComponentKey
            )
        )
    }
}
