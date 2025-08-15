package nerd.tuxmobil.fahrplan.congress.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.commit
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.base.BaseActivity
import nerd.tuxmobil.fahrplan.congress.extensions.applyToolbar
import nerd.tuxmobil.fahrplan.congress.extensions.applyTopAndSideInsets

class SettingsActivity : BaseActivity(R.layout.activity_generic) {

    companion object {

        const val REQUEST_CODE = 5

        fun startForResult(activity: Activity) {
            val intent = Intent(activity, SettingsActivity::class.java)
            activity.startActivityForResult(intent, REQUEST_CODE)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val toolbar = requireViewByIdCompat<Toolbar>(R.id.toolbar)
        applyToolbar(toolbar)

        val rootLayout = requireViewByIdCompat<View>(R.id.root_layout)
        rootLayout.applyTopAndSideInsets(viewToApplyTo = toolbar)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                add(R.id.fragment_container_view, SettingsFragment())
            }
        }
    }

}
