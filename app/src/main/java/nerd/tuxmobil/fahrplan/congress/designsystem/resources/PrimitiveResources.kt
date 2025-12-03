package nerd.tuxmobil.fahrplan.congress.designsystem.resources

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.util.TypedValue
import androidx.annotation.DimenRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalResources

/**
 * Loads a float resource.
 * Source: https://github.com/teogor/ceres/blob/d7381c48a88dd5f887fb41e0ad13475a47589e87/core/foundation/src/main/kotlin/dev/teogor/ceres/core/foundation/compositions/LocalResoures.kt
 */
@Composable
fun floatResource(@DimenRes id: Int): Float {
    val resources = LocalResources.current
    return remember(id) {
        if (SDK_INT < Q) {
            TypedValue().apply {
                resources.getValue(id, this, true)
            }.float
        } else {
            resources.getFloat(id)
        }
    }
}
