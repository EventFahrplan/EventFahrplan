import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import nerd.tuxmobil.fahrplan.congress.StringUtils;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class StringUtilsTests {

    @Test
    public void getHtmlLinkFromMarkdownWithSingleLinks() {
        String markdown = "[Chaos Computer Club](https://www.ccc.de)";
        String htmlLink = "<a href=\"https://www.ccc.de\">Chaos Computer Club</a>";
        assertThat(StringUtils.getHtmlLinkFromMarkdown(markdown)).isEqualTo(htmlLink);
    }

    @Test
    public void getHtmlLinkFromMarkdownWithMultipleLinks() {
        String markdown = "[Chaos Computer Club](https://www.ccc.de)<br>" +
                "[Bundestag](https://www.bundestag.de)";
        String htmlLink = "<a href=\"https://www.ccc.de\">Chaos Computer Club</a><br>" +
                "<a href=\"https://www.bundestag.de\">Bundestag</a>";
        assertThat(StringUtils.getHtmlLinkFromMarkdown(markdown)).isEqualTo(htmlLink);
    }

}
