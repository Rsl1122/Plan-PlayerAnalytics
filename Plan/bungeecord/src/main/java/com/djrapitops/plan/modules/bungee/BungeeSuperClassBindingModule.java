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
package com.djrapitops.plan.modules.bungee;

import com.djrapitops.plan.BungeeTaskSystem;
import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.gathering.BungeeSensor;
import com.djrapitops.plan.gathering.ServerSensor;
import com.djrapitops.plan.gathering.listeners.BungeeListenerSystem;
import com.djrapitops.plan.gathering.listeners.ListenerSystem;
import com.djrapitops.plan.identification.BungeeServerInfo;
import com.djrapitops.plan.identification.ServerInfo;
import dagger.Binds;
import dagger.Module;

/**
 * Module for binding Bungee specific classes as interface implementations.
 *
 * @author Rsl1122
 */
@Module
public interface BungeeSuperClassBindingModule {

    @Binds
    ServerInfo bindServerInfo(BungeeServerInfo serverInfo);

    @Binds
    TaskSystem bindTaskSystem(BungeeTaskSystem taskSystem);

    @Binds
    ListenerSystem bindListenerSystem(BungeeListenerSystem listenerSystem);

    @Binds
    ServerSensor<Object> bindServerSensor(BungeeSensor sensor);
}