package nerd.tuxmobil.fahrplan.congress.details

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.base.BaseActivity
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.extensions.withArguments
import nerd.tuxmobil.fahrplan.congress.extensions.withExtras
import nerd.tuxmobil.fahrplan.congress.utils.showWhenLockedCompat

class SessionDetailsActivity : BaseActivity(R.layout.detail_frame) {

    companion object {

        const val REQUEST_CODE = 2

        @JvmStatic
        fun start(activity: Activity, sessionId: String) {
            activity.startActivity(createIntent(activity, sessionId))
        }

        @JvmStatic
        fun startForResult(activity: Activity, sessionId: String) {
            activity.startActivityForResult(createIntent(activity, sessionId), REQUEST_CODE)
        }

        private fun createIntent(activity: Activity, sessionId: String) =
            Intent(activity, SessionDetailsActivity::class.java).withExtras(
                BundleKeys.SESSION_ID to sessionId
            )

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        showWhenLockedCompat()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val actionBarColor = ContextCompat.getColor(this, R.color.colorActionBar)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(actionBarColor))

        val intent = this.intent
        if (intent == null) {
            finish()
        }
        if (intent != null && findViewById<View?>(R.id.detail) != null) {
            val sessionId = requireNotNull(intent.getStringExtra(BundleKeys.SESSION_ID)) {
                "Bundle does not contain a SESSION_ID."
            }
            openDetails(sessionId)
        }
    }

    private fun openDetails(sessionId: String) {
        val fragment = SessionDetailsFragment().withArguments(
                BundleKeys.SESSION_ID to sessionId
        )
        replaceFragment(R.id.detail, fragment, SessionDetailsFragment.FRAGMENT_TAG)
    }

}
