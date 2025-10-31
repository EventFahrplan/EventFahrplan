package nerd.tuxmobil.fahrplan.congress.designsystem.modifiers

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.material3.minimumInteractiveComponentSize as material3MinimumInteractiveComponentSize

@Stable
fun Modifier.minimumInteractiveComponentSize(): Modifier {
    return material3MinimumInteractiveComponentSize()
}
