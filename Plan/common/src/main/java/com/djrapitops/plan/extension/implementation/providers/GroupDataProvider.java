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
package com.djrapitops.plan.extension.implementation.providers;

import com.djrapitops.plan.extension.annotation.Conditional;
import com.djrapitops.plan.extension.annotation.GroupProvider;
import com.djrapitops.plan.extension.annotation.StringProvider;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.ProviderInformation;

import java.lang.reflect.Method;

/**
 * Represents a DataExtension API method annotated with {@link StringProvider} annotation.
 * <p>
 * Used to obtain data to place in the database.
 *
 * @author Rsl1122
 */
public class GroupDataProvider extends DataProvider<String[]> {

    private GroupDataProvider(ProviderInformation providerInformation, MethodWrapper<String[]> methodWrapper) {
        super(providerInformation, methodWrapper);
    }

    public static void placeToDataProviders(
            DataProviders dataProviders, Method method, GroupProvider annotation,
            Conditional condition, String tab, String pluginName
    ) {
        MethodWrapper<String[]> methodWrapper = new MethodWrapper<>(method, String[].class);
        Icon providerIcon = new Icon(annotation.iconFamily(), annotation.iconName(), annotation.groupColor());

        ProviderInformation providerInformation = new ProviderInformation(
                pluginName, method.getName(), annotation.text(), null, providerIcon, 0, true, tab, condition
        );

        dataProviders.put(new GroupDataProvider(providerInformation, methodWrapper));
    }
}