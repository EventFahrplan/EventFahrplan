package nerd.tuxmobil.fahrplan.congress.sharing;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import info.metadude.android.eventfahrplan.commons.temporal.DateFormatter;
import nerd.tuxmobil.fahrplan.congress.models.Session;
import nerd.tuxmobil.fahrplan.congress.utils.EventUrlComposer;
import nerd.tuxmobil.fahrplan.congress.wiki.WikiEventUtils;

public class SimpleLectureFormat {

    private static final String LINE_BREAK = "\n";
    private static final String COMMA = ",";
    private static final String SPACE = " ";
    private static final String HORIZONTAL_DIVIDERS = "---";


    @NonNull
    public static String format(@NonNull Session lecture) {
        StringBuilder builder = new StringBuilder();
        appendLecture(builder, lecture);
        return builder.toString();
    }

    @Nullable
    public static String format(@NonNull List<Session> lectures) {
        if (lectures.isEmpty()) {
            return null;
        }
        int lecturesSize = lectures.size();
        if (lecturesSize == 1) {
            return format(lectures.get(0));
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < lecturesSize; ++i) {
            Session lecture = lectures.get(i);
            appendLecture(builder, lecture);
            if (i < lecturesSize - 1) {
                appendDivider(builder);
            }
        }
        return builder.toString();
    }

    private static void appendLecture(StringBuilder builder, Session lecture) {
        long startTime = lecture.getStartTimeMilliseconds();
        String formattedTime = DateFormatter.newInstance().getFormattedDateTime(startTime);
        builder.append(lecture.title);
        builder.append(LINE_BREAK);
        builder.append(formattedTime);
        builder.append(COMMA);
        builder.append(SPACE);
        builder.append(lecture.room);
        if (!WikiEventUtils.containsWikiLink(lecture.getLinks())) {
            builder.append(LINE_BREAK);
            builder.append(LINE_BREAK);
            String eventUrl = new EventUrlComposer(lecture).getEventUrl();
            builder.append(eventUrl);
        }
    }

    private static void appendDivider(StringBuilder builder) {
        builder.append(LINE_BREAK);
        builder.append(LINE_BREAK);
        builder.append(HORIZONTAL_DIVIDERS);
        builder.append(LINE_BREAK);
        builder.append(LINE_BREAK);
    }

}
