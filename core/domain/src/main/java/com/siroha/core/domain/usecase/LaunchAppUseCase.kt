package com.siroha.core.domain.usecase

import com.siroha.core.domain.repository.InstalledAppsRepository
import javax.inject.Inject

/**
 * Records a launch event for "recently used" / "frequently used" surfaces.
 * The actual Intent-firing (startActivity) stays in the UI layer since it
 * needs an Android Context/Activity, which domain must not depend on.
 */
class LaunchAppUseCase @Inject constructor(
    private val installedAppsRepository: InstalledAppsRepository
) {
    suspend operator fun invoke(componentKey: String) {
        installedAppsRepository.recordAppLaunch(componentKey)
    }
}
