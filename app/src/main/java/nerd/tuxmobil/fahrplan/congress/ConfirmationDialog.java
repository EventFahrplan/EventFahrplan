package nerd.tuxmobil.fahrplan.congress;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class ConfirmationDialog extends DialogFragment {

    interface OnConfirmationDialogClicked {
        void onAccepted(int dlgRequestCode);

        void onDenied(int dlgRequestCode);
    }

    public static final String BUNDLE_DLG_TITLE = "ConfirmationDialog.DLG_TITLE";
    public static final String BUNDLE_DLG_REQUEST_CODE = "ConfirmationDialog.DLG_REQUEST_CODE";
    public static final String TAG = "ConfirmationDialog.FRAGMENT_TAG";
    private int dlgTitle;
    private int dlgRequestCode;
    private OnConfirmationDialogClicked listener;

    public static ConfirmationDialog newInstance(@StringRes int dlgTitle, int requestCode) {
        ConfirmationDialog dialog = new ConfirmationDialog();
        dialog.listener = null;
        Bundle args = new Bundle();
        args.putInt(BUNDLE_DLG_TITLE, dlgTitle);
        args.putInt(BUNDLE_DLG_REQUEST_CODE, requestCode);
        dialog.setArguments(args);
        dialog.setCancelable(false);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            dlgTitle = args.getInt(BUNDLE_DLG_TITLE);
            dlgRequestCode = args.getInt(BUNDLE_DLG_REQUEST_CODE);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(dlgTitle)
                .setPositiveButton(R.string.dlg_delete_all_favorites_delete_all,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (listener != null) {
                                    listener.onAccepted(dlgRequestCode);
                                }
                            }
                        })
                .setNegativeButton(android.R.string.no,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (listener != null) {
                                    listener.onDenied(dlgRequestCode);
                                }
                            }
                        });
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

