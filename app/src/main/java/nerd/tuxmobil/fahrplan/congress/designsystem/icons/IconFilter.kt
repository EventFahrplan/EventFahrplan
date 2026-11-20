package nerd.tuxmobil.fahrplan.congress.designsystem.icons

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme

@Composable
fun IconFilter(
    enabled: Boolean,
    @StringRes contentDescription: Int,
) {
    IconActionable(
        modifier = Modifier.size(EventFahrplanTheme.dimensions.iconSize),
        icon = if (enabled) R.drawable.ic_filter_filled else R.drawable.ic_filter_outline,
        contentDescription = contentDescription,
    )
}

@PreviewLightDark
@Composable
private fun IconFilterEnabledPreview() {
    EventFahrplanTheme {
        IconFilter(enabled = true, contentDescription = 0)
    }
}

@PreviewLightDark
@Composable
private fun IconFilterDisabledPreview() {
    EventFahrplanTheme {
        IconFilter(enabled = false, contentDescription = 0)
    }
}
