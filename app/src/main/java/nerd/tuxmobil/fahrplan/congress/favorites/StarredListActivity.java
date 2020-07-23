package nerd.tuxmobil.fahrplan.congress.favorites;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment;
import nerd.tuxmobil.fahrplan.congress.base.BaseActivity;
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsActivity;
import nerd.tuxmobil.fahrplan.congress.models.Session;
import nerd.tuxmobil.fahrplan.congress.utils.ConfirmationDialog;

public class StarredListActivity extends BaseActivity implements
        AbstractListFragment.OnSessionListClick,
        ConfirmationDialog.OnConfirmationDialogClicked {

    private static final String LOG_TAG = "StarredListActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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
    public void onSessionListClick(Session session) {
        if (session != null) {
            SessionDetailsActivity.startForResult(this, session, session.day);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MyApp.SESSION_VIEW && resultCode == RESULT_OK) {
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
