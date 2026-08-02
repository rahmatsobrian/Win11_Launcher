package com.siroha.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Win11Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp)
)

/** Corner radius tokens exposed directly for non-Shape use sites (e.g. taskbar pill). */
data class Win11CornerRadius(
    val contextMenu: androidx.compose.ui.unit.Dp = 8.dp,
    val startMenu: androidx.compose.ui.unit.Dp = 12.dp,
    val taskbar: androidx.compose.ui.unit.Dp = 18.dp,
    val widget: androidx.compose.ui.unit.Dp = 12.dp,
    val tile: androidx.compose.ui.unit.Dp = 6.dp
)

val DefaultWin11CornerRadius = Win11CornerRadius()
