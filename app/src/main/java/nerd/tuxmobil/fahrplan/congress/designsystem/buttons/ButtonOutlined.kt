package nerd.tuxmobil.fahrplan.congress.designsystem.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.OutlinedButton as Material3OutlinedButton

@Composable
fun ButtonOutlined(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    border: BorderStroke? = ButtonDefaults.outlinedButtonBorder(enabled),
    content: @Composable RowScope.() -> Unit,
) {
    Material3OutlinedButton(
        onClick = { onClick() },
        modifier = modifier,
        border = border,
        content = content,
    )
}
