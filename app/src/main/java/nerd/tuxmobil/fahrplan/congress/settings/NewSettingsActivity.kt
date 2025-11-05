package nerd.tuxmobil.fahrplan.congress.settings

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import nerd.tuxmobil.fahrplan.congress.base.BaseActivity
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme

class NewSettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            EventFahrplanTheme {
                SettingsScreen(
                    onBack = { onBackPressedDispatcher.onBackPressed() },
                )
            }
        }
    }
}
