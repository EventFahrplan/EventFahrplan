package nerd.tuxmobil.fahrplan.congress;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

public class StarredListActivity extends ActionBarActivity implements AbstractListFragment
        .OnLectureListClick, ConfirmationDialog.OnConfirmationDialogClicked {

    private static final String LOG_TAG = "StarredListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_list);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new StarredListFragment(), FahrplanContract.FragmentTags.STARRED)
                    .commit();
            MyApp.LogDebug(LOG_TAG, "onCreate fragment created");
        }
    }

    @Override
    public void onLectureListClick(Lecture lecture) {
        if (lecture != null) {
            Intent intent = new Intent(this, EventDetail.class);
            intent.putExtra(BundleKeys.EVENT_TITLE, lecture.title);
            intent.putExtra(BundleKeys.EVENT_SUBTITLE, lecture.subtitle);
            intent.putExtra(BundleKeys.EVENT_ABSTRACT, lecture.abstractt);
            intent.putExtra(BundleKeys.EVENT_DESCRIPTION, lecture.description);
            intent.putExtra(BundleKeys.EVENT_SPEAKERS, lecture.speakers.replaceAll(";", ", "));
            intent.putExtra(BundleKeys.EVENT_LINKS, lecture.links);
            intent.putExtra(BundleKeys.EVENT_ID, lecture.lecture_id);
            intent.putExtra(BundleKeys.EVENT_TIME, lecture.startTime);
            intent.putExtra(BundleKeys.EVENT_DAY, lecture.day);
            intent.putExtra(BundleKeys.EVENT_ROOM, lecture.room);
            startActivityForResult(intent, MyApp.EVENTVIEW);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == MyApp.EVENTVIEW) && (resultCode == RESULT_OK)) {
            setResult(RESULT_OK);
        }
    }

    @Override
    public void onAccepted(int dlgId) {
        FragmentManager fm = getSupportFragmentManager();
        StarredListFragment fragment = (StarredListFragment)fm.findFragmentByTag(FahrplanContract
                .FragmentTags.STARRED);
        if (fragment != null) {
            fragment.deleteAllFavorites();
        } else {
            MyApp.LogDebug(LOG_TAG, "StarredListFragment not found");
        }
    }

    @Override
    public void onDenied(int dlgId) {
    }
}
