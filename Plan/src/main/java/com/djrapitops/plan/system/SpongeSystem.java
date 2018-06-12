/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system;

import com.djrapitops.plan.PlanSponge;
import com.djrapitops.plan.ShutdownHook;
import com.djrapitops.plan.api.ServerAPI;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.database.ServerDBSystem;
import com.djrapitops.plan.system.file.FileSystem;
import com.djrapitops.plan.system.info.ServerInfoSystem;
import com.djrapitops.plan.system.info.server.SpongeServerInfo;
import com.djrapitops.plan.system.listeners.SpongeListenerSystem;
import com.djrapitops.plan.system.settings.PlanErrorManager;
import com.djrapitops.plan.system.settings.config.SpongeConfigSystem;
import com.djrapitops.plan.system.settings.network.NetworkSettings;
import com.djrapitops.plan.system.tasks.SpongeTaskSystem;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.api.utility.log.Log;

/**
 * Represents PlanSystem for PlanSponge.
 *
 * @author Rsl1122
 */
public class SpongeSystem extends PlanSystem implements ServerSystem {

    private boolean firstInstall = false;

    public SpongeSystem(PlanSponge plugin) {
        setTestSystem(this);

        Log.setErrorManager(new PlanErrorManager());

        versionCheckSystem = new VersionCheckSystem(plugin.getVersion());
        fileSystem = new FileSystem(plugin);
        configSystem = new SpongeConfigSystem();
        databaseSystem = new ServerDBSystem();
        listenerSystem = new SpongeListenerSystem(plugin);
        taskSystem = new SpongeTaskSystem(plugin);

        infoSystem = new ServerInfoSystem();
        serverInfo = new SpongeServerInfo();

        hookHandler = new HookHandler();
        planAPI = new ServerAPI(this);

        StaticHolder.saveInstance(ShutdownHook.class, plugin.getClass());
        new ShutdownHook().register();
    }

    public static SpongeSystem getInstance() {
        return PlanSponge.getInstance().getSystem();
    }

    @Override
    public void enable() throws EnableException {
        super.enable();
        NetworkSettings.loadSettingsFromDB();
    }
}
