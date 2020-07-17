package nerd.tuxmobil.fahrplan.congress.changes;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys;
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository;
import nerd.tuxmobil.fahrplan.congress.schedule.MainActivity;

public class ChangesDialog extends DialogFragment {

    public static final String FRAGMENT_TAG = "changesDialog";

    private int changed;
    private int added;
    private int cancelled;
    private int markedAffected;
    private String version;
    private boolean requiresScheduleReload = false;

    public static ChangesDialog newInstance(
            @NonNull String version,
            @NonNull ChangeStatistic statistic,
            boolean requiresScheduleReload) {
        ChangesDialog dialog = new ChangesDialog();
        Bundle args = new Bundle();
        args.putInt(BundleKeys.CHANGES_DLG_NUM_CHANGED, statistic.getChangedLecturesCount());
        args.putInt(BundleKeys.CHANGES_DLG_NUM_NEW, statistic.getNewLecturesCount());
        args.putInt(BundleKeys.CHANGES_DLG_NUM_CANCELLED, statistic.getCanceledLecturesCount());
        args.putInt(BundleKeys.CHANGES_DLG_NUM_MARKED, statistic.getChangedFavoritesCount());
        args.putString(BundleKeys.CHANGES_DLG_VERSION, version);
        args.putBoolean(BundleKeys.REQUIRES_SCHEDULE_RELOAD, requiresScheduleReload);
        dialog.setArguments(args);
        dialog.setCancelable(false);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            changed = args.getInt(BundleKeys.CHANGES_DLG_NUM_CHANGED);
            added = args.getInt(BundleKeys.CHANGES_DLG_NUM_NEW);
            cancelled = args.getInt(BundleKeys.CHANGES_DLG_NUM_CANCELLED);
            markedAffected = args.getInt(BundleKeys.CHANGES_DLG_NUM_MARKED);
            version = args.getString(BundleKeys.CHANGES_DLG_VERSION);
            requiresScheduleReload = args.getBoolean(BundleKeys.REQUIRES_SCHEDULE_RELOAD);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = requireActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(getString(R.string.schedule_changes_dialog_title))
                .setPositiveButton(R.string.schedule_changes_dialog_browse, (dialog, which) -> onBrowse())
                .setNegativeButton(R.string.schedule_changes_dialog_later, (dialog, which) -> onLater());

        LayoutInflater inflater = activity.getLayoutInflater();
        View msgView = inflater.inflate(R.layout.changes_dialog, null);
        TextView changes1 = msgView.findViewById(R.id.schedule_changes_dialog_updated_to_text_view);
        SpannableStringBuilder span = new SpannableStringBuilder();
        span.append(getString(R.string.schedule_changes_dialog_updated_to_text));
        int spanStart = span.length();
        span.append(version);
        Resources resources = getResources();
        int spanColor = ContextCompat.getColor(activity, R.color.schedule_changes_dialog_new_version_text);
        span.setSpan(new ForegroundColorSpan(spanColor),
                spanStart, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.append(getString(R.string.schedule_changes_dialog_changed_new_cancelled_text,
                resources.getQuantityString(R.plurals.schedule_changes_dialog_number_of_lectures, changed, changed),
                resources.getQuantityString(R.plurals.schedule_changes_dialog_being, added, added),
                resources.getQuantityString(R.plurals.schedule_changes_dialog_being, cancelled, cancelled)));
        changes1.setText(span);

        TextView changes2 = msgView.findViewById(R.id.schedule_changes_dialog_changes_text_view);
        changes2.setText(getString(R.string.schedule_changes_dialog_affected_text, markedAffected));
        builder.setView(msgView);
        return builder.create();
    }

    private void onBrowse() {
        flagChangesAsSeen();
        Activity activity = requireActivity();
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).openLectureChanges(requiresScheduleReload);
        }
    }

    private void onLater() {
        flagChangesAsSeen();
    }

    private void flagChangesAsSeen() {
        AppRepository.INSTANCE.updateScheduleChangesSeen(true);
    }

}
