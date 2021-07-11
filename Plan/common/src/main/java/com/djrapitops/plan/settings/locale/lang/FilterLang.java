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
package com.djrapitops.plan.settings.locale.lang;

public enum FilterLang implements Lang {
    OPERATORS("", "Operators"),
    NON_OPERATORS("", "Non operators"),
    BANNED("", "Banned"),
    NOT_BANNED("", "Not banned"),
    ;

    private final String key;
    private final String defaultValue;

    FilterLang(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getIdentifier() {
        return "HTML - " + name() + " (Filters)";
    }

    @Override
    public String getKey() { return key; }

    @Override
    public String getDefault() {
        return defaultValue;
    }

}
