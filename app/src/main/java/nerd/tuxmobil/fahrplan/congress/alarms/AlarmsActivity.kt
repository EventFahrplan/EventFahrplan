package nerd.tuxmobil.fahrplan.congress.alarms

import android.content.Context
import android.content.Intent
import android.os.Bundle
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.base.BaseActivity
import nerd.tuxmobil.fahrplan.congress.base.OnSessionItemClickListener
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsActivity
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository

class AlarmsActivity : BaseActivity(),
    OnSessionItemClickListener {

    companion object {

        fun start(context: Context) {
            val intent = Intent(context, AlarmsActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarms)
        if (savedInstanceState == null) {
            addFragment(R.id.container, AlarmsFragment(), AlarmsFragment.FRAGMENT_TAG)
        }
    }

    override fun onSessionItemClick(sessionId: String) {
        if (AppRepository.updateSelectedSessionId(sessionId)) {
            SessionDetailsActivity.start(this)
        }
    }

}
