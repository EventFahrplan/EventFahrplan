package nerd.tuxmobil.fahrplan.congress.extensions

import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import nerd.tuxmobil.fahrplan.congress.R

fun AppCompatActivity.applyToolbar(
    toolbar: Toolbar,
    block: ActionBar.() -> Unit = {},
) {
    setSupportActionBar(toolbar)
    block(supportActionBar!!)
    val actionBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
    supportActionBar!!.setBackgroundDrawable(actionBarColor.toDrawable())
    toolbar.applyHorizontalInsets()
}
