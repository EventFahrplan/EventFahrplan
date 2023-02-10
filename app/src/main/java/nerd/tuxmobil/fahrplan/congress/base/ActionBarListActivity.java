package nerd.tuxmobil.fahrplan.congress.base;

import android.view.View;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Taken from <a href="https://stackoverflow.com/q/18403647/356895">StackOverflow</a>
 */
public abstract class ActionBarListActivity extends BaseActivity {

    private ListView mListView;

    protected ListView getListView() {
        if (mListView == null) {
            mListView = findViewById(android.R.id.list);
        }
        return mListView;
    }

    protected void setListAdapter(ListAdapter adapter) {
        getListView().setAdapter(adapter);
    }

    protected ListAdapter getListAdapter() {
        ListAdapter adapter = getListView().getAdapter();
        if (adapter instanceof HeaderViewListAdapter) {
            return ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        } else {
            return adapter;
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        if (mListView == null) {
            mListView = findViewById(android.R.id.list);
        }
        View emptyView = findViewById(android.R.id.empty);
        if (emptyView != null) {
            mListView.setEmptyView(emptyView);
        }
    }
}