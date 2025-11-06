package nerd.tuxmobil.fahrplan.congress.settings

import android.content.Intent
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
                    onSetActivityResult = ::setActivityResult
                )
            }
        }
    }

    private fun setActivityResult(keys: List<String>) {
        val resultIntent = Intent().apply {
            for (key in keys) {
                putExtra(key, true)
            }
        }

        setResult(RESULT_OK, resultIntent)
    }
}
