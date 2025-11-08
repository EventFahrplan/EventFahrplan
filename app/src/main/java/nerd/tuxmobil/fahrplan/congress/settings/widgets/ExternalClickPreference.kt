package nerd.tuxmobil.fahrplan.congress.settings.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.designsystem.modifiers.minimumInteractiveComponentSize
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme

@Composable
internal fun ExternalClickPreference(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick)
            .padding(
                horizontal = PREFERENCE_HORIZONTAL_PADDING_DP.dp,
                vertical = PREFERENCE_VERTICAL_PADDING_DP.dp,
            ),
    ) {
        PreferenceText(
            title = title,
            subtitle = subtitle,
            modifier = Modifier.weight(1f),
        )

        Image(
            painter = painterResource(R.drawable.ic_open_external),
            colorFilter = ColorFilter.tint(EventFahrplanTheme.colorScheme.onSurface),
            contentDescription = null,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}
