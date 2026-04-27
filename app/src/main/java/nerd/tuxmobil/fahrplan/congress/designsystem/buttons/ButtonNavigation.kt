package nerd.tuxmobil.fahrplan.congress.designsystem.buttons

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.designsystem.icons.IconDecorativeVector
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme

@Composable
fun ButtonNavigation(
    useCloseIcon: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (imageVector, contentDescription) = when (useCloseIcon) {
        true -> Icons.Default.Close to stringResource(R.string.menu_item_title_close_session_details)
        false -> Icons.AutoMirrored.Default.ArrowBack to stringResource(R.string.navigate_back_content_description)
    }
    ButtonIcon(
        modifier = modifier.semantics {
            this.contentDescription = contentDescription
        },
        onClick = onClick,
    ) {
        ButtonBox(
            modifier = Modifier.size(28.dp),
        ) {
            IconDecorativeVector(
                modifier = Modifier.size(18.dp),
                imageVector = imageVector,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ButtonNavigationClosePreview() {
    ButtonNavigation(true)
}

@PreviewLightDark
@Composable
private fun ButtonNavigationArrowBackPreview() {
    ButtonNavigation(false)
}

@Composable
private fun ButtonNavigation(useCloseIcon: Boolean) {
    EventFahrplanTheme {
        ButtonNavigation(
            useCloseIcon = useCloseIcon,
            onClick = {},
        )
    }
}
