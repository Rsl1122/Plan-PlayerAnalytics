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
package com.djrapitops.plan.system;

import com.djrapitops.plan.system.delivery.rendering.html.graphs.Graphs;
import com.djrapitops.plan.utilities.formatting.Formatters;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HtmlUtilities {

    private final Lazy<Formatters> formatters;
    private final Lazy<Graphs> graphs;

    @Inject
    public HtmlUtilities(
            Lazy<Formatters> formatters,
            Lazy<Graphs> graphs
    ) {
        this.formatters = formatters;
        this.graphs = graphs;
    }

    public Formatters getFormatters() {
        return formatters.get();
    }

    public Graphs getGraphs() {
        return graphs.get();
    }

}
