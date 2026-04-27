package nerd.tuxmobil.fahrplan.congress.designsystem.icons

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import androidx.compose.material3.Icon as Material3Icon

@Composable
fun IconActionable(
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    @StringRes contentDescription: Int? = null,
    tint: Color = LocalContentColor.current,
) {
    Material3Icon(
        modifier = modifier,
        tint = tint,
        painter = painterResource(icon),
        contentDescription = if (contentDescription == null) null else stringResource(contentDescription),
    )
}

@PreviewLightDark
@Composable
private fun IconActionablePreview() {
    EventFahrplanTheme {
        IconActionable(
            icon = R.drawable.ic_action_feedback,
            contentDescription = 0,
        )
    }
}
