package nerd.tuxmobil.fahrplan.congress.utils

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle

import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.extensions.withArguments

class ConfirmationDialog : DialogFragment() {

    interface OnConfirmationDialogClicked {
        fun onAccepted(requestCode: Int)
    }

    companion object {
        const val FRAGMENT_TAG = "ConfirmationDialog.FRAGMENT_TAG"
        private const val BUNDLE_DLG_TITLE = "ConfirmationDialog.DLG_TITLE"
        private const val BUNDLE_DLG_REQUEST_CODE = "ConfirmationDialog.DLG_REQUEST_CODE"

        fun newInstance(@StringRes title: Int, requestCode: Int) =
            ConfirmationDialog().withArguments(
                BUNDLE_DLG_TITLE to title,
                BUNDLE_DLG_REQUEST_CODE to requestCode
            ).apply {
                listener = null
                isCancelable = false
            }
    }

    private var title = 0
    private var requestCode = 0
    private var listener: OnConfirmationDialogClicked? = null

    @MainThread
    @CallSuper
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnConfirmationDialogClicked) {
            listener = context
        }
    }

    @MainThread
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = requireArguments()
        title = args.getInt(BUNDLE_DLG_TITLE)
        requestCode = args.getInt(BUNDLE_DLG_REQUEST_CODE)
    }

    @MainThread
    override fun onCreateDialog(savedInstanceState: Bundle?) = AlertDialog
        .Builder(requireContext())
        .setTitle(title)
        .setPositiveButton(R.string.dlg_delete_all_favorites_delete_all) { _, _ ->
            listener?.onAccepted(requestCode)
        }
        .setNegativeButton(android.R.string.cancel) { _, _ ->
            // Do nothing.
        }
        .create()
}
