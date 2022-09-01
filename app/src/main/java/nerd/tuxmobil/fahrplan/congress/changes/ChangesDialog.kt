package nerd.tuxmobil.fahrplan.congress.changes

import android.app.Dialog
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.extensions.requireViewByIdCompat
import nerd.tuxmobil.fahrplan.congress.extensions.withArguments
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository.updateScheduleChangesSeen
import nerd.tuxmobil.fahrplan.congress.schedule.MainActivity

class ChangesDialog : DialogFragment() {

    companion object {

        const val FRAGMENT_TAG = "ChangesDialog"

        fun newInstance(version: String, statistic: ChangeStatistic) = ChangesDialog()
            .withArguments(
                BundleKeys.CHANGES_DLG_NUM_CHANGED to statistic.getChangedSessionsCount(),
                BundleKeys.CHANGES_DLG_NUM_NEW to statistic.getNewSessionsCount(),
                BundleKeys.CHANGES_DLG_NUM_CANCELLED to statistic.getCanceledSessionsCount(),
                BundleKeys.CHANGES_DLG_NUM_MARKED to statistic.getChangedFavoritesCount(),
                BundleKeys.CHANGES_DLG_VERSION to version
            )
            .apply { isCancelable = false }
    }

    private var changed = 0
    private var added = 0
    private var cancelled = 0
    private var markedAffected = 0
    private var version = ""

    @MainThread
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(requireArguments()) {
            changed = getInt(BundleKeys.CHANGES_DLG_NUM_CHANGED)
            added = getInt(BundleKeys.CHANGES_DLG_NUM_NEW)
            cancelled = getInt(BundleKeys.CHANGES_DLG_NUM_CANCELLED)
            markedAffected = getInt(BundleKeys.CHANGES_DLG_NUM_MARKED)
            version = getString(BundleKeys.CHANGES_DLG_VERSION)!!
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val msgView = activity.layoutInflater.inflate(R.layout.changes_dialog, null)

        val builder = AlertDialog.Builder(activity)
            .setTitle(getString(R.string.schedule_changes_dialog_title))
            .setPositiveButton(R.string.schedule_changes_dialog_browse) { _, _ -> onBrowse() }
            .setNegativeButton(R.string.schedule_changes_dialog_later) { _, _ -> onLater() }

        val span = SpannableStringBuilder()
        span.append(getString(R.string.schedule_changes_dialog_updated_to_text) + version)

        val spanColor = ContextCompat.getColor(activity, R.color.schedule_changes_dialog_new_version_text)
        span.setSpan(ForegroundColorSpan(spanColor), span.length - version.length, span.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        span.append(getString(
            R.string.schedule_changes_dialog_changed_new_cancelled_text,
            resources.getQuantityString(
                R.plurals.schedule_changes_dialog_number_of_sessions,
                changed,
                changed
            ),
            resources.getQuantityString(R.plurals.schedule_changes_dialog_being, added, added),
            resources.getQuantityString(R.plurals.schedule_changes_dialog_phrase_new, added),
            resources.getQuantityString(
                R.plurals.schedule_changes_dialog_being,
                cancelled,
                cancelled
            ),
            resources.getQuantityString(
                R.plurals.schedule_changes_dialog_phrase_cancelled,
                cancelled
            )
        ))

        val changes1 = msgView.requireViewByIdCompat<TextView>(R.id.schedule_changes_dialog_updated_to_text_view)
        changes1.text = span

        val changes2 = msgView.requireViewByIdCompat<TextView>(R.id.schedule_changes_dialog_changes_text_view)
        changes2.text = getString(R.string.schedule_changes_dialog_affected_text, markedAffected)

        builder.setView(msgView)
        return builder.create()
    }

    private fun onBrowse() {
        flagChangesAsSeen()
        val activity = requireActivity()
        if (activity is MainActivity) {
            activity.openSessionChanges()
        }
    }

    private fun onLater() {
        flagChangesAsSeen()
    }

    private fun flagChangesAsSeen() {
        updateScheduleChangesSeen(true)
    }

}
