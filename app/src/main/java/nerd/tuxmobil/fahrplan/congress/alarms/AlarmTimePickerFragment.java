package nerd.tuxmobil.fahrplan.congress.alarms;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import nerd.tuxmobil.fahrplan.congress.BuildConfig;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.extensions.Contexts;
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository;

public class AlarmTimePickerFragment extends DialogFragment {

    private static final String FRAGMENT_TAG =
            BuildConfig.APPLICATION_ID + ".ALERT_TIME_PICKER_FRAGMENT_TAG";

    public static final String ALARM_PICKED_INTENT_KEY =
            BuildConfig.APPLICATION_ID + ".ALERT_TIME_PICKER_INTENT_KEY";

    public static final int ALERT_TIME_PICKED_RESULT_CODE = 120166;

    protected Spinner spinner;

    protected int alarmTimeIndex;

    public static void show(@NonNull Fragment invokingFragment,
                            int requestCode) {
        DialogFragment dialogFragment = new AlarmTimePickerFragment();
        dialogFragment.setTargetFragment(invokingFragment, requestCode);
        FragmentActivity activity = invokingFragment.requireActivity();
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        dialogFragment.show(fragmentManager, FRAGMENT_TAG);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = requireContext();
        AppRepository appRepository = AppRepository.INSTANCE;
        alarmTimeIndex = appRepository.readAlarmTimeIndex();
        LayoutInflater inflater = Contexts.getLayoutInflater(context);
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.reminder_dialog, null, false);
        // https://possiblemobile.com/2013/05/layout-inflation-as-intended/
        initializeSpinner(layout);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder
                .setView(layout)
                .setTitle(R.string.choose_alarm_time)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> passBackAlarmTimesIndex())
                .setNegativeButton(android.R.string.cancel, null);
        return dialogBuilder.create();
    }

    private void initializeSpinner(@NonNull View rootView) {
        spinner = rootView.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                rootView.getContext(),
                R.array.preference_entries_alarm_time_titles,
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
