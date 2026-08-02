package com.siroha.core.data.mapper

import com.siroha.core.database.entity.AppEntity
import com.siroha.core.domain.model.AppCategory
import com.siroha.core.domain.model.AppInfo

fun AppEntity.toDomain(): AppInfo = AppInfo(
    packageName = packageName,
    activityClassName = activityClassName,
    label = label,
    userHandleId = userHandleId,
    isSystemApp = isSystemApp,
    installTimeMillis = installTimeMillis,
    isHidden = isHidden,
    isPinnedToTaskbar = isPinnedToTaskbar,
    isPinnedToStart = isPinnedToStart,
    category = runCatching { AppCategory.valueOf(category) }.getOrDefault(AppCategory.UNCATEGORIZED)
)

fun AppInfo.toEntity(launchCount: Int = 0, lastLaunchedMillis: Long = 0L): AppEntity = AppEntity(
    componentKey = componentKey,
    packageName = packageName,
    activityClassName = activityClassName,
    label = label,
    userHandleId = userHandleId,
    isSystemApp = isSystemApp,
    installTimeMillis = installTimeMillis,
    isHidden = isHidden,
    isPinnedToTaskbar = isPinnedToTaskbar,
    isPinnedToStart = isPinnedToStart,
    category = category.name,
    launchCount = launchCount,
    lastLaunchedMillis = lastLaunchedMillis
)
