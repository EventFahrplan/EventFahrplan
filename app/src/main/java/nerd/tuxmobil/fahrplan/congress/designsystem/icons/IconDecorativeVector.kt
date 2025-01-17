package nerd.tuxmobil.fahrplan.congress.designsystem.icons

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon as Material3Icon

@Composable
fun IconDecorativeVector(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    Material3Icon(
        modifier = modifier,
        imageVector = imageVector,
        tint = tint,
        contentDescription = null
    )
}
