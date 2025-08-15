package nerd.tuxmobil.fahrplan.congress.details

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.R.id.root_layout
import nerd.tuxmobil.fahrplan.congress.R.id.toolbar
import nerd.tuxmobil.fahrplan.congress.R.string.session_details_screen_name
import nerd.tuxmobil.fahrplan.congress.base.BaseActivity
import nerd.tuxmobil.fahrplan.congress.extensions.applyToolbar
import nerd.tuxmobil.fahrplan.congress.extensions.applyTopAndSideInsets
import nerd.tuxmobil.fahrplan.congress.extensions.isLandscape
import nerd.tuxmobil.fahrplan.congress.utils.showWhenLockedCompat

class SessionDetailsActivity : BaseActivity(R.layout.activity_generic) {

    companion object {

        const val REQUEST_CODE = 2

        fun start(activity: Activity) {
            activity.startActivity(createIntent(activity))
        }

        fun startForResult(activity: Activity) {
            activity.startActivityForResult(createIntent(activity), REQUEST_CODE)
        }

        private fun createIntent(activity: Activity) =
            Intent(activity, SessionDetailsActivity::class.java)

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        showWhenLockedCompat()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val toolbar = requireViewByIdCompat<Toolbar>(toolbar)
        applyToolbar(toolbar) {
            this.title = if (this@SessionDetailsActivity.isLandscape()) this@SessionDetailsActivity.getString(session_details_screen_name) else ""
            setDisplayHomeAsUpEnabled(true)
        }

        val rootLayout = requireViewByIdCompat<View>(root_layout)
        rootLayout.applyTopAndSideInsets(viewToApplyTo = toolbar)

        if (savedInstanceState == null) {
            val fragment = SessionDetailsFragment.newInstance(sidePane = false)
            addFragment(R.id.fragment_container_view, fragment, SessionDetailsFragment.FRAGMENT_TAG)
        }
    }

}
