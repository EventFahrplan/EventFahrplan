package nerd.tuxmobil.fahrplan.congress.details

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.base.BaseActivity
import nerd.tuxmobil.fahrplan.congress.extensions.applyEdgeToEdgeInsets
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.utils.setShowWhenLockedCompat

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
        setShowWhenLockedCompat(AppRepository.readShowOnLockscreenEnabled())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireViewByIdCompat<View>(R.id.root_layout).applyEdgeToEdgeInsets()

        if (savedInstanceState == null) {
            val fragment = SessionDetailsFragment.newInstance(sidePane = false)
            addFragment(R.id.fragment_container_view, fragment, SessionDetailsFragment.FRAGMENT_TAG)
        }
    }

}
