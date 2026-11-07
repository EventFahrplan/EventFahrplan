package nerd.tuxmobil.fahrplan.congress.utils.compose

import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityManager.TouchExplorationStateChangeListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Returns whether touch exploration (e.g. TalkBack) is currently enabled.
 * The state updates when the user turns the screen reader on or off.
 */
@Composable
internal fun rememberTouchExplorationEnabled(): State<Boolean> {
    val context = LocalContext.current
    val accessibilityManager = remember(context) {
        context.getSystemService(AccessibilityManager::class.java)
    }
    val enabled = remember(accessibilityManager) {
        mutableStateOf(accessibilityManager?.isTouchExplorationEnabled == true)
    }
    DisposableEffect(accessibilityManager) {
        if (accessibilityManager == null) {
            return@DisposableEffect onDispose {}
        }
        val listener = TouchExplorationStateChangeListener { enabled.value = it }
        accessibilityManager.addTouchExplorationStateChangeListener(listener)
        enabled.value = accessibilityManager.isTouchExplorationEnabled
        onDispose {
            accessibilityManager.removeTouchExplorationStateChangeListener(listener)
        }
    }
    return enabled
}
