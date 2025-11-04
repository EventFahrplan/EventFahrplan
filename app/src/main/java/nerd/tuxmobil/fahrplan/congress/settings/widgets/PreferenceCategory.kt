package nerd.tuxmobil.fahrplan.congress.settings.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.Text
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme

@Composable
internal fun PreferenceCategory(
    text: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Spacer(Modifier.height(20.dp))

        Text(
            text = text,
            style = EventFahrplanTheme.typography.titleSmall,
            color = EventFahrplanTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = PREFERENCE_HORIZONTAL_PADDING_DP.dp)
        )

        Spacer(Modifier.height(8.dp))

        content()
    }
}
