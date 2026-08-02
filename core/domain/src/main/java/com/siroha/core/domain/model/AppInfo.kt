package com.siroha.core.domain.model

/**
 * Domain representation of an installed launchable app. Deliberately does
 * not hold a Drawable/Bitmap directly — icon bytes live in the icon cache
 * (see IconRepository) so this model stays cheap to hold in lists/StateFlow.
 */
data class AppInfo(
    val packageName: String,
    val activityClassName: String,
    val label: String,
    val userHandleId: Int,
    val isSystemApp: Boolean,
    val installTimeMillis: Long,
    val isHidden: Boolean = false,
    val isPinnedToTaskbar: Boolean = false,
    val isPinnedToStart: Boolean = false,
    val category: AppCategory = AppCategory.UNCATEGORIZED
) {
    /** Stable key across profiles (work/personal) sharing the same package name. */
    val componentKey: String
        get() = "$packageName/$activityClassName/$userHandleId"
}

enum class AppCategory {
    GAME,
    SOCIAL,
    PRODUCTIVITY,
    ENTERTAINMENT,
    UTILITY,
    SYSTEM,
    UNCATEGORIZED
}
