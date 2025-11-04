package nerd.tuxmobil.fahrplan.congress.settings.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.Text
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme

@Composable
internal fun PreferenceText(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(modifier) {
        Text(
            text = title,
            style = EventFahrplanTheme.typography.preferenceTitle,
        )

        if (subtitle != null) {
            Text(
                text = subtitle,
                style = EventFahrplanTheme.typography.bodyMedium,
            )
        }
    }
}
