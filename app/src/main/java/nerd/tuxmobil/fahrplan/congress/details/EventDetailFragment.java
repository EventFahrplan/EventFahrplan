package nerd.tuxmobil.fahrplan.congress.details;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.ligi.tracedroid.logging.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import nerd.tuxmobil.fahrplan.congress.BuildConfig;
import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmTimePickerFragment;
import nerd.tuxmobil.fahrplan.congress.calendar.CalendarSharing;
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys;
import nerd.tuxmobil.fahrplan.congress.models.Lecture;
import nerd.tuxmobil.fahrplan.congress.navigation.RoomForC3NavConverter;
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository;
import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment;
import nerd.tuxmobil.fahrplan.congress.sharing.LectureSharer;
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleLectureFormat;
import nerd.tuxmobil.fahrplan.congress.sidepane.OnSidePaneCloseListener;
import nerd.tuxmobil.fahrplan.congress.utils.EventUrlComposer;
import nerd.tuxmobil.fahrplan.congress.utils.FahrplanMisc;
import nerd.tuxmobil.fahrplan.congress.utils.FeedbackUrlComposer;
import nerd.tuxmobil.fahrplan.congress.utils.StringUtils;
import nerd.tuxmobil.fahrplan.congress.wiki.WikiEventUtils;


public class EventDetailFragment extends Fragment {

    private static final String LOG_TAG = "Detail";

    public static final String FRAGMENT_TAG = "detail";

    public static final int EVENT_DETAIL_FRAGMENT_REQUEST_CODE = 546;

    private static final String SCHEDULE_FEEDBACK_URL = BuildConfig.SCHEDULE_FEEDBACK_URL;

    private static final boolean SHOW_FEEDBACK_MENU_ITEM = !TextUtils.isEmpty(SCHEDULE_FEEDBACK_URL);

    private AppRepository appRepository;

    private String eventId;

    private String title;

    private Locale locale;

    private Typeface boldCondensed;

    private Typeface black;

    private Typeface light;

    private Typeface regular;

    private Typeface bold;

    private Lecture lecture;

    private int day;

    private String subtitle;

    private String spkr;

    private String abstractt;

    private String descr;

    private String links;

    private String room;

    private Boolean sidePane = false;

    private boolean requiresScheduleReload = false;

    private boolean hasArguments = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        appRepository = AppRepository.INSTANCE;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        MyApp.LogDebug(LOG_TAG, "onCreate");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (sidePane) {
            return inflater.inflate(R.layout.detail_narrow, container, false);
        } else {
            return inflater.inflate(R.layout.detail, container, false);
        }
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        day = args.getInt(BundleKeys.EVENT_DAY, 0);
        eventId = args.getString(BundleKeys.EVENT_ID);
        title = args.getString(BundleKeys.EVENT_TITLE);
        subtitle = args.getString(BundleKeys.EVENT_SUBTITLE);
        spkr = args.getString(BundleKeys.EVENT_SPEAKERS);
        abstractt = args.getString(BundleKeys.EVENT_ABSTRACT);
        descr = args.getString(BundleKeys.EVENT_DESCRIPTION);
        links = args.getString(BundleKeys.EVENT_LINKS);
        room = args.getString(BundleKeys.EVENT_ROOM);
        sidePane = args.getBoolean(BundleKeys.SIDEPANE, false);
        requiresScheduleReload = args.getBoolean(BundleKeys.REQUIRES_SCHEDULE_RELOAD, false);
        hasArguments = true;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Activity activity = requireActivity();
        if (hasArguments) {
            AssetManager assetManager = activity.getAssets();
            boldCondensed = Typeface.createFromAsset(assetManager, "Roboto-BoldCondensed.ttf");
            black = Typeface.createFromAsset(assetManager, "Roboto-Black.ttf");
            light = Typeface.createFromAsset(assetManager, "Roboto-Light.ttf");
            regular = Typeface.createFromAsset(assetManager, "Roboto-Regular.ttf");
            bold = Typeface.createFromAsset(assetManager, "Roboto-Bold.ttf");

            locale = getResources().getConfiguration().locale;

            // TODO: Remove this after 36C3. Right now it's only kept to minimize the likelihood of
            //  unintended behavior changes.
            FahrplanFragment.loadLectureList(appRepository, day, requiresScheduleReload);

            lecture = eventIdToLecture(eventId);

            // Detailbar

            TextView t;
            t = view.findViewById(R.id.lecture_detailbar_date_time);
            if (lecture != null && lecture.dateUTC > 0) {
                DateFormat df = SimpleDateFormat
                        .getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);
                t.setText(df.format(new Date(lecture.dateUTC)));
            } else {
                t.setText("");
            }

            t = view.findViewById(R.id.lecture_detailbar_location);
            if (t != null) {
                t.setText(room);
            } else {
                t.setText("");
            }

            t = view.findViewById(R.id.lecture_detailbar_lecture_id);
            if (t != null) {
                t.setText("ID: " + eventId);
            }

            // Title

            t = view.findViewById(R.id.event_detail_content_title_view);
            setUpTextView(t, boldCondensed, title);

            // Subtitle

            t = view.findViewById(R.id.event_detail_content_subtitle_view);
            if (TextUtils.isEmpty(subtitle)) {
                t.setVisibility(View.GONE);
            } else {
                setUpTextView(t, light, subtitle);
            }

            // Speakers

            t = view.findViewById(R.id.event_detail_content_speakers_view);
            if (TextUtils.isEmpty(spkr)) {
                t.setVisibility(View.GONE);
            } else {
                setUpTextView(t, black, spkr);
            }

            // Abstract

            t = view.findViewById(R.id.event_detail_content_abstract_view);
            if (TextUtils.isEmpty(abstractt)) {
                t.setVisibility(View.GONE);
            } else {
                abstractt = StringUtils.getHtmlLinkFromMarkdown(abstractt);
                setUpHtmlTextView(t, bold, abstractt);
            }

            // Description

            t = view.findViewById(R.id.event_detail_content_description_view);
            if (TextUtils.isEmpty(descr)) {
                t.setVisibility(View.GONE);
            } else {
                descr = StringUtils.getHtmlLinkFromMarkdown(descr);
                setUpHtmlTextView(t, regular, descr);
            }

            // Links

            TextView l = view.findViewById(R.id.event_detail_content_links_section_view);
            t = view.findViewById(R.id.event_detail_content_links_view);
            if (TextUtils.isEmpty(links)) {
                l.setVisibility(View.GONE);
                t.setVisibility(View.GONE);
            } else {
                l.setTypeface(bold);
                MyApp.LogDebug(LOG_TAG, "show links");
                l.setVisibility(View.VISIBLE);
                links = links.replaceAll("\\),", ")<br>");
                links = StringUtils.getHtmlLinkFromMarkdown(links);
                setUpHtmlTextView(t, regular, links);
            }

            // Event online

            final TextView eventOnlineSection = view.findViewById(R.id.event_detail_content_event_online_section_view);
            eventOnlineSection.setTypeface(bold);
            final TextView eventOnlineLink = view.findViewById(R.id.event_detail_content_event_online_view);
            if (WikiEventUtils.containsWikiLink(links)) {
                eventOnlineSection.setVisibility(View.GONE);
                eventOnlineLink.setVisibility(View.GONE);
            } else {
                String eventUrl = new EventUrlComposer(lecture).getEventUrl();
                if (eventUrl.isEmpty()) {
                    eventOnlineSection.setVisibility(View.GONE);
                    eventOnlineLink.setVisibility(View.GONE);
                } else {
                    eventOnlineSection.setVisibility(View.VISIBLE);
                    eventOnlineLink.setVisibility(View.VISIBLE);
                    String eventLink = "<a href=\"" + eventUrl + "\">" + eventUrl + "</a>";
                    setUpHtmlTextView(eventOnlineLink, regular, eventLink);
                }
            }

            activity.invalidateOptionsMenu();
        }
        activity.setResult(Activity.RESULT_CANCELED);
    }

    private void setUpTextView(@NonNull TextView textView,
                               @NonNull Typeface typeface,
                               @NonNull String text) {
        textView.setTypeface(typeface);
        textView.setText(text);
        textView.setVisibility(View.VISIBLE);
    }


    private void setUpHtmlTextView(@NonNull TextView textView,
                                   @NonNull Typeface typeface,
                                   @NonNull String text) {
        textView.setTypeface(typeface);
        textView.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE);
        textView.setLinkTextColor(ContextCompat.getColor(textView.getContext(), R.color.text_link_color));
        textView.setMovementMethod(new LinkMovementMethod());
        textView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.detailmenu, menu);
        MenuItem item;
        if (lecture != null) {
            if (lecture.highlight) {
                item = menu.findItem(R.id.menu_item_flag_as_favorite);
                if (item != null) {
                    item.setVisible(false);
                }
                item = menu.findItem(R.id.menu_item_unflag_as_favorite);
                if (item != null) {
                    item.setVisible(true);
                }
            }
            if (lecture.hasAlarm) {
                item = menu.findItem(R.id.menu_item_set_alarm);
                if (item != null) {
                    item.setVisible(false);
                }
                item = menu.findItem(R.id.menu_item_delete_alarm);
                if (item != null) {
                    item.setVisible(true);
                }
            }
        }
        item = menu.findItem(R.id.menu_item_feedback);
        String feedbackUrl = new FeedbackUrlComposer(lecture, SCHEDULE_FEEDBACK_URL).getFeedbackUrl();
        if (SHOW_FEEDBACK_MENU_ITEM && !TextUtils.isEmpty(feedbackUrl)) {
            if (item != null) {
                item.setVisible(true);
            }
        } else {
            if (item != null) {
                item.setVisible(false);
            }
        }
        if (sidePane) {
            item = menu.findItem(R.id.menu_item_close_event_details);
            if (item != null) {
                item.setVisible(true);
            }
        }
        item = menu.findItem(R.id.menu_item_navigate);
        if (item != null) {
            boolean isVisible = !getRoomConvertedForC3Nav().isEmpty();
            item.setVisible(isVisible);
        }
    }

    @NonNull
    private String getRoomConvertedForC3Nav() {
        String currentRoom = requireActivity().getIntent().getStringExtra(BundleKeys.EVENT_ROOM);
        if (currentRoom == null) {
            currentRoom = room;
        }
        return RoomForC3NavConverter.convert(currentRoom);
    }

    @NonNull
    private Lecture eventIdToLecture(String eventId) {
        return appRepository.readLectureByLectureId(eventId);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EVENT_DETAIL_FRAGMENT_REQUEST_CODE &&
                resultCode == AlarmTimePickerFragment.ALERT_TIME_PICKED_RESULT_CODE) {
            int alarmTimesIndex = data.getIntExtra(
                    AlarmTimePickerFragment.ALARM_PICKED_INTENT_KEY, 0);
            onAlarmTimesIndexPicked(alarmTimesIndex);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showAlarmTimePicker() {
        AlarmTimePickerFragment.show(this, EVENT_DETAIL_FRAGMENT_REQUEST_CODE);
    }

    private void onAlarmTimesIndexPicked(int alarmTimesIndex) {
        Activity activity = requireActivity();
        if (lecture != null) {
            FahrplanMisc.addAlarm(activity, appRepository, lecture, alarmTimesIndex);
        } else {
            Log.e(getClass().getName(), "onAlarmTimesIndexPicked: lecture: null. alarmTimesIndex: " + alarmTimesIndex);
        }
        refreshUI(activity);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Lecture l;
        Activity activity = requireActivity();
        switch (item.getItemId()) {
            case R.id.menu_item_feedback: {
                l = eventIdToLecture(eventId);
                String feedbackUrl = new FeedbackUrlComposer(l, SCHEDULE_FEEDBACK_URL).getFeedbackUrl();
                Uri uri = Uri.parse(feedbackUrl);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            }
            case R.id.menu_item_share_event:
                l = eventIdToLecture(eventId);
                String formattedLecture = SimpleLectureFormat.format(l);
                if (!LectureSharer.shareSimple(activity, formattedLecture)) {
                    Toast.makeText(activity, R.string.share_error_activity_not_found, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.menu_item_add_to_calendar:
                l = eventIdToLecture(eventId);
                CalendarSharing.addToCalendar(l, activity);
                return true;
            case R.id.menu_item_flag_as_favorite:
                if (lecture != null) {
                    lecture.highlight = true;
                    appRepository.updateHighlight(lecture);
                    appRepository.updateLecturesLegacy(lecture);
                }
                refreshUI(activity);
                return true;
            case R.id.menu_item_unflag_as_favorite:
                if (lecture != null) {
                    lecture.highlight = false;
                    appRepository.updateHighlight(lecture);
                    appRepository.updateLecturesLegacy(lecture);
                }
                refreshUI(activity);
                return true;
            case R.id.menu_item_set_alarm:
                showAlarmTimePicker();
                return true;
            case R.id.menu_item_delete_alarm:
                if (lecture != null) {
                    FahrplanMisc.deleteAlarm(activity, appRepository, lecture);
                }
                refreshUI(activity);
                return true;
            case R.id.menu_item_close_event_details:
                closeFragment(activity, FRAGMENT_TAG);
                return true;
            case R.id.menu_item_navigate:
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(BuildConfig.C3NAV_URL + getRoomConvertedForC3Nav()));
                startActivity(intent);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshUI(@NonNull Activity activity) {
        activity.invalidateOptionsMenu();
        activity.setResult(Activity.RESULT_OK);
        if (activity instanceof FahrplanFragment.OnRefreshEventMarkers) {
            ((FahrplanFragment.OnRefreshEventMarkers) activity).refreshEventMarkers();
        }
    }

    private void closeFragment(@NonNull Activity activity, @NonNull String fragmentTag) {
        if (activity instanceof OnSidePaneCloseListener) {
            ((OnSidePaneCloseListener) activity).onSidePaneClose(fragmentTag);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyApp.LogDebug(LOG_TAG, "onDestroy");
    }
}
