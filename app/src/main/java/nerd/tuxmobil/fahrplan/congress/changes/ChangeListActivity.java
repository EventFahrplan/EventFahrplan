package nerd.tuxmobil.fahrplan.congress.changes;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;

import nerd.tuxmobil.fahrplan.congress.AbstractListFragment;
import nerd.tuxmobil.fahrplan.congress.BaseActivity;
import nerd.tuxmobil.fahrplan.congress.Lecture;
import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.details.EventDetail;

public class ChangeListActivity extends BaseActivity implements
        AbstractListFragment.OnLectureListClick {

    private static final String LOG_TAG = "ChangeListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        int actionBarColor = ContextCompat.getColor(this, R.color.colorActionBar);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(actionBarColor));

        if (savedInstanceState == null) {
            addFragment(R.id.container, new ChangeListFragment(), ChangeListFragment.FRAGMENT_TAG);
            MyApp.LogDebug(LOG_TAG, "onCreate fragment created");
        }
    }

    @Override
    public void onLectureListClick(Lecture lecture) {
        if (lecture != null) {
            EventDetail.startForResult(this, lecture, lecture.day);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == MyApp.EVENTVIEW) && (resultCode == RESULT_OK)) {
            setResult(RESULT_OK);
        }
    }
}
