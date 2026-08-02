package com.siroha.designsystem.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val placeholderPalette = listOf(
    Color(0xFF0078D4), Color(0xFF107C10), Color(0xFFC42B1C),
    Color(0xFF8764B8), Color(0xFFFFB900), Color(0xFF00B7C3)
)

private fun placeholderColorFor(label: String): Color =
    placeholderPalette[kotlin.math.abs(label.hashCode()) % placeholderPalette.size]

/**
 * Shared icon rendering surface used by Desktop, Taskbar, Start Menu, and
 * App Drawer. Renders the real decoded app icon when available; otherwise
 * falls back to a deterministic colored initial so the UI never shows a
 * blank/broken tile while an icon is still loading from IconRepository.
 */
@Composable
fun AppIcon(
    label: String,
    bitmap: Bitmap?,
    size: Dp,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = label,
                modifier = Modifier.size(size)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(size)
                    .background(placeholderColorFor(label)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label.take(1).uppercase(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
