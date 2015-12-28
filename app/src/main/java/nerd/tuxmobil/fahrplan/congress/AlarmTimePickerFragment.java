package nerd.tuxmobil.fahrplan.congress;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class AlarmTimePickerFragment extends DialogFragment {

    public static final String FRAGMENT_TAG =
            BuildConfig.APPLICATION_ID + ".ALERT_TIME_PICKER_FRAGMENT_TAG";

    public static final String ALARM_PICKED_INTENT_KEY =
            BuildConfig.APPLICATION_ID + ".ALERT_TIME_PICKER_INTENT_KEY";

    public static final int ALERT_TIME_PICKED_RESULT_CODE = 120166;

    protected Spinner spinner;

    protected int alarmTimeIndex;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context activity = getActivity();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        int defaultAlarmTimeIndex = activity.getResources().getInteger(R.integer.default_alarm_time_index);
        alarmTimeIndex = prefs.getInt(BundleKeys.PREFS_ALARM_TIME_INDEX, defaultAlarmTimeIndex);
        LayoutInflater inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.reminder_dialog, null, false);
        // https://possiblemobile.com/2013/05/layout-inflation-as-intended/
        initializeSpinner(layout);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder
                .setView(layout)
                .setTitle(R.string.choose_alarm_time)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                passBackAlarmTimesIndex();
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null);
        return dialogBuilder.create();
    }

    private void initializeSpinner(@NonNull View rootView) {
        spinner = (Spinner) rootView.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(),
                R.array.alarm_time_titles,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(alarmTimeIndex);
    }

    private void passBackAlarmTimesIndex() {
        int alarmTimesIndex = spinner.getSelectedItemPosition();
        Intent intent = new Intent();
        intent.putExtra(ALARM_PICKED_INTENT_KEY, alarmTimesIndex);
        Fragment fragment = getTargetFragment();
        if (fragment == null) {
            throw new NullPointerException("Target fragment is null.");
        }
        fragment.onActivityResult(getTargetRequestCode(), ALERT_TIME_PICKED_RESULT_CODE, intent);
    }

}
