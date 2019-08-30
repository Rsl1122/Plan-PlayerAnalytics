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
package com.djrapitops.plan.system.storage.database.access.transactions.events;

import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.system.storage.database.access.transactions.Transaction;
import com.djrapitops.plan.system.storage.database.queries.DataStoreQueries;

import java.util.UUID;
import java.util.function.BiPredicate;

/**
 * Transaction to store player's nickname information in the database.
 *
 * @author Rsl1122
 */
public class NicknameStoreTransaction extends Transaction {

    private final UUID playerUUID;
    private final Nickname nickname;
    private final BiPredicate<UUID, String> isNicknameCachedCheck;

    public NicknameStoreTransaction(UUID playerUUID, Nickname nickname, BiPredicate<UUID, String> isNicknameCachedCheck) {
        this.playerUUID = playerUUID;
        this.nickname = nickname;
        this.isNicknameCachedCheck = isNicknameCachedCheck;
    }

    @Override
    protected boolean shouldBeExecuted() {
        return !isNicknameCachedCheck.test(playerUUID, nickname.getName());
    }

    @Override
    protected void performOperations() {
        execute(DataStoreQueries.storePlayerNickname(playerUUID, nickname));
    }
}