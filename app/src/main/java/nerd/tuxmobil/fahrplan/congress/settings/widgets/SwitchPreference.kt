package nerd.tuxmobil.fahrplan.congress.settings.widgets

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import nerd.tuxmobil.fahrplan.congress.designsystem.buttons.Switch
import nerd.tuxmobil.fahrplan.congress.designsystem.indicators.ripple
import nerd.tuxmobil.fahrplan.congress.designsystem.modifiers.WithoutRipple
import nerd.tuxmobil.fahrplan.congress.designsystem.modifiers.minimumInteractiveComponentSize

@Composable
internal fun SwitchPreference(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        verticalAlignment = CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .toggleable(
                value = checked,
                interactionSource = interactionSource,
                indication = ripple(),
                role = Role.Switch,
                onValueChange = onCheckedChange,
            )
            .padding(
                horizontal = PREFERENCE_HORIZONTAL_PADDING_DP.dp,
                vertical = PREFERENCE_VERTICAL_PADDING_DP.dp,
            )
    ) {
        PreferenceText(
            title = title,
            subtitle = subtitle,
            modifier = Modifier.weight(1f),
        )

        // The ripple effect is displayed on the Row. Don't show a separate one on the Switch.
        WithoutRipple {
            Switch(
                checked = checked,
                onCheckedChange = null,
                // Clicks on the row will highlight the switch thumb
                interactionSource = interactionSource,
                modifier = Modifier.padding(start = 16.dp),
            )
        }
    }
}
