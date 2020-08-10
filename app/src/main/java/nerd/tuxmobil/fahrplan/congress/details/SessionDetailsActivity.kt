package nerd.tuxmobil.fahrplan.congress.details

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import nerd.tuxmobil.fahrplan.congress.MyApp
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.base.BaseActivity
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.models.Session

class SessionDetailsActivity : BaseActivity() {

    companion object {

        @JvmStatic
        fun startForResult(activity: Activity, session: Session) {
            val extras = bundleOf(BundleKeys.SESSION_ID to session.sessionId)
            with(Intent(activity, SessionDetailsActivity::class.java)) {
                putExtras(extras)
                activity.startActivityForResult(this, MyApp.SESSION_VIEW)
            }
        }

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val actionBarColor = ContextCompat.getColor(this, R.color.colorActionBar)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(actionBarColor))
        setContentView(R.layout.detail_frame)

        val intent = this.intent
        if (intent == null) {
            finish()
        }
        if (intent != null && findViewById<View?>(R.id.detail) != null) {
            openDetails(intent.getStringExtra(BundleKeys.SESSION_ID))
        }
    }

    private fun openDetails(sessionId: String) {
        val args = bundleOf(BundleKeys.SESSION_ID to sessionId)
        val fragment = SessionDetailsFragment().apply { arguments = args }
        replaceFragment(R.id.detail, fragment, SessionDetailsFragment.FRAGMENT_TAG)
    }

}
