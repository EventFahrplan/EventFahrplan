package nerd.tuxmobil.fahrplan.congress.alarms

import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.extensions.getLayoutInflater
import nerd.tuxmobil.fahrplan.congress.extensions.requireViewByIdCompat
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository

import android.os.Bundle
import android.app.Dialog
import android.view.View
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.annotation.SuppressLint

import androidx.annotation.MainThread
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import nerd.tuxmobil.fahrplan.congress.extensions.withArguments

import java.lang.NullPointerException

class AlarmTimePickerFragment : DialogFragment() {

    companion object {
        const val ALARM_TIMES_INDEX_BUNDLE_KEY = "${BuildConfig.APPLICATION_ID}.ALARM_TIMES_INDEX_BUNDLE_KEY"
        private const val FRAGMENT_TAG = "${BuildConfig.APPLICATION_ID}.ALARM_TIME_PICKER_FRAGMENT_TAG"
        private const val REQUEST_KEY_BUNDLE_KEY = "REQUEST_KEY_BUNDLE_KEY"

        fun show(
            invokingFragment: Fragment,
            requestKey: String,
            resultListener: FragmentResultListener
        ) {
            val fragmentManager = invokingFragment.parentFragmentManager
            fragmentManager.setFragmentResultListener(
                requestKey,
                invokingFragment.viewLifecycleOwner,
                resultListener
            )
            AlarmTimePickerFragment().withArguments(
                REQUEST_KEY_BUNDLE_KEY to requestKey
            ).show(fragmentManager, FRAGMENT_TAG)
        }
    }

    private lateinit var spinner: Spinner
    private var alarmTimeIndex = 0

    @MainThread
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        alarmTimeIndex = AppRepository.readAlarmTimeIndex()

        @SuppressLint("InflateParams")
        val layout = context.getLayoutInflater().inflate(
            R.layout.reminder_dialog,
            null,
            false
        )
        // https://possiblemobile.com/2013/05/layout-inflation-as-intended/
        initializeSpinner(layout)
        return AlertDialog.Builder(context).apply {
            setView(layout)
            setTitle(R.string.choose_alarm_time)
            setPositiveButton(android.R.string.ok) { _, _ -> passBackAlarmTimesIndex() }
            setNegativeButton(android.R.string.cancel, null)
        }.create()
    }

    private fun initializeSpinner(rootView: View) {
        val arrayAdapter = ArrayAdapter.createFromResource(
            rootView.context,
            R.array.preference_entries_alarm_time_titles,
            android.R.layout.simple_spinner_item
        )
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner = rootView.requireViewByIdCompat<Spinner>(R.id.spinner).apply{
            adapter = arrayAdapter
            setSelection(alarmTimeIndex)
        }
    }

    private fun passBackAlarmTimesIndex() {
        val alarmTimesIndex = spinner.selectedItemPosition
        val requestKey = requireArguments().getString(REQUEST_KEY_BUNDLE_KEY)
            ?: throw NullPointerException("Request key must be passed via fragment arguments.")
        val result = bundleOf(
            ALARM_TIMES_INDEX_BUNDLE_KEY to alarmTimesIndex
        )
        parentFragmentManager.setFragmentResult(requestKey, result)
    }

}
