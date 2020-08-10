package nerd.tuxmobil.fahrplan.congress.net

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.NonNull
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.utils.AlertDialogHelper

/**
 * Displays the given certificate error message in a dialog.
 */
class CertificateErrorFragment : DialogFragment() {

    companion object {

        private const val FRAGMENT_TAG = "CERTIFICATE_ERROR_FRAGMENT_TAG"
        private const val BUNDLE_KEY_ERROR_MESSAGE = "BUNDLE_KEY_ERROR_MESSAGE"

        @JvmStatic
        fun showDialog(fragmentManager: FragmentManager, errorMessage: String) {
            val fragment = CertificateErrorFragment().apply {
                arguments = bundleOf(BUNDLE_KEY_ERROR_MESSAGE to errorMessage)
            }
            fragment.show(fragmentManager, FRAGMENT_TAG)
        }

    }

    @NonNull
    private lateinit var errorMessage: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            errorMessage = it.getString(BUNDLE_KEY_ERROR_MESSAGE)!!
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
            AlertDialogHelper.createErrorDialog(
                    requireContext(),
                    R.string.certificate_error_title,
                    R.string.certificate_error_message,
                    errorMessage
            )

}
