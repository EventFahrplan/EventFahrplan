package nerd.tuxmobil.fahrplan.congress;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import nerd.tuxmobil.fahrplan.congress.R;

public class ChangeListActivity extends AppCompatActivity implements
        AbstractListFragment.OnLectureListClick {

    private static final String LOG_TAG = "ChangeListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_list);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorActionBar)));

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ChangeListFragment())
                    .commit();
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
