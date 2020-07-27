package nerd.tuxmobil.fahrplan.congress.sharing;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import info.metadude.android.eventfahrplan.commons.temporal.DateFormatter;
import nerd.tuxmobil.fahrplan.congress.models.Session;
import nerd.tuxmobil.fahrplan.congress.utils.SessionUrlComposer;
import nerd.tuxmobil.fahrplan.congress.wiki.WikiSessionUtils;

public class SimpleSessionFormat {

    private static final String LINE_BREAK = "\n";
    private static final String COMMA = ",";
    private static final String SPACE = " ";
    private static final String HORIZONTAL_DIVIDERS = "---";


    @NonNull
    public static String format(@NonNull Session session) {
        StringBuilder builder = new StringBuilder();
        appendSession(builder, session);
        return builder.toString();
    }

    @Nullable
    public static String format(@NonNull List<Session> sessions) {
        if (sessions.isEmpty()) {
            return null;
        }
        int sessionsSize = sessions.size();
        if (sessionsSize == 1) {
            return format(sessions.get(0));
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < sessionsSize; ++i) {
            Session session = sessions.get(i);
            appendSession(builder, session);
            if (i < sessionsSize - 1) {
                appendDivider(builder);
            }
        }
        return builder.toString();
    }

    private static void appendSession(StringBuilder builder, Session session) {
        long startTime = session.getStartTimeMilliseconds();
        String shareableStartTime = DateFormatter.newInstance().getFormattedShareable(startTime);
        builder.append(session.title);
        builder.append(LINE_BREAK);
        builder.append(shareableStartTime);
        builder.append(COMMA);
        builder.append(SPACE);
        builder.append(session.room);
        if (!WikiSessionUtils.containsWikiLink(session.getLinks())) {
            builder.append(LINE_BREAK);
            builder.append(LINE_BREAK);
            String sessionUrl = new SessionUrlComposer(session).getSessionUrl();
            builder.append(sessionUrl);
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
