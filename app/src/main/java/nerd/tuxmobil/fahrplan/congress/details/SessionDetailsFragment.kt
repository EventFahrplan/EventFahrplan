package nerd.tuxmobil.fahrplan.congress.details

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.annotation.MainThread
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import io.noties.markwon.Markwon
import io.noties.markwon.linkify.LinkifyPlugin
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.MyApp
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmTimePickerFragment
import nerd.tuxmobil.fahrplan.congress.calendar.CalendarSharing
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.extensions.requireViewByIdCompat
import nerd.tuxmobil.fahrplan.congress.extensions.toSpanned
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.sharing.SessionSharer
import nerd.tuxmobil.fahrplan.congress.sidepane.OnSidePaneCloseListener
import nerd.tuxmobil.fahrplan.congress.utils.LinkMovementMethodCompat
import nerd.tuxmobil.fahrplan.congress.utils.ServerBackendType
import nerd.tuxmobil.fahrplan.congress.utils.TypefaceFactory

class SessionDetailsFragment : Fragment(), SessionDetailsViewModel.ViewActionHandler {

    companion object {

        private const val LOG_TAG = "Detail"
        const val FRAGMENT_TAG = "detail"
        const val SESSION_DETAILS_FRAGMENT_REQUEST_CODE = 546
        private const val SCHEDULE_FEEDBACK_URL = BuildConfig.SCHEDULE_FEEDBACK_URL
        private val SHOW_FEEDBACK_MENU_ITEM = !TextUtils.isEmpty(SCHEDULE_FEEDBACK_URL)

    }

    private lateinit var appRepository: AppRepository
    private lateinit var sessionId: String
    private lateinit var viewModel: SessionDetailsViewModel
    private lateinit var alarmServices: AlarmServices
    private lateinit var markwon: Markwon
    private var sidePane = false
    private var hasArguments = false

    @MainThread
    @CallSuper
    override fun onAttach(@NonNull context: Context) {
        super.onAttach(context)
        appRepository = AppRepository
        viewModel = SessionDetailsViewModel(appRepository, sessionId, this)
        alarmServices = AlarmServices.newInstance(context, appRepository)
        markwon = Markwon.builder(requireContext())
            .usePlugin(LinkifyPlugin.create())
            .build()
    }

    @MainThread
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        MyApp.LogDebug(LOG_TAG, "onCreate")
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        @LayoutRes val layout = if (sidePane) R.layout.detail_narrow else R.layout.detail
        return inflater.inflate(layout, container, false)
    }

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        requireNotNull(args)
        sessionId = args.getString(BundleKeys.SESSION_ID) ?: error("Missing 'sessionId' argument.")
        sidePane = args.getBoolean(BundleKeys.SIDEPANE, false)
        hasArguments = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity()
        if (hasArguments) {
            val typefaceFactory = TypefaceFactory.getNewInstance(activity)

            // Detailbar
            var textView: TextView = view.requireViewByIdCompat(R.id.session_detailbar_date_time_view)
            textView.text = if (viewModel.hasDateUtc) viewModel.formattedZonedDateTime else ""

            textView = view.requireViewByIdCompat(R.id.session_detailbar_location_view)
            textView.text = viewModel.roomName
            textView = view.requireViewByIdCompat(R.id.session_detailbar_session_id_view)
            textView.text = if (viewModel.isSessionIdEmpty) "" else getString(R.string.session_details_session_id, viewModel.sessionId)

            // Title
            textView = view.requireViewByIdCompat(R.id.session_details_content_title_view)
            var typeface = typefaceFactory.getTypeface(viewModel.titleFont)
            textView.applyText(typeface, viewModel.title)

            // Subtitle
            textView = view.requireViewByIdCompat(R.id.session_details_content_subtitle_view)
            if (viewModel.isSubtitleEmpty) {
                textView.isVisible = false
            } else {
                typeface = typefaceFactory.getTypeface(viewModel.subtitleFont)
                textView.applyText(typeface, viewModel.subtitle)
            }

            // Speakers
            textView = view.requireViewByIdCompat(R.id.session_details_content_speakers_view)
            if (viewModel.isSpeakersEmpty) {
                textView.isVisible = false
            } else {
                typeface = typefaceFactory.getTypeface(viewModel.speakersFont)
                textView.applyText(typeface, viewModel.speakers)
            }

            // Abstract
            textView = view.requireViewByIdCompat(R.id.session_details_content_abstract_view)
            if (viewModel.isAbstractEmpty) {
                textView.isVisible = false
            } else {
                typeface = typefaceFactory.getTypeface(viewModel.abstractFont)
                if (ServerBackendType.PENTABARF.name == BuildConfig.SERVER_BACKEND_TYPE) {
                    textView.applyHtml(typeface, viewModel.formattedAbstract)
                } else {
                    textView.applyMarkdown(typeface, viewModel.abstractt)
                }
            }

            // Description
            textView = view.requireViewByIdCompat(R.id.session_details_content_description_view)
            if (viewModel.isDescriptionEmpty) {
                textView.isVisible = false
            } else {
                typeface = typefaceFactory.getTypeface(viewModel.descriptionFont)
                if (ServerBackendType.PENTABARF.name == BuildConfig.SERVER_BACKEND_TYPE) {
                    textView.applyHtml(typeface, viewModel.formattedDescription)
                } else {
                    textView.applyMarkdown(typeface, viewModel.description)
                }
            }

            // Links
            val linksView = view.requireViewByIdCompat<TextView>(R.id.session_details_content_links_section_view)
            textView = view.requireViewByIdCompat(R.id.session_details_content_links_view)
            if (viewModel.isLinksEmpty) {
                linksView.isVisible = false
                textView.isVisible = false
            } else {
                typeface = typefaceFactory.getTypeface(viewModel.linksSectionFont)
                linksView.typeface = typeface
                MyApp.LogDebug(LOG_TAG, "show links")
                linksView.isVisible = true
                typeface = typefaceFactory.getTypeface(viewModel.linksFont)
                textView.applyHtml(typeface, viewModel.formattedLinks)
            }

            // Session online
            val sessionOnlineSectionView = view.requireViewByIdCompat<TextView>(R.id.session_details_content_session_online_section_view)
            typeface = typefaceFactory.getTypeface(viewModel.sessionOnlineSectionFont)
            sessionOnlineSectionView.typeface = typeface
            val sessionOnlineLinkView = view.requireViewByIdCompat<TextView>(R.id.session_details_content_session_online_view)
            if (viewModel.hasWikiLinks) {
                sessionOnlineSectionView.isVisible = false
                sessionOnlineLinkView.isVisible = false
            } else {
                val sessionLink = viewModel.sessionLink
                if (sessionLink.isEmpty()) {
                    sessionOnlineSectionView.isVisible = false
                    sessionOnlineLinkView.isVisible = false
                } else {
                    sessionOnlineSectionView.isVisible = true
                    sessionOnlineLinkView.isVisible = true
                    typeface = typefaceFactory.getTypeface(viewModel.sessionOnlineFont)
                    sessionOnlineLinkView.applyHtml(typeface, sessionLink)
                }
            }
            activity.invalidateOptionsMenu()
        }
        activity.setResult(Activity.RESULT_CANCELED)
    }

    private fun TextView.applyText(typeface: Typeface, text: String) {
        this.typeface = typeface
        this.text = text
        this.isVisible = true
    }

    private fun TextView.applyHtml(typeface: Typeface, text: String) {
        this.typeface = typeface
        this.setText(text.toSpanned(), TextView.BufferType.SPANNABLE)
        this.setLinkTextColor(ContextCompat.getColor(context, R.color.text_link_on_light))
        this.movementMethod = LinkMovementMethodCompat.getInstance()
        this.isVisible = true
    }

    private fun TextView.applyMarkdown(typeface: Typeface, markdown: String) {
        markwon.setMarkdown(this, markdown)
        this.typeface = typeface
        this.setLinkTextColor(ContextCompat.getColor(context, R.color.text_link_on_light))
        this.movementMethod = LinkMovementMethodCompat.getInstance()
        this.isVisible = true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.detailmenu, menu)
        if (viewModel.isFlaggedAsFavorite) {
            menu.findItem(R.id.menu_item_flag_as_favorite)?.let { it.isVisible = false }
            menu.findItem(R.id.menu_item_unflag_as_favorite)?.let { it.isVisible = true }
        }
        if (viewModel.hasAlarm()) {
            menu.findItem(R.id.menu_item_set_alarm)?.let { it.isVisible = false }
            menu.findItem(R.id.menu_item_delete_alarm)?.let { it.isVisible = true }
        }
        var item: MenuItem? = menu.findItem(R.id.menu_item_feedback)
        if (SHOW_FEEDBACK_MENU_ITEM && !viewModel.isFeedbackUrlEmpty) {
            item?.let { it.isVisible = true }
        } else {
            item?.let { it.isVisible = false }
        }
        if (sidePane) {
            menu.findItem(R.id.menu_item_close_session_details)?.let { it.isVisible = true }
        }
        menu.findItem(R.id.menu_item_navigate)?.let {
            it.isVisible = !viewModel.isC3NavRoomNameEmpty
        }
        @Suppress("ConstantConditionIf")
        item = if (BuildConfig.ENABLE_CHAOSFLIX_EXPORT) {
            menu.findItem(R.id.menu_item_share_session_menu)
        } else {
            menu.findItem(R.id.menu_item_share_session)
        }
        item?.let { it.isVisible = true }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SESSION_DETAILS_FRAGMENT_REQUEST_CODE &&
                resultCode == AlarmTimePickerFragment.ALERT_TIME_PICKED_RESULT_CODE &&
                data != null
        ) {
            val alarmTimesIndex = data.getIntExtra(
                    AlarmTimePickerFragment.ALARM_PICKED_INTENT_KEY, 0)
            onAlarmTimesIndexPicked(alarmTimesIndex)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun onAlarmTimesIndexPicked(alarmTimesIndex: Int) {
        val session = appRepository.readSessionBySessionId(sessionId)
        val activity = requireActivity()
        alarmServices.addSessionAlarm(session, alarmTimesIndex)
        // Update the ViewModel session because refreshUI refers to its state.
        viewModel.setHasAlarm(session.hasAlarm)
        refreshUI(activity)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (viewModel.onOptionsMenuItemSelected(item.itemId)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun refreshUI(activity: Activity) {
        activity.invalidateOptionsMenu()
        activity.setResult(Activity.RESULT_OK)
    }

    @MainThread
    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        MyApp.LogDebug(LOG_TAG, "onDestroy")
    }

    override fun openFeedback(uri: Uri) {
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    override fun shareAsPlainText(formattedSessions: String) {
        val context = requireContext()
        SessionSharer.shareSimple(context, formattedSessions)
    }

    override fun shareAsJson(formattedSessions: String) {
        val context = requireContext()
        if (!SessionSharer.shareJson(context, formattedSessions)) {
            Toast.makeText(context, R.string.share_error_activity_not_found, Toast.LENGTH_SHORT).show()
        }
    }

    override fun addToCalendar(session: Session) {
        CalendarSharing(requireContext()).addToCalendar(session)
    }

    override fun showAlarmTimePicker() {
        AlarmTimePickerFragment.show(this, SESSION_DETAILS_FRAGMENT_REQUEST_CODE)
    }

    override fun deleteAlarm(session: Session) {
        alarmServices.deleteSessionAlarm(session)
    }

    override fun closeDetails() {
        val activity: Activity = requireActivity()
        if (activity is OnSidePaneCloseListener) {
            (activity as OnSidePaneCloseListener).onSidePaneClose(FRAGMENT_TAG)
        }
    }

    override fun navigateToRoom(uri: Uri) {
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    override fun refreshUI() {
        refreshUI(requireActivity())
    }

}
