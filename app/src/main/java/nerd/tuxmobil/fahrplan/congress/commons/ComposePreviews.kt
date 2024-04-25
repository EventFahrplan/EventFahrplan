package nerd.tuxmobil.fahrplan.congress.commons

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "1 Android xs",
    device = "id:Nexus 5",
    showSystemUi = true,
)
@Preview(
    name = "2 Pixel 4 XL - light",
    device = "id:pixel_4_xl",
    uiMode = UI_MODE_NIGHT_NO,
    showSystemUi = true,
)
@Preview(
    name = "3 Pixel 4 XL - dark",
    device = "id:pixel_4_xl",
    uiMode = UI_MODE_NIGHT_YES,
    showSystemUi = true,
)
@Preview(
    name = "4 Galaxy Tab A7 lite - portrait",
    device = "spec:width=800dp,height=1334dp,dpi=179",
    showSystemUi = true,
)
@Preview(
    name = "5 Galaxy Tab A7 lite - landscape",
    device = "spec:width=1334dp,height=800dp,dpi=179",
    showSystemUi = true,
)
annotation class MultiDevicePreview
