/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.update;

import com.djrapitops.plugin.api.utility.Version;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * Utility for loading version information from github.
 *
 * @author Rsl1122
 */
public class VersionInfoLoader {

    private static final String VERSION_TXT_URL =
            "https://raw.githubusercontent.com/Rsl1122/Plan-PlayerAnalytics/master/versions.txt";

    /**
     * Loads version information from github.
     *
     * @return List of VersionInfo, newest version first.
     * @throws IOException                    If site can not be accessed.
     * @throws java.net.MalformedURLException If VERSION_TXT_URL is not valid.
     */
    public static List<VersionInfo> load() throws IOException {
        URL url = new URL(VERSION_TXT_URL);

        List<VersionInfo> versionInfo = new ArrayList<>();

        try (Scanner websiteScanner = new Scanner(url.openStream())) {
            while (websiteScanner.hasNextLine()) {
                String line = websiteScanner.nextLine();
                if (!line.startsWith("REL") && !line.startsWith("DEV")) {
                    continue;
                }
                String[] parts = line.split("\\|");
                if (parts.length < 4) {
                    continue;
                }
                boolean release = parts[0].equals("REL");
                Version version = new Version(parts[1]);
                String downloadUrl = parts[2];
                String changeLogUrl = parts[3];

                versionInfo.add(new VersionInfo(release, version, downloadUrl, changeLogUrl));
            }
        }

        Collections.sort(versionInfo);
        return versionInfo;
    }

}