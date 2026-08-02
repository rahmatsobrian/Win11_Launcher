package com.siroha.core.domain.usecase

import com.siroha.core.domain.model.AppInfo
import com.siroha.core.domain.repository.InstalledAppsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class SearchResults(
    val apps: List<AppInfo>,
    val query: String
)

/**
 * Ranks app matches by: exact label match > label starts-with > label
 * contains > package-name contains. Kept simple and synchronous over the
 * already-cached app list rather than hitting PackageManager per keystroke.
 */
class SearchLauncherUseCase @Inject constructor(
    private val installedAppsRepository: InstalledAppsRepository
) {
    suspend operator fun invoke(query: String): SearchResults {
        if (query.isBlank()) return SearchResults(emptyList(), query)

        val allApps = installedAppsRepository.observeInstalledApps().first()
        val normalizedQuery = query.trim().lowercase()

        val ranked = allApps
            .filter { app ->
                app.label.lowercase().contains(normalizedQuery) ||
                    app.packageName.lowercase().contains(normalizedQuery)
            }
            .sortedWith(
                compareBy(
                    { app -> rankFor(app, normalizedQuery) },
                    { app -> app.label.lowercase() }
                )
            )

        return SearchResults(apps = ranked, query = query)
    }

    private fun rankFor(app: AppInfo, query: String): Int {
        val label = app.label.lowercase()
        return when {
            label == query -> 0
            label.startsWith(query) -> 1
            label.contains(query) -> 2
            else -> 3
        }
    }
}

fun Flow<List<AppInfo>>.filterByQuery(query: String): Flow<List<AppInfo>> =
    map { apps ->
        if (query.isBlank()) apps
        else apps.filter { it.label.contains(query, ignoreCase = true) }
    }
