package nerd.tuxmobil.fahrplan.congress.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.base.BaseActivity
import androidx.core.graphics.drawable.toDrawable

class SettingsActivity : BaseActivity(R.layout.settings) {

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
        setSupportActionBar(toolbar)
        val actionBarColor = ContextCompat.getColor(this, R.color.colorActionBar)
        supportActionBar!!.setBackgroundDrawable(actionBarColor.toDrawable())

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                add(R.id.container, SettingsFragment())
            }
        }
    }

}
