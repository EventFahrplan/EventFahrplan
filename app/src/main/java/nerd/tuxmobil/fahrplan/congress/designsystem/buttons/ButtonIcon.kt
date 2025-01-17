package nerd.tuxmobil.fahrplan.congress.designsystem.buttons

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.IconButton as Material3IconButton

@Composable
fun ButtonIcon(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Material3IconButton(
        onClick = { onClick() },
        modifier = modifier,
        content = content,
    )
}
