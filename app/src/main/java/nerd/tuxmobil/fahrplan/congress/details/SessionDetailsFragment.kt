package nerd.tuxmobil.fahrplan.congress.details

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
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
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.MainThread
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import io.noties.markwon.Markwon
import io.noties.markwon.linkify.LinkifyPlugin
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.MyApp
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmTimePickerFragment
import nerd.tuxmobil.fahrplan.congress.calendar.CalendarSharing
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.extensions.replaceFragment
import nerd.tuxmobil.fahrplan.congress.extensions.requireViewByIdCompat
import nerd.tuxmobil.fahrplan.congress.extensions.toSpanned
import nerd.tuxmobil.fahrplan.congress.extensions.withArguments
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.sharing.SessionSharer
import nerd.tuxmobil.fahrplan.congress.sidepane.OnSidePaneCloseListener
import nerd.tuxmobil.fahrplan.congress.utils.LinkMovementMethodCompat
import nerd.tuxmobil.fahrplan.congress.utils.ServerBackendType
import nerd.tuxmobil.fahrplan.congress.utils.TypefaceFactory

class SessionDetailsFragment : Fragment() {

    companion object {

        private const val LOG_TAG = "SessionDetailsFragment"
        const val FRAGMENT_TAG = "detail"
        const val SESSION_DETAILS_FRAGMENT_REQUEST_CODE = 546
        private const val SCHEDULE_FEEDBACK_URL = BuildConfig.SCHEDULE_FEEDBACK_URL
        private val SHOW_FEEDBACK_MENU_ITEM = !TextUtils.isEmpty(SCHEDULE_FEEDBACK_URL)

        @JvmStatic
        fun replaceAtBackStack(fragmentManager: FragmentManager, @IdRes containerViewId: Int, sidePane: Boolean) {
            val fragment = SessionDetailsFragment().withArguments(
                BundleKeys.SIDEPANE to sidePane
            )
            fragmentManager.popBackStack(FRAGMENT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            fragmentManager.replaceFragment(containerViewId, fragment, FRAGMENT_TAG, FRAGMENT_TAG)
        }

        fun replace(fragmentManager: FragmentManager, @IdRes containerViewId: Int) {
            val fragment = SessionDetailsFragment()
            fragmentManager.replaceFragment(containerViewId, fragment, FRAGMENT_TAG)
        }

    }

    private lateinit var appRepository: AppRepository
    private lateinit var alarmServices: AlarmServices
    private val viewModel: SessionDetailsViewModel by viewModels { SessionDetailsViewModelFactory(appRepository, alarmServices) }
    private lateinit var model: SelectedSessionParameter
    private lateinit var markwon: Markwon
    private var sidePane = false
    private var hasArguments = false

    private val viewModelFunctionByMenuItemId = mapOf<Int, SessionDetailsViewModel.() -> Unit>(
        R.id.menu_item_feedback to { openFeedback() },
        R.id.menu_item_share_session to { share() },
        R.id.menu_item_share_session_text to { share() },
        R.id.menu_item_share_session_json to { shareToChaosflix() },
        R.id.menu_item_add_to_calendar to { addToCalendar() },
        R.id.menu_item_flag_as_favorite to { favorSession() },
        R.id.menu_item_unflag_as_favorite to { unfavorSession() },
        R.id.menu_item_set_alarm to { setAlarm() },
        R.id.menu_item_delete_alarm to { deleteAlarm() },
        R.id.menu_item_close_session_details to { closeDetails() },
        R.id.menu_item_navigate to { navigateToRoom() },
    )

    @MainThread
    @CallSuper
    override fun onAttach(@NonNull context: Context) {
        super.onAttach(context)
        appRepository = AppRepository
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
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        @LayoutRes val layout = if (sidePane) R.layout.detail_narrow else R.layout.detail
        return inflater.inflate(layout, container, false)
    }

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        if (args != null) {
            sidePane = args.getBoolean(BundleKeys.SIDEPANE, false)
            hasArguments = true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        val activity = requireActivity()
        if (hasArguments) {
            activity.invalidateOptionsMenu()
        }
        activity.setResult(Activity.RESULT_CANCELED)
    }

    private fun observeViewModel() {
        viewModel.selectedSessionParameter.observe(viewLifecycleOwner) { model ->
            this.model = model
            updateView()
            updateOptionsMenu()
        }
        viewModel.openFeedBack.observe(viewLifecycleOwner) { uri ->
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
        viewModel.shareSimple.observe(viewLifecycleOwner) { formattedSession ->
            SessionSharer.shareSimple(requireContext(), formattedSession)
        }
        viewModel.shareJson.observe(viewLifecycleOwner) { formattedSession ->
            val context = requireContext()
            if (!SessionSharer.shareJson(context, formattedSession)) {
                Toast.makeText(context, R.string.share_error_activity_not_found, Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.addToCalendar.observe(viewLifecycleOwner) { session ->
            CalendarSharing(requireContext()).addToCalendar(session)
        }
        viewModel.setAlarm.observe(viewLifecycleOwner) {
            AlarmTimePickerFragment.show(this, SESSION_DETAILS_FRAGMENT_REQUEST_CODE)
        }
        viewModel.navigateToRoom.observe(viewLifecycleOwner) { uri ->
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
        viewModel.closeDetails.observe(viewLifecycleOwner) {
            val activity = requireActivity()
            if (activity is OnSidePaneCloseListener) {
                (activity as OnSidePaneCloseListener).onSidePaneClose(FRAGMENT_TAG)
            }
        }
    }

    private fun updateView() {
        val view = requireView()
        val activity = requireActivity()
        val typefaceFactory = TypefaceFactory.getNewInstance(activity)

        // Detailbar
        var textView: TextView = view.requireViewByIdCompat(R.id.session_detailbar_date_time_view)
        textView.text = if (model.hasDateUtc) model.formattedZonedDateTime else ""

        textView = view.requireViewByIdCompat(R.id.session_detailbar_location_view)
        textView.text = model.roomName
        textView = view.requireViewByIdCompat(R.id.session_detailbar_session_id_view)
        textView.text = if (model.sessionId.isEmpty()) "" else textView.context.getString(R.string.session_details_session_id, model.sessionId)

        // Title
        textView = view.requireViewByIdCompat(R.id.session_details_content_title_view)
        var typeface = typefaceFactory.getTypeface(viewModel.titleFont)
        textView.applyText(typeface, model.title)

        // Subtitle
        textView = view.requireViewByIdCompat(R.id.session_details_content_subtitle_view)
        if (model.subtitle.isEmpty()) {
            textView.isVisible = false
        } else {
            typeface = typefaceFactory.getTypeface(viewModel.subtitleFont)
            textView.applyText(typeface, model.subtitle)
        }

        // Speakers
        textView = view.requireViewByIdCompat(R.id.session_details_content_speakers_view)
        if (model.speakerNames.isEmpty()) {
            textView.isVisible = false
        } else {
            typeface = typefaceFactory.getTypeface(viewModel.speakersFont)
            val speakerNamesContentDescription = Session.getSpeakersContentDescription(textView.context, model.speakersCount, model.speakerNames)
            textView.applyText(typeface, model.speakerNames, speakerNamesContentDescription)
        }

        // Abstract
        textView = view.requireViewByIdCompat(R.id.session_details_content_abstract_view)
        if (model.abstract.isEmpty()) {
            textView.isVisible = false
        } else {
            typeface = typefaceFactory.getTypeface(viewModel.abstractFont)
            if (ServerBackendType.PENTABARF.name == BuildConfig.SERVER_BACKEND_TYPE) {
                textView.applyHtml(typeface, model.formattedAbstract)
            } else {
                textView.applyMarkdown(typeface, model.abstract)
            }
        }

        // Description
        textView = view.requireViewByIdCompat(R.id.session_details_content_description_view)
        if (model.description.isEmpty()) {
            textView.isVisible = false
        } else {
            typeface = typefaceFactory.getTypeface(viewModel.descriptionFont)
            if (ServerBackendType.PENTABARF.name == BuildConfig.SERVER_BACKEND_TYPE) {
                textView.applyHtml(typeface, model.formattedDescription)
            } else {
                textView.applyMarkdown(typeface, model.description)
            }
        }

        // Links
        val linksView = view.requireViewByIdCompat<TextView>(R.id.session_details_content_links_section_view)
        textView = view.requireViewByIdCompat(R.id.session_details_content_links_view)
        if (model.hasLinks) {
            typeface = typefaceFactory.getTypeface(viewModel.linksSectionFont)
            linksView.typeface = typeface
            MyApp.LogDebug(LOG_TAG, "show links")
            linksView.isVisible = true
            typeface = typefaceFactory.getTypeface(viewModel.linksFont)
            textView.applyHtml(typeface, model.formattedLinks)
        } else {
            linksView.isVisible = false
            textView.isVisible = false
        }

        // Session online
        val sessionOnlineSectionView = view.requireViewByIdCompat<TextView>(R.id.session_details_content_session_online_section_view)
        typeface = typefaceFactory.getTypeface(viewModel.sessionOnlineSectionFont)
        sessionOnlineSectionView.typeface = typeface
        val sessionOnlineLinkView = view.requireViewByIdCompat<TextView>(R.id.session_details_content_session_online_view)
        val sessionLink = model.sessionLink
        if (model.hasWikiLinks || sessionLink.isEmpty()) {
            sessionOnlineSectionView.isVisible = false
            sessionOnlineLinkView.isVisible = false
        } else {
            sessionOnlineSectionView.isVisible = true
            sessionOnlineLinkView.isVisible = true
            typeface = typefaceFactory.getTypeface(viewModel.sessionOnlineFont)
            sessionOnlineLinkView.applyHtml(typeface, sessionLink)
        }
    }

    private fun TextView.applyText(typeface: Typeface, text: String, contentDescription: String? = null) {
        this.typeface = typeface
        this.text = text
        if (contentDescription != null) {
            this.contentDescription = contentDescription
        }
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

    private fun updateOptionsMenu() {
        requireActivity().invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (!::model.isInitialized) {
            // Skip if lifecycle is faster than ViewModel.
            return
        }
        inflater.inflate(R.menu.detailmenu, menu)
        if (model.isFlaggedAsFavorite) {
            menu.setMenuItemVisibility(R.id.menu_item_flag_as_favorite, false)
            menu.setMenuItemVisibility(R.id.menu_item_unflag_as_favorite, true)
        }
        if (model.hasAlarm) {
            menu.setMenuItemVisibility(R.id.menu_item_set_alarm, false)
            menu.setMenuItemVisibility(R.id.menu_item_delete_alarm, true)
        }
        menu.setMenuItemVisibility(R.id.menu_item_feedback, SHOW_FEEDBACK_MENU_ITEM && !model.isFeedbackUrlEmpty)
        if (sidePane) {
            menu.setMenuItemVisibility(R.id.menu_item_close_session_details, true)
        }
        menu.setMenuItemVisibility(R.id.menu_item_navigate, !model.isC3NavRoomNameEmpty)
        @Suppress("ConstantConditionIf")
        val item = if (BuildConfig.ENABLE_CHAOSFLIX_EXPORT) {
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
            val alarmTimesIndex = data.getIntExtra(AlarmTimePickerFragment.ALARM_PICKED_INTENT_KEY, 0)
            viewModel.addAlarm(alarmTimesIndex)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        return when (val function = viewModelFunctionByMenuItemId[itemId]) {
            null -> {
                return super.onOptionsItemSelected(item)
            }
            else -> {
                function.invoke(viewModel)
                true
            }
        }
    }

    private fun Menu.setMenuItemVisibility(itemId: Int, isVisible: Boolean) {
        findItem(itemId)?.let { it.isVisible = isVisible }
    }

}
