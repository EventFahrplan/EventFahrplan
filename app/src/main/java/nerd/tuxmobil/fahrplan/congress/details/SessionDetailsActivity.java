package nerd.tuxmobil.fahrplan.congress.details;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.base.BaseActivity;
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys;
import nerd.tuxmobil.fahrplan.congress.models.Session;

public class SessionDetailsActivity extends BaseActivity {

    public static void startForResult(@NonNull Activity activity,
                                      @NonNull Session session,
                                      int sessionDay,
                                      boolean requiresScheduleReload) {
        Intent intent = new Intent(activity, SessionDetailsActivity.class);
        intent.putExtra(BundleKeys.SESSION_TITLE, session.title);
        intent.putExtra(BundleKeys.SESSION_SUBTITLE, session.subtitle);
        intent.putExtra(BundleKeys.SESSION_ABSTRACT, session.abstractt);
        intent.putExtra(BundleKeys.SESSION_DESCRIPTION, session.description);
        intent.putExtra(BundleKeys.SESSION_SPEAKERS, session.getFormattedSpeakers());
        intent.putExtra(BundleKeys.SESSION_LINKS, session.links);
        intent.putExtra(BundleKeys.SESSION_ID, session.sessionId);
        intent.putExtra(BundleKeys.SESSION_DAY, sessionDay);
        intent.putExtra(BundleKeys.SESSION_ROOM, session.room);
        intent.putExtra(BundleKeys.REQUIRES_SCHEDULE_RELOAD, requiresScheduleReload);
        activity.startActivityForResult(intent, MyApp.SESSION_VIEW);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    }

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        int actionBarColor = ContextCompat.getColor(this, R.color.colorActionBar);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(actionBarColor));

        setContentView(R.layout.detail_frame);
        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }

        if (intent != null && findViewById(R.id.detail) != null) {
            SessionDetailsFragment sessionDetailsFragment = new SessionDetailsFragment();
            Bundle args = new Bundle();
            args.putString(BundleKeys.SESSION_TITLE,
                    intent.getStringExtra(BundleKeys.SESSION_TITLE));
            args.putString(BundleKeys.SESSION_SUBTITLE,
                    intent.getStringExtra(BundleKeys.SESSION_SUBTITLE));
            args.putString(BundleKeys.SESSION_ABSTRACT,
                    intent.getStringExtra(BundleKeys.SESSION_ABSTRACT));
            args.putString(BundleKeys.SESSION_DESCRIPTION,
                    intent.getStringExtra(BundleKeys.SESSION_DESCRIPTION));
            args.putString(BundleKeys.SESSION_SPEAKERS,
                    intent.getStringExtra(BundleKeys.SESSION_SPEAKERS));
            args.putString(BundleKeys.SESSION_LINKS,
                    intent.getStringExtra(BundleKeys.SESSION_LINKS));
            args.putString(BundleKeys.SESSION_ID,
                    intent.getStringExtra(BundleKeys.SESSION_ID));
            args.putInt(BundleKeys.SESSION_DAY,
                    intent.getIntExtra(BundleKeys.SESSION_DAY, 0));
            args.putString(BundleKeys.SESSION_ROOM,
                    intent.getStringExtra(BundleKeys.SESSION_ROOM));
            args.putBoolean(BundleKeys.REQUIRES_SCHEDULE_RELOAD,
                    intent.getBooleanExtra(BundleKeys.REQUIRES_SCHEDULE_RELOAD, false));
            sessionDetailsFragment.setArguments(args);
            replaceFragment(R.id.detail, sessionDetailsFragment,
                    SessionDetailsFragment.FRAGMENT_TAG);
        }
    }

}
