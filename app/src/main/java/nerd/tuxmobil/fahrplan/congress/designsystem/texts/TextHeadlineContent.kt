package nerd.tuxmobil.fahrplan.congress.designsystem.texts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.Text as Material3Text

@Composable
fun TextHeadlineContent(
    text: String,
    modifier: Modifier = Modifier,
) {
    Material3Text(
        text = text,
        modifier = modifier,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
    )
}
