package nerd.tuxmobil.fahrplan.congress.favorites;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;

import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment;
import nerd.tuxmobil.fahrplan.congress.base.BaseActivity;
import nerd.tuxmobil.fahrplan.congress.details.EventDetail;
import nerd.tuxmobil.fahrplan.congress.models.Lecture;
import nerd.tuxmobil.fahrplan.congress.utils.ConfirmationDialog;

public class StarredListActivity extends BaseActivity implements
        AbstractListFragment.OnLectureListClick,
        ConfirmationDialog.OnConfirmationDialogClicked {

    private static final String LOG_TAG = "StarredListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        int actionBarColor = ContextCompat.getColor(this, R.color.colorActionBar);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(actionBarColor));

        if (savedInstanceState == null) {
            addFragment(R.id.container, new StarredListFragment(),
                    StarredListFragment.FRAGMENT_TAG);
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

    @Override
    public void onAccepted(int dlgId) {
        Fragment fragment = findFragment(StarredListFragment.FRAGMENT_TAG);
        if (fragment != null) {
            ((StarredListFragment) fragment).deleteAllFavorites();
        } else {
            MyApp.LogDebug(LOG_TAG, "StarredListFragment not found");
        }
    }

    @Override
    public void onDenied(int dlgId) {
    }
}
