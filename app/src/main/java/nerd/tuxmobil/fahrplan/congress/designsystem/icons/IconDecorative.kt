package nerd.tuxmobil.fahrplan.congress.designsystem.icons

import androidx.annotation.DrawableRes
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Icon as Material3Icon

@Composable
fun IconDecorative(
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    Material3Icon(
        modifier = modifier,
        painter = painterResource(icon),
        tint = tint,
        contentDescription = null
    )
}
