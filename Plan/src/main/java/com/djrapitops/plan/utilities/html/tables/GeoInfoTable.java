/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.utilities.FormatUtils;

import java.util.List;

/**
 * Utility Class for creating IP Table for inspect page.
 *
 * @author Rsl1122
 */
public class GeoInfoTable extends TableContainer {

    public GeoInfoTable(List<GeoInfo> geoInfo) {
        super("IP", "Geolocation", "Last Used");

        if (geoInfo.isEmpty()) {
            addRow("No Connections");
        } else {
            addValues(geoInfo);
        }
    }

    private void addValues(List<GeoInfo> geoInfo) {
        boolean displayIP = Settings.DISPLAY_PLAYER_IPS.isTrue();
        for (GeoInfo info : geoInfo) {
            long date = info.getLastUsed();
            addRow(
                    displayIP ? info.getIp() : "Hidden (Config)",
                    info.getGeolocation(),
                    date != 0 ? FormatUtils.formatTimeStampYear(date) : "-"
            );
        }
    }
}
