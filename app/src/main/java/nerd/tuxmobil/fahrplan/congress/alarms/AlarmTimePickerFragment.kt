package nerd.tuxmobil.fahrplan.congress.alarms

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.MainThread
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import kotlinx.collections.immutable.ImmutableMap
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.extensions.getLayoutInflater
import nerd.tuxmobil.fahrplan.congress.extensions.requireViewByIdCompat
import nerd.tuxmobil.fahrplan.congress.extensions.withArguments
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.settings.getAlarmTimeEntries

class AlarmTimePickerFragment : DialogFragment() {

    companion object {
        const val ALARM_TIME_BUNDLE_KEY = "${BuildConfig.APPLICATION_ID}.ALARM_TIME_BUNDLE_KEY"
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
    private lateinit var alarmTimeEntries: ImmutableMap<Int, String>

    @MainThread
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val defaultAlarmTime = AppRepository.readAlarmTime()
        alarmTimeEntries = getAlarmTimeEntries(context)

        @SuppressLint("InflateParams")
        val layout = context.getLayoutInflater().inflate(
            R.layout.reminder_dialog,
            null,
            false
        )
        // https://possiblemobile.com/2013/05/layout-inflation-as-intended/
        initializeSpinner(layout, defaultAlarmTime)
        return AlertDialog.Builder(context).apply {
            setView(layout)
            setTitle(R.string.choose_alarm_time)
            setPositiveButton(android.R.string.ok) { _, _ -> passBackAlarmTime() }
            setNegativeButton(android.R.string.cancel, null)
        }.create()
    }

    private fun initializeSpinner(rootView: View, alarmTime: Int) {
        val selectedItemPosition = alarmTimeEntries.keys.indexOf(alarmTime)
            .takeIf { it != -1 } ?: error("Unsupported alarmTime: $alarmTime")

        val arrayAdapter = ArrayAdapter(
            rootView.context,
            android.R.layout.simple_spinner_item,
            alarmTimeEntries.values.toList(),
        )
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner = rootView.requireViewByIdCompat<Spinner>(R.id.spinner).apply{
            adapter = arrayAdapter
            setSelection(selectedItemPosition)
        }
    }

    private fun passBackAlarmTime() {
        val selectedItemPosition = spinner.selectedItemPosition
        val alarmTimeValues = alarmTimeEntries.keys.toList()
        if (selectedItemPosition !in alarmTimeValues.indices) {
            return
        }

        val alarmTime = alarmTimeValues[selectedItemPosition]
        val requestKey = requireArguments().getString(REQUEST_KEY_BUNDLE_KEY)
            ?: throw MissingRequestKeyException()
        val result = bundleOf(
            ALARM_TIME_BUNDLE_KEY to alarmTime,
        )
        parentFragmentManager.setFragmentResult(requestKey, result)
    }

}

private class MissingRequestKeyException : NullPointerException(
    "Request key must be passed via fragment arguments."
)
