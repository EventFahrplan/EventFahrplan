package nerd.tuxmobil.fahrplan.congress.changes;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;

import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment;
import nerd.tuxmobil.fahrplan.congress.base.BaseActivity;
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys;
import nerd.tuxmobil.fahrplan.congress.details.EventDetail;
import nerd.tuxmobil.fahrplan.congress.models.Lecture;

public class ChangeListActivity extends BaseActivity implements
        AbstractListFragment.OnLectureListClick {

    private static final String LOG_TAG = "ChangeListActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        int actionBarColor = ContextCompat.getColor(this, R.color.colorActionBar);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(actionBarColor));

        boolean requiresScheduleReload = false;
        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                requiresScheduleReload = extras.getBoolean(BundleKeys.REQUIRES_SCHEDULE_RELOAD, false);
            }
        }

        if (savedInstanceState == null) {
            ChangeListFragment fragment = ChangeListFragment.newInstance(false, requiresScheduleReload);
            addFragment(R.id.container, fragment, ChangeListFragment.FRAGMENT_TAG);
            MyApp.LogDebug(LOG_TAG, "onCreate fragment created");
        }
    }

    @Override
    public void onLectureListClick(Lecture lecture, boolean requiresScheduleReload) {
        if (lecture != null) {
            EventDetail.startForResult(this, lecture, lecture.day, requiresScheduleReload);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MyApp.EVENTVIEW && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
        }
    }
}
