package nerd.tuxmobil.fahrplan.congress.base

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ContentView
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import nerd.tuxmobil.fahrplan.congress.utils.ActivityHelper.navigateUp

abstract class BaseActivity : AppCompatActivity {

    constructor() : super()

    @ContentView
    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> return navigateUp()
        }
        return super.onOptionsItemSelected(item)
    }

    protected fun addFragment(
        @IdRes containerViewId: Int,
        fragment: Fragment,
        fragmentTag: String
    ) {
        supportFragmentManager
            .beginTransaction()
            .add(containerViewId, fragment, fragmentTag)
            .commit()
    }

    @Suppress("SameParameterValue")
    protected fun replaceFragment(
        @IdRes containerViewId: Int,
        fragment: Fragment,
        fragmentTag: String
    ) {
        supportFragmentManager
            .beginTransaction()
            .replace(containerViewId, fragment, fragmentTag)
            .commit()
    }

    protected fun removeFragment(fragmentTag: String) {
        val fragment = supportFragmentManager.findFragmentByTag(fragmentTag)
        if (fragment != null) {
            supportFragmentManager
                .beginTransaction()
                .remove(fragment)
                .commit()
        }
    }

    protected fun findFragment(fragmentTag: String) =
        supportFragmentManager.findFragmentByTag(fragmentTag)

    /**
     * See [ActivityCompat.requireViewById].
     */
    fun <T : View?> requireViewByIdCompat(@IdRes id: Int): T {
        return ActivityCompat.requireViewById(this, id)
    }

}
