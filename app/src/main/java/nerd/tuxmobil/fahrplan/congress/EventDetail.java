package nerd.tuxmobil.fahrplan.congress;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import nerd.tuxmobil.fahrplan.congress.FahrplanContract.FragmentTags;

public class EventDetail extends AppCompatActivity {

    public static void startForResult(@NonNull Activity activity,
                                      @NonNull Lecture lecture,
                                      int lectureDay) {
        Intent intent = new Intent(activity, EventDetail.class);
        intent.putExtra(BundleKeys.EVENT_TITLE, lecture.title);
        intent.putExtra(BundleKeys.EVENT_SUBTITLE, lecture.subtitle);
        intent.putExtra(BundleKeys.EVENT_ABSTRACT, lecture.abstractt);
        intent.putExtra(BundleKeys.EVENT_DESCRIPTION, lecture.description);
        intent.putExtra(BundleKeys.EVENT_SPEAKERS, lecture.getFormattedSpeakers());
        intent.putExtra(BundleKeys.EVENT_LINKS, lecture.links);
        intent.putExtra(BundleKeys.EVENT_ID, lecture.lecture_id);
        intent.putExtra(BundleKeys.EVENT_TIME, lecture.startTime);
        intent.putExtra(BundleKeys.EVENT_DAY, lectureDay);
        intent.putExtra(BundleKeys.EVENT_ROOM, lecture.room);
        activity.startActivityForResult(intent, MyApp.EVENTVIEW);
    }

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorActionBar)));

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
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.item_nav).setVisible(getRoomConvertedForC3Nav() != null);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return ActivityHelper.navigateUp(this);

            case R.id.item_nav:
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://c3nav.de/?d=" + getRoomConvertedForC3Nav()));
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    private String getRoomConvertedForC3Nav() {
        final String currentRoom = getIntent().getStringExtra(BundleKeys.EVENT_ROOM);
        return RoomForC3NavConverter.convert(BuildConfig.VENUE, currentRoom);
    }
}
