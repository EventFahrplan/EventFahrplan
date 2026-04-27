package nerd.tuxmobil.fahrplan.congress.designsystem.icons

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme

@Composable
fun IconDelete(modifier: Modifier = Modifier) {
    IconActionable(
        modifier = modifier.size(EventFahrplanTheme.dimensions.iconSize),
        icon = R.drawable.ic_delete,
    )
}

@PreviewLightDark
@Composable
private fun IconDeletePreview() {
    EventFahrplanTheme {
        IconDelete()
    }
}
