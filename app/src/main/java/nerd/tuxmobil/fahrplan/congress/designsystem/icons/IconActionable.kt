package nerd.tuxmobil.fahrplan.congress.designsystem.icons

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.Icon as Material3Icon

@Composable
fun IconActionable(
    @DrawableRes icon: Int,
    tint: Color,
    @StringRes contentDescription: Int,
    modifier: Modifier = Modifier,
) {
    Material3Icon(
        modifier = modifier,
        tint = tint,
        painter = painterResource(icon),
        contentDescription = stringResource(contentDescription),
    )
}
