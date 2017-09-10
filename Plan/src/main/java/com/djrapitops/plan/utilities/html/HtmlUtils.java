package main.java.com.djrapitops.plan.utilities.html;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.systems.webserver.WebServer;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

/**
 * @author Rsl1122
 */
public class HtmlUtils {

    /**
     * Constructor used to hide the public constructor
     */
    private HtmlUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * @param html
     * @param replaceMap
     * @return
     */
    public static String replacePlaceholders(String html, Map<String, Serializable> replaceMap) {
        StrSubstitutor sub = new StrSubstitutor(replaceMap);
        sub.setEnableSubstitutionInVariables(true);
        return sub.replace(html);
    }

    /**
     * @return
     */
    public static String getServerAnalysisUrlWithProtocol() {
        return getProtocol() + ":" + getServerAnalysisUrl();
    }

    /**
     * @return
     */
    public static String getServerAnalysisUrl() {
        String ip = getIP();
        return "//" + ip + "/server";
    }

    /**
     * Used to get the WebServer's IP with Port.
     *
     * @return For example 127.0.0.1:8804
     */
    public static String getIP() {
        int port = Settings.WEBSERVER_PORT.getNumber();
        String ip;
        if (Settings.SHOW_ALTERNATIVE_IP.isTrue()) {
            ip = Settings.ALTERNATIVE_IP.toString().replace("%port%", String.valueOf(port));
        } else {
            ip = Plan.getInstance().getVariable().getIp() + ":" + port;
        }
        return ip;
    }

    public static String getProtocol() {
        WebServer uiServer = Plan.getInstance().getWebServer();
        return uiServer.isEnabled() ? uiServer.getProtocol() : Settings.EXTERNAL_WEBSERVER_LINK_PROTOCOL.toString();
    }

    /**
     * @param playerName
     * @return
     */
    public static String getInspectUrlWithProtocol(String playerName) {
        return getProtocol() + ":" + getInspectUrl(playerName);
    }

    /**
     * @param playerName
     * @return
     * @deprecated Use getRelativeInspectUrl instead.
     */
    @Deprecated
    public static String getInspectUrl(String playerName) {
        String ip = getIP();
        return "//" + ip + "/player/" + playerName.replace(" ", "%20").replace(".", "%2E");
    }

    public static String getRelativeInspectUrl(String playerName) {
        return "../player/" + playerName;
    }

    public static String getRelativeInspectUrl(UUID uuid) {
        return getRelativeInspectUrl(Plan.getInstance().getDataCache().getName(uuid));
    }

    /**
     * @param string
     * @return
     */
    public static String removeXSS(String string) {
        return StringUtils.removeAll(string, "(<!--)|(-->)|(</?script>)");
    }

    /**
     * @param string
     * @return
     */
    public static String swapColorsToSpan(String string) {
        Html[] replacer = new Html[]{Html.COLOR_0, Html.COLOR_1, Html.COLOR_2, Html.COLOR_3,
                Html.COLOR_4, Html.COLOR_5, Html.COLOR_6, Html.COLOR_7, Html.COLOR_8, Html.COLOR_9,
                Html.COLOR_A, Html.COLOR_B, Html.COLOR_C, Html.COLOR_D, Html.COLOR_E, Html.COLOR_F};

        for (Html html : replacer) {
            string = string.replace("§" + Character.toLowerCase(html.name().charAt(6)), html.parse());
        }

        int spans = string.split("<span").length - 1;
        for (int i = 0; i < spans; i++) {
            string = Html.SPAN.parse(string);
        }

        return StringUtils.remove(string, "§r");
    }

    public static String separateWithQuotes(String... strings) {
        StringBuilder build = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            build.append("\"");
            build.append(strings[i]);
            build.append("\"");
            if (i < strings.length - 1) {
                build.append(", ");
            }
        }
        return build.toString();
    }
}
