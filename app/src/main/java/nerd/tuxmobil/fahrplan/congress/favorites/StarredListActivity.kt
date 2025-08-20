package nerd.tuxmobil.fahrplan.congress.favorites

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import info.metadude.android.eventfahrplan.commons.logging.Logging
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment.OnSessionListClick
import nerd.tuxmobil.fahrplan.congress.base.BaseActivity
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsActivity
import nerd.tuxmobil.fahrplan.congress.extensions.applyToolbar
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.utils.ConfirmationDialog.OnConfirmationDialogClicked

class StarredListActivity : BaseActivity(R.layout.activity_generic), OnSessionListClick, OnConfirmationDialogClicked {

    companion object {

        private const val LOG_TAG = "StarredListActivity"

        fun start(context: Context) {
            val intent = Intent(context, StarredListActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val logging = Logging.get()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val toolbar = requireViewByIdCompat<Toolbar>(R.id.toolbar)
        applyToolbar(toolbar)

        if (savedInstanceState == null) {
            addFragment(R.id.fragment_container_view, StarredListFragment(), StarredListFragment.FRAGMENT_TAG)
        }
    }

    override fun onSessionListClick(sessionId: String) {
        if (AppRepository.updateSelectedSessionId(sessionId)) {
            SessionDetailsActivity.start(this)
        }
    }

    override fun onAccepted(requestCode: Int) {
        val fragment = findFragment(StarredListFragment.FRAGMENT_TAG)
        if (fragment is StarredListFragment) {
            fragment.deleteAllFavorites()
        } else {
            logging.e(LOG_TAG, "StarredListFragment not found")
        }
    }

}
