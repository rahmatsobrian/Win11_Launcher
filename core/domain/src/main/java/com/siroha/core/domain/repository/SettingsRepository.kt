package com.siroha.core.domain.repository

import com.siroha.core.domain.model.LauncherSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    fun observeSettings(): Flow<LauncherSettings>

    suspend fun updateSettings(transform: (LauncherSettings) -> LauncherSettings)

    suspend fun resetToDefaults()

    /** Serializes current settings + desktop layout + favorites to a portable JSON blob. */
    suspend fun exportBackup(): String

    suspend fun importBackup(json: String): Result<Unit>
}
