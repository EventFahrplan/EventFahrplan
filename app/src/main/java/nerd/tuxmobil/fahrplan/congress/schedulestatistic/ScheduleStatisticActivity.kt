package nerd.tuxmobil.fahrplan.congress.schedulestatistic

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.base.BaseActivity
import nerd.tuxmobil.fahrplan.congress.extensions.applyEdgeToEdgeInsets

class ScheduleStatisticActivity : BaseActivity() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ScheduleStatisticActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_statistic)

        val rootLayout = requireViewByIdCompat<View>(R.id.root_layout)
        rootLayout.applyEdgeToEdgeInsets()

        if (savedInstanceState == null) {
            addFragment(R.id.container, ScheduleStatisticFragment(), ScheduleStatisticFragment.FRAGMENT_TAG)
        }
    }

}
