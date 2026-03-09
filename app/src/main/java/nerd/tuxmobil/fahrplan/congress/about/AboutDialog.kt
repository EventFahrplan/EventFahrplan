package nerd.tuxmobil.fahrplan.congress.about

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.compose.runtime.collectAsState
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import nerd.tuxmobil.fahrplan.congress.R

class AboutDialog : DialogFragment() {

    companion object {
        const val FRAGMENT_TAG = "AboutDialog"
    }

    private val viewModel by viewModels<AboutViewModel> { AboutViewModelFactory(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        AboutScreen(
            parameter = viewModel.aboutParameter.collectAsState().value,
            onViewEvent = viewModel::onViewEvent,
        )
    }.also { it.isClickable = true }

    override fun onStart() {
        super.onStart()
        val width = resources.getInteger(R.integer.about_percentage_width)
        dialog?.window?.setPercentageWidth(width)
    }

}

/**
 * Sets the width of the window to a percentage of the current screen width.
 * To be invoked when the hosting activity is created.
 */
private fun Window.setPercentageWidth(percentage: Int) {
    val metrics = Resources.getSystem().displayMetrics
    val width = (metrics.widthPixels * (percentage / 100f)).toInt()
    setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
}
