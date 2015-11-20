package nerd.tuxmobil.fahrplan.congress;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

interface OnCloseDetailListener {

    public void closeDetailView();
}

public class EventDetailFragment extends Fragment {

    private final String LOG_TAG = "Detail";

    public static final int EVENT_DETAIL_FRAGMENT_REQUEST_CODE = 546;

    private String event_id;

    private String title;

    private static String feedbackURL = BuildConfig.SCHEDULE_FEEDBACK_URL;

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

    private boolean hasArguments = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        MyApp.LogDebug(LOG_TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
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
        event_id = args.getString(BundleKeys.EVENT_ID);
        title = args.getString(BundleKeys.EVENT_TITLE);
        subtitle = args.getString(BundleKeys.EVENT_SUBTITLE);
        spkr = args.getString(BundleKeys.EVENT_SPEAKERS);
        abstractt = args.getString(BundleKeys.EVENT_ABSTRACT);
        descr = args.getString(BundleKeys.EVENT_DESCRIPTION);
        links = args.getString(BundleKeys.EVENT_LINKS);
        room = args.getString(BundleKeys.EVENT_ROOM);
        sidePane = args.getBoolean(BundleKeys.SIDEPANE, false);
        hasArguments = true;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (hasArguments) {
            boldCondensed = Typeface
                    .createFromAsset(getActivity().getAssets(), "Roboto-BoldCondensed.ttf");
            black = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Black.ttf");
            light = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf");
            regular = Typeface
                    .createFromAsset(getActivity().getAssets(), "Roboto-Regular.ttf");
            bold = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Bold.ttf");

            locale = getResources().getConfiguration().locale;

            FahrplanFragment.loadLectureList(getActivity(), day, false);
            lecture = eventid2Lecture(event_id);

            TextView t;
            t = (TextView) view.findViewById(R.id.date);
            if ((lecture != null) && (lecture.dateUTC > 0)) {
                DateFormat df = SimpleDateFormat
                        .getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);
                t.setText(df.format(new Date(lecture.dateUTC)) + " - " + room);
            } else {
                t.setText("");
            }

            t = (TextView) view.findViewById(R.id.lectureid);
            if (t != null) {
                t.setText("ID: " + event_id);
            }

            t = (TextView) view.findViewById(R.id.title);
            t.setTypeface(boldCondensed);
            t.setText(title);

            t = (TextView) view.findViewById(R.id.subtitle);
            t.setText(subtitle);
            t.setTypeface(light);
            if (subtitle.length() == 0) {
                t.setVisibility(View.GONE);
            }

            t = (TextView) view.findViewById(R.id.speakers);
            t.setTypeface(black);
            t.setText(spkr);

            t = (TextView) view.findViewById(R.id.abstractt);
            t.setTypeface(bold);
            abstractt = abstractt
                    .replaceAll("\\[(.*?)\\]\\(([^ \\)]+).*?\\)", "<a href=\"$2\">$1</a>");
            t.setText(Html.fromHtml(abstractt), TextView.BufferType.SPANNABLE);
            t.setLinkTextColor(getResources().getColor(R.color.text_link_color));
            t.setMovementMethod(new LinkMovementMethod());

            t = (TextView) view.findViewById(R.id.description);
            t.setTypeface(regular);
            descr = descr.replaceAll("\\[(.*?)\\]\\(([^ \\)]+).*?\\)", "<a href=\"$2\">$1</a>");
            t.setText(Html.fromHtml(descr), TextView.BufferType.SPANNABLE);
            t.setLinkTextColor(getResources().getColor(R.color.text_link_color));
            t.setMovementMethod(new LinkMovementMethod());

            TextView l = (TextView) view.findViewById(R.id.linksSection);
            l.setTypeface(bold);
            t = (TextView) view.findViewById(R.id.links);
            t.setTypeface(regular);

            if (links.length() > 0) {
                MyApp.LogDebug(LOG_TAG, "show links");
                l.setVisibility(View.VISIBLE);
                t.setVisibility(View.VISIBLE);
                links = links.replaceAll("\\),", ")<br>");
                links = links.replaceAll("\\[(.*?)\\]\\(([^ \\)]+).*?\\)", "<a href=\"$2\">$1</a>");
                t.setText(Html.fromHtml(links), TextView.BufferType.SPANNABLE);
                t.setLinkTextColor(getResources().getColor(R.color.text_link_color));
                t.setMovementMethod(new LinkMovementMethod());
            } else {
                l.setVisibility(View.GONE);
                t.setVisibility(View.GONE);
            }

            final TextView eventOnlineSection = (TextView) view
                    .findViewById(R.id.eventOnlineSection);
            eventOnlineSection.setTypeface(bold);
            final TextView eventOnlineLink = (TextView) view.findViewById(R.id.eventOnline);
            eventOnlineLink.setTypeface(regular);
            final String eventUrl = FahrplanMisc.getEventUrl(getActivity(), event_id);
            final String eventLink = "<a href=\"" + eventUrl + "\">" + eventUrl + "</a>";
            eventOnlineLink.setText(Html.fromHtml(eventLink), TextView.BufferType.SPANNABLE);
            eventOnlineLink.setMovementMethod(new LinkMovementMethod());
            eventOnlineLink.setLinkTextColor(getResources().getColor(R.color.text_link_color));

            getActivity().supportInvalidateOptionsMenu();
        }
        getActivity().setResult(FragmentActivity.RESULT_CANCELED);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.detailmenu, menu);
        MenuItem item;
        if (Build.VERSION.SDK_INT < 14) {
            item = menu.findItem(R.id.item_add_to_calendar);
            if (item != null) {
                item.setVisible(false);
            }
        }
        if (lecture != null) {
            if (lecture.highlight) {
                item = menu.findItem(R.id.item_fav);
                if (item != null) {
                    item.setVisible(false);
                }
                item = menu.findItem(R.id.item_unfav);
                if (item != null) {
                    item.setVisible(true);
                }
            }
            if (lecture.has_alarm) {
                item = menu.findItem(R.id.item_set_alarm);
                if (item != null) {
                    item.setVisible(false);
                }
                item = menu.findItem(R.id.item_clear_alarm);
                if (item != null) {
                    item.setVisible(true);
                }
            }
        }
        if (sidePane) {
            item = menu.findItem(R.id.item_close);
            if (item != null) {
                item.setVisible(true);
            }
        }
    }

    private Lecture eventid2Lecture(String event_id) {
        if (MyApp.lectureList == null) {
            return null;
        }
        for (Lecture lecture : MyApp.lectureList) {
            if (lecture.lecture_id.equals(event_id)) {
                return lecture;
            }
        }
        return null;
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
        DialogFragment dialogFragment = new AlarmTimePickerFragment();
        dialogFragment.setTargetFragment(this, EVENT_DETAIL_FRAGMENT_REQUEST_CODE);
        dialogFragment.show(getActivity().getSupportFragmentManager(),
                AlarmTimePickerFragment.FRAGMENT_TAG);
    }

    private void onAlarmTimesIndexPicked(int alarmTimesIndex) {
        FahrplanMisc.addAlarm(getActivity(), lecture, alarmTimesIndex);
        getActivity().supportInvalidateOptionsMenu();
        getActivity().setResult(FragmentActivity.RESULT_OK);
        refreshEventMarkers();
    }

    public void refreshEventMarkers() {
        FragmentActivity activity = getActivity();
        if ((activity != null) && (activity instanceof OnRefreshEventMarkers)) {
            ((OnRefreshEventMarkers) activity).refreshEventMarkers();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Lecture l;
        switch (item.getItemId()) {
            case R.id.item_feedback:
                Uri uri = Uri.parse(String.format(feedbackURL, event_id));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            case R.id.item_share:
                l = eventid2Lecture(event_id);
                if (l != null) {
                    FahrplanMisc.share(getActivity(), l);
                }
                return true;
            case R.id.item_add_to_calendar:
                l = eventid2Lecture(event_id);
                if (l != null) {
                    FahrplanMisc.addToCalender(getActivity(), l);
                }
                return true;
            case R.id.item_fav:
                lecture.highlight = true;
                if (lecture != null) {
                    FahrplanMisc.writeHighlight(getActivity(), lecture);
                }
                getActivity().supportInvalidateOptionsMenu();
                getActivity().setResult(FragmentActivity.RESULT_OK);
                refreshEventMarkers();
                return true;
            case R.id.item_unfav:
                lecture.highlight = false;
                if (lecture != null) {
                    FahrplanMisc.writeHighlight(getActivity(), lecture);
                }
                getActivity().supportInvalidateOptionsMenu();
                getActivity().setResult(FragmentActivity.RESULT_OK);
                refreshEventMarkers();
                return true;
            case R.id.item_set_alarm:
                showAlarmTimePicker();
                return true;
            case R.id.item_clear_alarm:
                if (lecture != null) {
                    FahrplanMisc.deleteAlarm(getActivity(), lecture);
                }
                getActivity().supportInvalidateOptionsMenu();
                getActivity().setResult(FragmentActivity.RESULT_OK);
                refreshEventMarkers();
                return true;
            case R.id.item_close:
                FragmentActivity activity = getActivity();
                if ((activity != null) && (activity instanceof OnCloseDetailListener)) {
                    ((OnCloseDetailListener) activity).closeDetailView();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyApp.LogDebug(LOG_TAG, "onDestroy");
    }
}
