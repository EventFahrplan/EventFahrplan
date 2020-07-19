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
                                      @NonNull Session session) {
        Intent intent = new Intent(activity, SessionDetailsActivity.class);
        intent.putExtra(BundleKeys.SESSION_ID, session.sessionId);
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
            args.putString(BundleKeys.SESSION_ID, intent.getStringExtra(BundleKeys.SESSION_ID));
            sessionDetailsFragment.setArguments(args);
            replaceFragment(R.id.detail, sessionDetailsFragment,
                    SessionDetailsFragment.FRAGMENT_TAG);
        }
    }

}
