package nerd.tuxmobil.fahrplan.congress.designsystem.headers

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.Text

@Composable
fun HeaderSessionList(text: String) {
    Text(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
        text = text,
        fontWeight = Bold,
        fontSize = 22.sp,
    )
}

@Preview
@Composable
private fun HeaderSessionListScheduleChangesPreview() {
    HeaderSessionList(stringResource(R.string.schedule_changes))
}
