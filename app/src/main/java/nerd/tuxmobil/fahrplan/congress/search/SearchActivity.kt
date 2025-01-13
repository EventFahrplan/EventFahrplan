package nerd.tuxmobil.fahrplan.congress.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment.OnSessionListClick
import nerd.tuxmobil.fahrplan.congress.base.BaseActivity
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsActivity
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository

class SearchActivity :
    BaseActivity(),
    OnSessionListClick {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SearchActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        if (savedInstanceState == null) {
            addFragment(R.id.container, SearchFragment(), SearchFragment.FRAGMENT_TAG)
        }
    }

    override fun onSessionListClick(guid: String) {
        if (AppRepository.updateSelectedGuid(guid)) {
            SessionDetailsActivity.start(this)
        }
    }

}
