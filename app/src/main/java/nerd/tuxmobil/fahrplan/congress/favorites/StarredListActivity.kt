package nerd.tuxmobil.fahrplan.congress.favorites

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment.OnSessionListClick
import nerd.tuxmobil.fahrplan.congress.base.BaseActivity
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsActivity
import nerd.tuxmobil.fahrplan.congress.extensions.applyEdgeToEdgeInsets
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository

class StarredListActivity : BaseActivity(R.layout.activity_generic), OnSessionListClick {

    companion object {

        fun start(context: Context) {
            val intent = Intent(context, StarredListActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireViewByIdCompat<AppBarLayout>(R.id.app_bar_layout).apply {
            fitsSystemWindows = false
            visibility = View.GONE
        }

        val fragmentContainer = requireViewByIdCompat<View>(R.id.fragment_container_view)
        (fragmentContainer.layoutParams as CoordinatorLayout.LayoutParams).behavior = null

        val rootLayout = requireViewByIdCompat<View>(R.id.root_layout)
        rootLayout.applyEdgeToEdgeInsets()

        if (savedInstanceState == null) {
            addFragment(R.id.fragment_container_view, StarredListFragment(), StarredListFragment.FRAGMENT_TAG)
        }
    }

    override fun onSessionListClick(sessionId: String) {
        if (AppRepository.updateSelectedSessionId(sessionId)) {
            SessionDetailsActivity.start(this)
        }
    }

}
