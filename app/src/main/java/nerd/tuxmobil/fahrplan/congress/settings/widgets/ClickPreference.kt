package nerd.tuxmobil.fahrplan.congress.settings.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nerd.tuxmobil.fahrplan.congress.designsystem.modifiers.minimumInteractiveComponentSize

@Composable
internal fun ClickPreference(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: () -> Unit,
) {
    PreferenceText(
        title = title,
        subtitle = subtitle,
        modifier = modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick)
            .padding(
                horizontal = PREFERENCE_HORIZONTAL_PADDING_DP.dp,
                vertical = PREFERENCE_VERTICAL_PADDING_DP.dp,
            )
    )
}
