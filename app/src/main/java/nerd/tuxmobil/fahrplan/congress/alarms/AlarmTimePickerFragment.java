package nerd.tuxmobil.fahrplan.congress.alarms;

import static nerd.tuxmobil.fahrplan.congress.extensions.ViewExtensions.requireViewByIdCompat;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import nerd.tuxmobil.fahrplan.congress.BuildConfig;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.extensions.Contexts;
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository;

public class AlarmTimePickerFragment extends DialogFragment {

    private static final String FRAGMENT_TAG =
            BuildConfig.APPLICATION_ID + ".ALARM_TIME_PICKER_FRAGMENT_TAG";

    public static final String ALARM_TIMES_INDEX_BUNDLE_KEY =
            BuildConfig.APPLICATION_ID + ".ALARM_TIMES_INDEX_BUNDLE_KEY";

    private static final String REQUEST_KEY_BUNDLE_KEY = "REQUEST_KEY_BUNDLE_KEY";

    protected Spinner spinner;

    protected int alarmTimeIndex;

    public static void show(
            @NonNull Fragment invokingFragment,
            @NonNull String requestKey,
            @NonNull FragmentResultListener resultListener
    ) {
        DialogFragment dialogFragment = new AlarmTimePickerFragment();
        FragmentManager fragmentManager = invokingFragment.getParentFragmentManager();
        fragmentManager.setFragmentResultListener(
                requestKey,
                invokingFragment.getViewLifecycleOwner(),
                resultListener
        );
        Bundle arguments = new Bundle();
        arguments.putString(REQUEST_KEY_BUNDLE_KEY, requestKey);
        dialogFragment.setArguments(arguments);
        dialogFragment.show(fragmentManager, FRAGMENT_TAG);
    }

    @MainThread
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
        spinner = requireViewByIdCompat(rootView, R.id.spinner);
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
        Bundle arguments = requireArguments();
        String requestKey = arguments.getString(REQUEST_KEY_BUNDLE_KEY);
        if (requestKey == null) {
            throw new NullPointerException("Request key must be passed via fragment arguments.");
        }
        Bundle result = new Bundle();
        result.putInt(ALARM_TIMES_INDEX_BUNDLE_KEY, alarmTimesIndex);
        getParentFragmentManager().setFragmentResult(requestKey, result);
    }

}
