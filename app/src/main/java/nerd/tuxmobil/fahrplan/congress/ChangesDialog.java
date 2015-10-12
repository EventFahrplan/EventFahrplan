package nerd.tuxmobil.fahrplan.congress;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ChangesDialog extends DialogFragment {

    private int changed;
    private int added;
    private int cancelled;
    private int marked_affected;
    private String version;

    public static ChangesDialog newInstance(String version, int changed, int added,
                                            int cancelled, int marked) {
        ChangesDialog dialog = new ChangesDialog();
        Bundle args = new Bundle();
        args.putInt(BundleKeys.CHANGES_DLG_NUM_CHANGED, changed);
        args.putInt(BundleKeys.CHANGES_DLG_NUM_NEW, added);
        args.putInt(BundleKeys.CHANGES_DLG_NUM_CANCELLED, cancelled);
        args.putInt(BundleKeys.CHANGES_DLG_NUM_MARKED, marked);
        args.putString(BundleKeys.CHANGES_DLG_VERSION, version);
        dialog.setArguments(args);
        dialog.setCancelable(false);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            changed = args.getInt(BundleKeys.CHANGES_DLG_NUM_CHANGED);
            added = args.getInt(BundleKeys.CHANGES_DLG_NUM_NEW);
            cancelled = args.getInt(BundleKeys.CHANGES_DLG_NUM_CANCELLED);
            marked_affected = args.getInt(BundleKeys.CHANGES_DLG_NUM_MARKED);
            version = args.getString(BundleKeys.CHANGES_DLG_VERSION);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.schedule_udpate))
                .setPositiveButton(R.string.btn_dlg_browse,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                onBrowse();
                            }
                        })
                .setNegativeButton(R.string.btn_dlg_later,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onLater();
                            }
                        });

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View msgView = inflater.inflate(R.layout.changes_dialog, null);
        TextView changes1 = (TextView) msgView.findViewById(R.id.changes_dlg_text);
        SpannableStringBuilder span = new SpannableStringBuilder();
        span.append(getString(R.string.changes_dlg_text1));
        span.append(" ");
        int spanStart = span.length();
        span.append(version);
        Resources resources = getResources();
        span.setSpan(new ForegroundColorSpan(resources.getColor(R.color.colorAccent)),
                spanStart, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.append(getString(R.string.changes_dlg_text2, version,
                resources.getQuantityString(R.plurals.numberOfLectures, changed, changed),
                resources.getQuantityString(R.plurals.being, added, added),
                resources.getQuantityString(R.plurals.being, cancelled, cancelled)));
        changes1.setText(span);

        TextView changes2 = (TextView) msgView.findViewById(R.id.changes_dlg_text2);
        changes2.setText(getString(R.string.changes_dlg_text3, marked_affected));
        builder.setView(msgView);
        return builder.create();
    }

    private void onBrowse() {
        flagChangesAsSeen();
        FragmentActivity activity = getActivity();
        if (activity instanceof MainActivity) {
            ((MainActivity)activity).openLectureChanges();
        }
    }

    private void onLater() {
        flagChangesAsSeen();
    }

    private void flagChangesAsSeen() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(BundleKeys.PREFS_CHANGES_SEEN, true);
        edit.commit();
    }

}
