package com.djrapitops.plan.common.system.database.databases.operation;

import com.djrapitops.plan.common.system.info.server.ServerInfo;

import java.util.UUID;

public interface CheckOperations {

    boolean isPlayerRegistered(UUID player);

    boolean isPlayerRegistered(UUID player, UUID server);

    boolean doesWebUserExists(String username);

    default boolean isPlayerRegisteredOnThisServer(UUID player) {
        return isPlayerRegistered(player, ServerInfo.getServerUUID());
    }

    boolean isServerInDatabase(UUID serverUUID);
}
