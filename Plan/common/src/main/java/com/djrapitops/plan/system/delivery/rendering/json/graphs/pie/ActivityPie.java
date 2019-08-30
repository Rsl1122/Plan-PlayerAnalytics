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
package com.djrapitops.plan.system.delivery.rendering.json.graphs.pie;

import com.djrapitops.plan.system.delivery.domain.mutators.ActivityIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Pie about different Activity Groups defined by ActivityIndex.
 *
 * @author Rsl1122
 * @see ActivityIndex
 */
public class ActivityPie extends Pie {

    ActivityPie(Map<String, Integer> activityData, String[] colors) {
        super(turnToSlices(activityData, colors));
    }

    private static List<PieSlice> turnToSlices(Map<String, Integer> activityData, String[] colors) {
        int maxCol = colors.length;

        List<PieSlice> slices = new ArrayList<>();
        int i = 0;
        for (String group : ActivityIndex.getGroups()) {
            int num = activityData.getOrDefault(group, 0);

            slices.add(new PieSlice(group, num, colors[i % maxCol], false));
            i++;
        }

        return slices;
    }
}
