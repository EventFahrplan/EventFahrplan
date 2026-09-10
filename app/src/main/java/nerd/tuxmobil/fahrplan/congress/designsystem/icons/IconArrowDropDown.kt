package nerd.tuxmobil.fahrplan.congress.designsystem.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme

@Composable
fun IconArrowDropDown(modifier: Modifier = Modifier) {
    IconDecorativeVector(
        imageVector = Icons.Filled.ArrowDropDown,
        modifier = modifier,
    )
}

@PreviewLightDark
@Composable
private fun IconArrowDropDownPreview() {
    EventFahrplanTheme {
        IconArrowDropDown()
    }
}
