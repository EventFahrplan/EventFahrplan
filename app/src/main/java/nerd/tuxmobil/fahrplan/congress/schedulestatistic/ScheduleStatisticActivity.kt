package nerd.tuxmobil.fahrplan.congress.schedulestatistic

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import nerd.tuxmobil.fahrplan.congress.base.BaseActivity
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme

class ScheduleStatisticActivity : BaseActivity() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ScheduleStatisticActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            EventFahrplanTheme {
                ScheduleStatisticScreen(
                    onBack = { onBackPressedDispatcher.onBackPressed() },
                )
            }
        }
    }

}
