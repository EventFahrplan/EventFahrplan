package nerd.tuxmobil.fahrplan.congress.base;

import android.view.MenuItem;
import android.view.View;

import androidx.annotation.ContentView;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import nerd.tuxmobil.fahrplan.congress.utils.ActivityHelper;

public abstract class BaseActivity extends AppCompatActivity {

    public BaseActivity() {
        super();
    }

    @ContentView
    public BaseActivity(@LayoutRes int contentLayoutId) {
        super(contentLayoutId);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                return ActivityHelper.navigateUp(this);
        }
        return super.onOptionsItemSelected(item);
    }

    protected void addFragment(@IdRes int containerViewId,
                               @NonNull Fragment fragment,
                               @NonNull String fragmentTag) {
        getSupportFragmentManager()
                .beginTransaction()
                .add(containerViewId, fragment, fragmentTag)
                .commit();
    }

    protected void replaceFragment(@IdRes int containerViewId,
                                   @NonNull Fragment fragment,
                                   @NonNull String fragmentTag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(containerViewId, fragment, fragmentTag)
                .commit();
    }

    protected void removeFragment(@NonNull String fragmentTag) {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        Fragment fragment = supportFragmentManager.findFragmentByTag(fragmentTag);
        if (fragment != null) {
            supportFragmentManager
                    .beginTransaction()
                    .remove(fragment)
                    .commit();
        }
    }

    protected
    @Nullable
    Fragment findFragment(@NonNull String fragmentTag) {
        return getSupportFragmentManager().findFragmentByTag(fragmentTag);
    }

    /**
     * See {@link ActivityCompat#requireViewById}.
     */
    @NonNull
    protected <T extends View> T requireViewByIdCompat(@IdRes int id) {
        return ActivityCompat.requireViewById(this, id);
    }

}
