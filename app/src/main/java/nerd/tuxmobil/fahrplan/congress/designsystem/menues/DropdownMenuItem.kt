package nerd.tuxmobil.fahrplan.congress.designsystem.menues

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.DropdownMenuItem as Material3DropdownMenuItem

@Composable
fun DropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Material3DropdownMenuItem(
        text = text,
        onClick = onClick,
        modifier = modifier,
    )
}
