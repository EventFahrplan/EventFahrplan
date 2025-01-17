package nerd.tuxmobil.fahrplan.congress.designsystem.texts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.material3.Text as Material3Text

@Composable
fun TextSupportingContent(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    color: Color = Color.Unspecified,
) {
    Material3Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        textDecoration = textDecoration,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
    )
}
