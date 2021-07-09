/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.delivery.rendering.html;

import java.util.Arrays;
import java.util.Objects;

import static com.djrapitops.plan.delivery.rendering.html.Contributors.For.CODE;
import static com.djrapitops.plan.delivery.rendering.html.Contributors.For.LANG;

/**
 * Contains list of contributors to add to the about modal.
 *
 * @author AuroraLS3
 */
public class Contributors {

    private static final Contributor[] CONTRIBUTORS = new Contributor[]{
            new Contributor("aidn5", CODE),
            new Contributor("Antonok", CODE),
            new Contributor("Argetan", CODE),
            new Contributor("Aurelien", CODE, LANG),
            new Contributor("BrainStone", CODE),
            new Contributor("Catalina", LANG),
            new Contributor("Elguerrero", LANG),
            new Contributor("Combustible", CODE),
            new Contributor("Creeperface01", CODE),
            new Contributor("CyanTech", LANG),
            new Contributor("DarkPyves", CODE),
            new Contributor("DaveDevil", LANG),
            new Contributor("developStorm", CODE),
            new Contributor("enterih", LANG),
            new Contributor("Eyremba", LANG),
            new Contributor("f0rb1d (\u4f5b\u58c1\u706f)", LANG),
            new Contributor("Fur_xia", LANG),
            new Contributor("fuzzlemann", CODE, LANG),
            new Contributor("Guinness_Akihiko", LANG),
            new Contributor("hallo1142", LANG),
            new Contributor("itaquito", LANG),
            new Contributor("jyhsu2000", CODE),
            new Contributor("jvmuller", LANG),
            new Contributor("Malachiel", LANG),
            new Contributor("Miclebrick", CODE),
            new Contributor("Morsmorse", LANG),
            new Contributor("Nogapra", LANG),
            new Contributor("Sander0542", LANG),
            new Contributor("Saph1s", LANG),
            new Contributor("Shadowhackercz", LANG),
            new Contributor("shaokeyibb", LANG),
            new Contributor("skmedix", CODE),
            new Contributor("TDJisvan", LANG),
            new Contributor("Vankka", CODE),
            new Contributor("yukieji", LANG),
            new Contributor("qsefthuopq", LANG),
            new Contributor("Karlatemp", CODE, LANG),
            new Contributor("Mastory_Md5", LANG),
            new Contributor("FluxCapacitor2", CODE),
            new Contributor("galexrt", LANG),
            new Contributor("QuakyCZ", LANG),
            new Contributor("MrFriggo", LANG),
            new Contributor("vacoup", CODE),
            new Contributor("Kopo942", CODE),
            new Contributor("WolverStones", LANG),
            new Contributor("BruilsiozPro", LANG),
            new Contributor("AppleMacOS", CODE),
            new Contributor("10935336", LANG),
            new Contributor("EyuphanMandiraci", LANG),
            new Contributor("4drian3d", LANG),
            new Contributor("\u6d1b\u4f0a", LANG),
            new Contributor("portlek", CODE),
            new Contributor("mbax", CODE),
            new Contributor("KairuByte", CODE),
            new Contributor("rymiel", CODE)
    };

    private Contributors() {
        // Static method class
    }

    public static String generateContributorHtml() {
        int estimatedLength = CONTRIBUTORS.length * 40 + 50;
        StringBuilder html = new StringBuilder(estimatedLength);
        Arrays.stream(CONTRIBUTORS).sorted()
                .forEach(contributor -> contributor.appendHtml(html));
        return html.toString();
    }

    enum For {
        CODE("fa-code"),
        LANG("fa-language");

        private final String icon;

        For(String icon) {
            this.icon = icon;
        }

        String toHtml() {
            return " <i class=\"fa fa-fw " + icon + "\"></i>";
        }
    }

    private static class Contributor implements Comparable<Contributor> {
        final String name;
        final For[] contributed;

        Contributor(String name, For... contributed) {
            this.name = name;
            this.contributed = contributed;
        }

        public void appendHtml(StringBuilder html) {
            html.append("<li class=\"col-4\">")
                    .append(name);
            for (For contribution : contributed) {
                html.append(contribution.toHtml());
            }
            html.append("</li>");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Contributor that = (Contributor) o;
            return name.equals(that.name) &&
                    Arrays.equals(contributed, that.contributed);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(name);
            result = 31 * result + Arrays.hashCode(contributed);
            return result;
        }

        @Override
        public int compareTo(Contributor o) {
            return String.CASE_INSENSITIVE_ORDER.compare(this.name, o.name);
        }
    }

}
