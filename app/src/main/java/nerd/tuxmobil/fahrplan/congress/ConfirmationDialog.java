package nerd.tuxmobil.fahrplan.congress;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialogCompat;

public class ConfirmationDialog extends DialogFragment {

    interface OnConfirmationDialogClicked {
        void onAccepted(int dlgId);

        void onDenied(int dlgId);
    }

    public static final String BUNDLE_DLG_TEXT = "ConfirmationDialog.DLG_TEXT";
    public static final String BUNDLE_DLG_TITLE = "ConfirmationDialog.DLG_TITLE";
    public static final String BUNDLE_DLG_ID = "ConfirmationDialog.DLG_ID";
    public static final String TAG = "ConfirmationDialog.FRAGMENT_TAG";
    private int dlgText;
    private int dlgTitle;
    private int dlgId;
    private OnConfirmationDialogClicked listener;

    public static ConfirmationDialog newInstance(int dlgTitle, int dlgText, int dlgId) {
        ConfirmationDialog dialog = new ConfirmationDialog();
        dialog.listener = null;
        Bundle args = new Bundle();
        args.putInt(BUNDLE_DLG_TEXT, dlgText);
        args.putInt(BUNDLE_DLG_TITLE, dlgTitle);
        args.putInt(BUNDLE_DLG_ID, dlgId);
        dialog.setArguments(args);
        dialog.setCancelable(false);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            dlgText = args.getInt(BUNDLE_DLG_TEXT);
            dlgTitle = args.getInt(BUNDLE_DLG_TITLE);
            dlgId = args.getInt(BUNDLE_DLG_ID);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialogCompat.Builder builder = new MaterialDialogCompat.Builder(getActivity())
                .setMessage(dlgText)
                .setPositiveButton(android.R.string.yes,

                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (listener != null) {
                                    listener.onAccepted(dlgId);
                                }
                            }
                        })
                .setNegativeButton(android.R.string.no,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (listener != null) {
                                    listener.onDenied(dlgId);
                                }
                            }
                        });

        if (dlgTitle != 0) builder.setTitle(dlgTitle);
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnConfirmationDialogClicked) {
            listener = (OnConfirmationDialogClicked) activity;
        }
    }
}

