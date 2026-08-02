package com.siroha.feature.desktop.components

import android.graphics.Bitmap
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.siroha.designsystem.components.AppIcon

/**
 * Single desktop grid tile. Renders the real app icon via IconRepository
 * (passed down as a Bitmap by the caller) with a deterministic colored
 * placeholder fallback while the bitmap is still loading.
 *
 * Only handles tap (onClick). Long-press-to-drag is detected by the parent
 * DesktopGrid via a separate pointerInput chained onto this composable's
 * modifier — keeping both gesture detectors on the same tile would race
 * for the same long-press pointer event.
 */
@Composable
fun DesktopIcon(
    label: String,
    showLabel: Boolean,
    iconSizeDp: Int,
    bitmap: Bitmap?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppIcon(
            label = label,
            bitmap = bitmap,
            size = iconSizeDp.dp,
            cornerRadius = 14.dp
        )
        if (showLabel) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
