package com.siroha.core.common.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Launches a coroutine that swallows exceptions into a callback instead of
 * crashing the launcher process — critical here specifically, because this
 * app *is* the home screen: an uncaught exception on a UI-triggering
 * coroutine can leave the user with no way back to a working home screen
 * short of a reboot into safe mode.
 */
fun CoroutineScope.launchSafely(
    onError: (Throwable) -> Unit = {},
    block: suspend CoroutineScope.() -> Unit
) {
    launch {
        try {
            block()
        } catch (t: Throwable) {
            onError(t)
        }
    }
}
