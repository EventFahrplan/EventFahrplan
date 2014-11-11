package nerd.tuxmobil.fahrplan.congress;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import nerd.tuxmobil.fahrplan.congress.FahrplanContract.FragmentTags;

public class EventDetail extends SherlockFragmentActivity {

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.detail_frame);
        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }

        if (findViewById(R.id.detail) != null) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            EventDetailFragment ev = new EventDetailFragment();
            Bundle args = new Bundle();
            args.putString(BundleKeys.EVENT_TITLE, intent.getStringExtra(BundleKeys.EVENT_TITLE));
            args.putString(BundleKeys.EVENT_SUBTITLE,
                    intent.getStringExtra(BundleKeys.EVENT_SUBTITLE));
            args.putString(BundleKeys.EVENT_ABSTRACT,
                    intent.getStringExtra(BundleKeys.EVENT_ABSTRACT));
            args.putString(BundleKeys.EVENT_DESCRIPTION,
                    intent.getStringExtra(BundleKeys.EVENT_DESCRIPTION));
            args.putString(BundleKeys.EVENT_SPEAKERS,
                    intent.getStringExtra(BundleKeys.EVENT_SPEAKERS));
            args.putString(BundleKeys.EVENT_LINKS, intent.getStringExtra(BundleKeys.EVENT_LINKS));
            args.putString(BundleKeys.EVENT_ID, intent.getStringExtra(BundleKeys.EVENT_ID));
            args.putInt(BundleKeys.EVENT_TIME, intent.getIntExtra(BundleKeys.EVENT_TIME, 0));
            args.putInt(BundleKeys.EVENT_DAY, intent.getIntExtra(BundleKeys.EVENT_DAY, 0));
            args.putString(BundleKeys.EVENT_ROOM, intent.getStringExtra(BundleKeys.EVENT_ROOM));
            ev.setArguments(args);
            fragmentTransaction.replace(R.id.detail, ev, FragmentTags.DETAIL);
            fragmentTransaction.commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return ActivityHelper.navigateUp(this);
        }
        return super.onOptionsItemSelected(item);
    }
}
