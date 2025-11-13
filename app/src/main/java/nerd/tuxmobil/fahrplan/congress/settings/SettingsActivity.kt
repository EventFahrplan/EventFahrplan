package nerd.tuxmobil.fahrplan.congress.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import nerd.tuxmobil.fahrplan.congress.base.BaseActivity
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme

class SettingsActivity : BaseActivity() {

    companion object {

        const val REQUEST_CODE = 5

        fun startForResult(activity: Activity) {
            val intent = Intent(activity, SettingsActivity::class.java)
            activity.startActivityForResult(intent, REQUEST_CODE)
        }

    }

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
