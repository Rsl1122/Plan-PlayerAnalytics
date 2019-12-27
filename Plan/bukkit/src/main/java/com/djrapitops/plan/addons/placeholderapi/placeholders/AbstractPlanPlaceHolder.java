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
package com.djrapitops.plan.addons.placeholderapi.placeholders;

import com.djrapitops.plan.addons.placeholderapi.PlanPlaceHolders;
import com.djrapitops.plan.identification.ServerInfo;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * abstract class used for plan placeholders list. This class contains most used
 * methods (static methods and non-static one). It is also used as a universe
 * interface for the Plan-placeholders instances
 * {@link #onPlaceholderRequest(Player, String)}. Check {@link PlanPlaceHolders}
 * to learn more how it is used.
 *
 * @author aidn5
 * @see PlanPlaceHolders
 */
public abstract class AbstractPlanPlaceHolder {

    protected final ServerInfo serverInfo;

    AbstractPlanPlaceHolder(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    static long now() {
        return System.currentTimeMillis();
    }

    static long dayAgo() {
        return now() - TimeUnit.DAYS.toMillis(1L);
    }

    static long weekAgo() {
        return now() - (TimeUnit.DAYS.toMillis(7L));
    }

    static long monthAgo() {
        return now() - (TimeUnit.DAYS.toMillis(30L));
    }

    UUID serverUUID() {
        return serverInfo.getServerUUID();
    }

    /**
     * Look up the placeholder and check if it is registered in this instance.
     *
     * @param p      the player who is viewing the placeholder
     * @param params the placeholder to look up to.
     * @return the value of the placeholder if found, or empty {@link String} if no
     * value found but the placeholder is registered,
     * otherwise {@code null}
     * @throws Exception if any error occurs
     */
    public abstract String onPlaceholderRequest(Player p, String params) throws Exception;
}
