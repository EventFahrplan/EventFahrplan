package nerd.tuxmobil.fahrplan.congress;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;

import android.widget.ListAdapter;

/**
 * + * Taken from http://stackoverflow
 * .com/questions/18403647/actionbaractivity-of-android-support-v7-appcompat-and
 * -listactivity-in-same-act
 * +
 */
public abstract class ActionBarListActivity extends AppCompatActivity {

    private ListView mListView;

    protected ListView getListView() {
        if (mListView == null) {
            mListView = (ListView) findViewById(android.R.id.list);
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
            mListView = (ListView) findViewById(android.R.id.list);
        }
        View emptyView = findViewById(android.R.id.empty);
        if (emptyView != null) {
            mListView.setEmptyView(emptyView);
        }
    }
}