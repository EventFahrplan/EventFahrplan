package nerd.tuxmobil.fahrplan.congress.designsystem.icons

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale.Companion.FillBounds
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun IconBoxed(
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier
            .size(36.dp)
            .paint(
                painter = painterResource(icon),
                contentScale = FillBounds,
            ),
        contentAlignment = Alignment.Center,
        content = { content() },
    )
}
