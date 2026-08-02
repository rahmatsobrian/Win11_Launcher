package com.siroha.feature.taskbar.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun NotificationCenterOverlay(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationCenterViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val hasAccess = viewModel.hasAccess

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(150)),
        exit = fadeOut(tween(150)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.15f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomEnd
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(tween(200)) { it / 4 } + fadeIn(tween(200)),
                exit = slideOutVertically(tween(150)) { it / 4 } + fadeOut(tween(150))
            ) {
                Column(
                    modifier = Modifier
                        .padding(bottom = 64.dp, end = 8.dp)
                        .width(320.dp)
                        .height(420.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable(enabled = false) {}
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                    ) {
                        Text(text = "Notifications", style = MaterialTheme.typography.titleMedium)
                        if (notifications.isNotEmpty()) {
                            TextButton(onClick = { viewModel.clearAll() }) {
                                Text("Clear all")
                            }
                        }
                    }

                    when {
                        !hasAccess -> NotificationAccessPrompt(onGrant = { viewModel.openAccessSettings() })
                        notifications.isEmpty() -> EmptyNotifications()
                        else -> LazyColumn {
                            items(notifications, key = { it.key }) { notification ->
                                NotificationRow(
                                    notification = notification,
                                    onDismiss = { viewModel.dismiss(notification.key) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationAccessPrompt(onGrant: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Enable notification access to see your notifications here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Button(onClick = onGrant, modifier = Modifier.padding(top = 12.dp)) {
            Text("Open settings")
        }
    }
}

@Composable
private fun EmptyNotifications() {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = "No new notifications",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun NotificationRow(
    notification: LauncherNotification,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            Text(text = notification.appLabel, style = MaterialTheme.typography.labelSmall)
            TextButton(onClick = onDismiss) { Text("Dismiss") }
        }
        Text(text = notification.title, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = notification.text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
