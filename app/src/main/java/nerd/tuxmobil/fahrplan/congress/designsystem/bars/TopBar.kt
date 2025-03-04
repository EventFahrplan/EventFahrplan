package nerd.tuxmobil.fahrplan.congress.designsystem.bars

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.designsystem.buttons.ButtonIcon
import nerd.tuxmobil.fahrplan.congress.designsystem.icons.IconDecorativeVector
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.Text

@Composable
fun TopBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(R.color.text_primary),
            )
        },
        navigationIcon = {
            val backContentDescription = stringResource(R.string.navigate_back_content_description)
            ButtonIcon(
                modifier = Modifier.semantics {
                    contentDescription = backContentDescription
                },
                onClick = { onBack() },
            ) {
                IconDecorativeVector(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    tint = colorResource(R.color.text_primary),
                )
            }
        },
        actions = {
            actions()
        },
    )
}

@Preview
@Composable
private fun TopBarPreview() {
    TopBar(
        title = "TopBar Title",
        onBack = {},
        actions = {},
    )
}
