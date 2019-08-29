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
package com.djrapitops.plan.system.webserver.response.pages;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Rsl1122
 */
public class PlayerPageResponse extends PageResponse {

    private final UUID uuid;

    public PlayerPageResponse(UUID uuid, String html) {
        super.setHeader("HTTP/1.1 200 OK");
        super.setContent(html);
        this.uuid = uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerPageResponse)) return false;
        if (!super.equals(o)) return false;
        PlayerPageResponse that = (PlayerPageResponse) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), uuid);
    }
}
