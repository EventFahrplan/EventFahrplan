package nerd.tuxmobil.fahrplan.congress.favorites

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import info.metadude.android.eventfahrplan.commons.logging.Logging
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment.OnSessionListClick
import nerd.tuxmobil.fahrplan.congress.base.BaseActivity
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsActivity
import nerd.tuxmobil.fahrplan.congress.utils.ConfirmationDialog.OnConfirmationDialogClicked

class StarredListActivity :
    BaseActivity(),
    OnSessionListClick,
    OnConfirmationDialogClicked {

    companion object {

        private const val LOG_TAG = "StarredListActivity"

        @JvmStatic
        fun start(context: Context) {
            val intent = Intent(context, StarredListActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val logging = Logging.get()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_list)
        val toolbar = requireViewByIdCompat<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBarColor = ContextCompat.getColor(this, R.color.colorActionBar)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(actionBarColor))
        if (savedInstanceState == null) {
            addFragment(R.id.container, StarredListFragment(), StarredListFragment.FRAGMENT_TAG)
        }
    }

    override fun onSessionListClick(sessionId: String) {
        SessionDetailsActivity.start(this, sessionId)
    }

    override fun onAccepted(dlgId: Int) {
        val fragment = findFragment(StarredListFragment.FRAGMENT_TAG)
        if (fragment is StarredListFragment) {
            fragment.deleteAllFavorites()
        } else {
            logging.e(LOG_TAG, "StarredListFragment not found")
        }
    }

}
