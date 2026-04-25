package nerd.tuxmobil.fahrplan.congress.designsystem.buttons

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.designsystem.icons.IconDecorative
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import androidx.compose.material3.IconButton as Material3IconButton

@Composable
fun ButtonIcon(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Material3IconButton(
        onClick = onClick,
        modifier = modifier,
        content = content,
    )
}

@PreviewLightDark
@Composable
private fun ButtonIconPreview() {
    EventFahrplanTheme {
        ButtonIcon(
            content = { IconDecorative(R.drawable.ic_action_navigate) },
            onClick = { },
        )
    }
}
