package nerd.tuxmobil.fahrplan.congress.designsystem.buttons

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.OutlinedButton as Material3OutlinedButton

@Composable
fun ButtonOutlined(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    Material3OutlinedButton(
        onClick = { onClick() },
        modifier = modifier,
        content = content,
    )
}
