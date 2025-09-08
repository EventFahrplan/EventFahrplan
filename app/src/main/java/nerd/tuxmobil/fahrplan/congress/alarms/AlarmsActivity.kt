package nerd.tuxmobil.fahrplan.congress.alarms

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.base.BaseActivity
import nerd.tuxmobil.fahrplan.congress.base.OnSessionItemClickListener
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsActivity
import nerd.tuxmobil.fahrplan.congress.extensions.applyEdgeToEdgeInsets
import nerd.tuxmobil.fahrplan.congress.extensions.applyToolbar
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository

class AlarmsActivity : BaseActivity(R.layout.activity_generic), OnSessionItemClickListener {

    companion object {

        fun start(context: Context) {
            val intent = Intent(context, AlarmsActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val toolbar = requireViewByIdCompat<Toolbar>(R.id.toolbar)
        applyToolbar(toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }

        val rootLayout = requireViewByIdCompat<View>(R.id.root_layout)
        rootLayout.applyEdgeToEdgeInsets()

        if (savedInstanceState == null) {
            addFragment(R.id.fragment_container_view, AlarmsFragment(), AlarmsFragment.FRAGMENT_TAG)
        }
    }

    override fun onSessionItemClick(sessionId: String) {
        if (AppRepository.updateSelectedSessionId(sessionId)) {
            SessionDetailsActivity.start(this)
        }
    }

}
