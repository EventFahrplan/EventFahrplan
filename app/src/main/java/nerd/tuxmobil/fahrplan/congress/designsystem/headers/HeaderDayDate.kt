package nerd.tuxmobil.fahrplan.congress.designsystem.headers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.designsystem.dividers.DividerHorizontal
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.Text

@Composable
fun HeaderDayDate(text: String) {
    Column(
        Modifier.Companion.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
    ) {
        val color = colorResource(R.color.text_link_on_light)
        Text(
            color = color,
            text = text.uppercase(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Companion.Bold,
        )
        DividerHorizontal(thickness = 1.dp, color = color)
    }
}

@Preview
@Composable
private fun HeaderDayDatePreview() {
    HeaderDayDate("Day 1 - 31.02.2023")
}
