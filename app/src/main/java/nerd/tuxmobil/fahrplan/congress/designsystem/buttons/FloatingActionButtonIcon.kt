package nerd.tuxmobil.fahrplan.congress.designsystem.buttons

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.designsystem.icons.IconActionable
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme

@Composable
fun FloatingActionButtonIcon(
    @DrawableRes icon: Int,
    @StringRes contentDescription: Int,
    containerColor: Color = EventFahrplanTheme.colorScheme.surfaceContainer,
    contentColor: Color = contentColorFor(containerColor),
    onClick: () -> Unit,
) {
    FloatingActionButton(
        containerColor = containerColor,
        contentColor = contentColor,
        onClick = onClick,
    ) {
        IconActionable(
            icon = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(EventFahrplanTheme.dimensions.iconSize),
            tint = contentColor,
        )
    }
}

@PreviewLightDark
@Composable
private fun FloatingActionButtonIconPreview() {
    EventFahrplanTheme {
        FloatingActionButtonIcon(
            icon = R.drawable.ic_star_outline,
            contentDescription = 0,
            onClick = {},
        )
    }
}
