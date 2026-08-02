package com.siroha.designsystem.components

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

data class ContextMenuAction(
    val label: String,
    val icon: ImageVector? = null,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit
)

/**
 * Shared long-press context menu used by Desktop icons, Taskbar pinned
 * apps, and App Drawer rows — keeps the "uninstall / app info / pin" menu
 * visually and behaviorally consistent across every surface that lists
 * apps, matching how Windows 11's right-click menu looks the same whether
 * invoked from the taskbar, Start, or desktop.
 */
@Composable
fun AppContextMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    actions: List<ContextMenuAction>,
    offset: DpOffset = DpOffset(0.dp, 0.dp)
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        offset = offset
    ) {
        actions.forEach { action ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = action.label,
                        color = if (action.isDestructive) {
                            androidx.compose.ui.graphics.Color(0xFFC42B1C)
                        } else {
                            androidx.compose.ui.graphics.Color.Unspecified
                        }
                    )
                },
                leadingIcon = action.icon?.let { icon ->
                    { Icon(imageVector = icon, contentDescription = null) }
                },
                onClick = {
                    onDismiss()
                    action.onClick()
                }
            )
        }
    }
}
