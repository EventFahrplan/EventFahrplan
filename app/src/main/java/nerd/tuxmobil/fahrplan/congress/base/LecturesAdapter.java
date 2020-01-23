package nerd.tuxmobil.fahrplan.congress.base;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import info.metadude.android.eventfahrplan.commons.temporal.DateFormatter;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.extensions.Contexts;
import nerd.tuxmobil.fahrplan.congress.models.Lecture;

public abstract class LecturesAdapter extends ArrayAdapter<Lecture> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;
    private static final int NUM_VIEW_TYPES = 2;

    protected final Context context;

    private final List<Lecture> list;
    private final int numDays;
    private ArrayList<Integer> mMapper;
    private ArrayList<String> mSeparatorStrings;
    private TreeSet<Integer> mSeparatorsSet;

    public LecturesAdapter(Context context, @LayoutRes int layout, List<Lecture> list, int numDays) {
        super(context, layout, list);
        this.context = new ContextThemeWrapper(context, R.style.Theme_AppCompat_Light);
        this.list = list;
        this.numDays = numDays;
        initMapper();
    }

    @Override
    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View rowView = null;
        ViewHolder viewHolder = null;
        ViewHolderSeparator viewHolderSeparator = null;
        initViewSetup();

        int type = getItemViewType(position);

        if (convertView == null) {

            // clone the inflater using the ContextThemeWrapper
            LayoutInflater inflater = Contexts.getLayoutInflater(context);
            LayoutInflater localInflater = inflater.cloneInContext(context);

            switch (type) {
                case TYPE_ITEM:
                    rowView = localInflater.inflate(R.layout.lecture_list_item, parent, false);
                    viewHolder = new ViewHolder();

                    viewHolder.title = rowView.findViewById(R.id.lecture_list_item_title_view);
                    viewHolder.subtitle = rowView.findViewById(R.id.lecture_list_item_subtitle_view);
                    viewHolder.speakers = rowView.findViewById(R.id.lecture_list_item_speakers_view);
                    viewHolder.lang = rowView.findViewById(R.id.lecture_list_item_language_view);
                    viewHolder.day = rowView.findViewById(R.id.lecture_list_item_day_view);
                    viewHolder.time = rowView.findViewById(R.id.lecture_list_item_time_view);
                    viewHolder.room = rowView.findViewById(R.id.lecture_list_item_room_view);
                    viewHolder.duration = rowView.findViewById(R.id.lecture_list_item_duration_view);
                    viewHolder.video = rowView.findViewById(R.id.lecture_list_item_video_view);
                    viewHolder.noVideo = rowView.findViewById(R.id.lecture_list_item_no_video_view);
                    viewHolder.withoutVideoRecording = rowView.findViewById(R.id.lecture_list_item_without_video_recording_view);
                    rowView.setTag(viewHolder);
                    break;
                case TYPE_SEPARATOR:
                    rowView = localInflater.inflate(R.layout.lecture_list_separator, parent, false);
                    viewHolderSeparator = new ViewHolderSeparator();
                    viewHolderSeparator.text = rowView.findViewById(R.id.lecture_list_separator_title_view);
                    rowView.setTag(viewHolderSeparator);
                    break;
                default:
                    throw new IllegalStateException("Unknown type: " + type);
            }

        } else {
            rowView = convertView;
            switch (type) {
                case TYPE_ITEM:
                    viewHolder = (ViewHolder) rowView.getTag();
                    break;
                case TYPE_SEPARATOR:
                    viewHolderSeparator = (ViewHolderSeparator) rowView.getTag();
                    break;
                default:
                    throw new IllegalStateException("Unknown type: " + type);
            }
        }

        switch (type) {
            case TYPE_ITEM:
                setItemContent(position, viewHolder);
                break;
            case TYPE_SEPARATOR:
                setSeparatorContent(position, viewHolderSeparator);
                break;
            default:
                throw new IllegalStateException("Unknown type: " + type);
        }

        return rowView;
    }

    protected Lecture getLecture(int position) {
        return list.get(getItemIndex(position));
    }

    protected void resetItemStyles(ViewHolder viewHolder) {
        resetTextStyle(viewHolder.title, R.style.ScheduleListPrimary);
        resetTextStyle(viewHolder.subtitle, R.style.ScheduleListSecondary);
        resetTextStyle(viewHolder.speakers, R.style.ScheduleListSecondary);
        resetTextStyle(viewHolder.lang, R.style.ScheduleListSecondary);
        resetTextStyle(viewHolder.day, R.style.ScheduleListSecondary);
        resetTextStyle(viewHolder.time, R.style.ScheduleListSecondary);
        resetTextStyle(viewHolder.room, R.style.ScheduleListSecondary);
        resetTextStyle(viewHolder.duration, R.style.ScheduleListSecondary);
        viewHolder.withoutVideoRecording.setImageResource(R.drawable.ic_without_video_recording);
    }

    protected void resetTextStyle(TextView textView, int style) {
        textView.setTextAppearance(context, style);
    }

    protected abstract void initViewSetup();

    protected abstract void setItemContent(int position, ViewHolder viewHolder);

    private void setSeparatorContent(int position, ViewHolderSeparator viewHolderSeparator) {
        viewHolderSeparator.text.setText(mSeparatorStrings.get(mMapper.get(position)));
    }

    @Override
    public int getViewTypeCount() {
        return NUM_VIEW_TYPES;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return !mSeparatorsSet.contains(position);
    }

    @Override
    public int getItemViewType(int position) {
        return mSeparatorsSet.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
    }

    @Override
    public int getCount() {
        int count = 0;
        if (list != null) {
            count += list.size();
        }
        if (mSeparatorsSet != null) {
            count += mSeparatorsSet.size();
        }
        return count;
    }

    private void initMapper() {
        mSeparatorsSet = new TreeSet<>();
        mSeparatorStrings = new ArrayList<>();
        mMapper = new ArrayList<>();
        int day;
        int lastDay = 0;
        int sepCount = 0;

        if (list == null) {
            return;
        }

        String daySeparator = context.getString(R.string.day_separator);

        for (int index = 0; index < list.size(); index++) {

            Lecture l = list.get(index);
            day = l.day;
            if (day != lastDay) {
                lastDay = day;
                if (numDays > 1) {
                    String formattedDate = DateFormatter.Companion.getFormattedDate(l.dateUTC);
                    String dayDateSeparator = String.format(daySeparator, day, formattedDate);
                    mSeparatorStrings.add(dayDateSeparator);
                    mSeparatorsSet.add(index + sepCount);
                    mMapper.add(sepCount);
                    sepCount++;
                }
            }

            mMapper.add(index);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        initMapper();
    }

    public int getItemIndex(int position) {
        return mMapper.get(position);
    }

    public static class ViewHolder {
        public TextView title;
        public TextView subtitle;
        public TextView speakers;
        public TextView lang;
        public TextView day;
        public TextView time;
        public TextView room;
        public TextView duration;
        public ImageView noVideo;
        public ImageView video;
        public ImageView withoutVideoRecording;
    }

    static class ViewHolderSeparator {
        TextView text;
    }
}
