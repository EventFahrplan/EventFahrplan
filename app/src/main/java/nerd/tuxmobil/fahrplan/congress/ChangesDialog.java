package nerd.tuxmobil.fahrplan.congress;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class ChangesDialog extends SherlockDialogFragment {

    private int changed;
    private int added;
    private int cancelled;
    private int marked_affected;
    private String version;

    public static ChangesDialog newInstance(String version, int changed, int added, int cancelled, int marked) {
        ChangesDialog dialog = new ChangesDialog();
        Bundle args = new Bundle();
        args.putInt(BundleKeys.CHANGES_DLG_NUM_CHANGED, changed);
        args.putInt(BundleKeys.CHANGES_DLG_NUM_NEW, added);
        args.putInt(BundleKeys.CHANGES_DLG_NUM_CANCELLED, cancelled);
        args.putInt(BundleKeys.CHANGES_DLG_NUM_MARKED, marked);
        args.putString(BundleKeys.CHANGES_DLG_VERSION, version);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.changes_dialog, container, false);
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
        setStyle(DialogFragment.STYLE_NO_TITLE, getTheme());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView changes1 = (TextView)view.findViewById(R.id.changes_dlg_text);
        changes1.setText(getString(R.string.changes_dlg_text1, version,
                getResources().getQuantityString(R.plurals.numberOfLectures, changed, changed),
                getResources().getQuantityString(R.plurals.being, added, added),
                getResources().getQuantityString(R.plurals.being, cancelled, cancelled)));

        TextView changes2 = (TextView)view.findViewById(R.id.changes_dlg_text2);
        changes2.setText(getString(R.string.changes_dlg_text2, marked_affected));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity());
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(BundleKeys.PREFS_CHANGES_SEEN, true);
        edit.commit();
    }

}
