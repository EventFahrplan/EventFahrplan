package nerd.tuxmobil.fahrplan.congress.utils;

import android.support.annotation.NonNull;

public abstract class StringUtils {

    // language=regex
    public static final String MARKDOWN_LINK_REGEX = "\\[(.*?)\\]\\(([^ \\)]+).*?\\)";

    public static final String HTML_LINK_TEMPLATE = "<a href=\"$2\">$1</a>";

    public static String getHtmlLinkFromMarkdown(@NonNull String markdown) {
        return markdown.replaceAll(MARKDOWN_LINK_REGEX, HTML_LINK_TEMPLATE);
    }

}
