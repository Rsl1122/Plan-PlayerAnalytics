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
package com.djrapitops.plan;

import com.djrapitops.plan.command.use.BungeeCommand;
import com.djrapitops.plan.commands.use.ColorScheme;
import com.djrapitops.plan.commands.use.Subcommand;
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.settings.theme.PlanColorScheme;
import io.github.slimjar.app.builder.ApplicationBuilder;
import net.md_5.bungee.api.plugin.Plugin;
import net.playeranalytics.plugin.BungeePlatformLayer;
import net.playeranalytics.plugin.PlatformAbstractionLayer;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.server.PluginLogger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bungee Main class.
 *
 * @author AuroraLS3
 */
public class PlanBungee extends Plugin implements PlanPlugin {

    private PlanSystem system;
    private Locale locale;

    private PluginLogger logger;
    private RunnableFactory runnableFactory;
    private PlatformAbstractionLayer abstractionLayer;

    @Override
    public void onLoad() {
        abstractionLayer = new BungeePlatformLayer(this);
        logger = abstractionLayer.getPluginLogger();
        runnableFactory = abstractionLayer.getRunnableFactory();

        getLogger().log(Level.INFO, "Loading dependencies, this might take a while...");
        try {
            ApplicationBuilder.appending("Plan").downloadDirectoryPath(Paths.get(getDataFolder().getAbsolutePath()).resolve("libraries")).build();
        } catch (IOException | ReflectiveOperationException | URISyntaxException | NoSuchAlgorithmException e) {
            String version = abstractionLayer.getPluginInformation().getVersion();
            getLogger().log(Level.SEVERE, e, () -> this.getClass().getSimpleName() + "-v" + version);
            getLogger().log(Level.SEVERE, "Plan failed to load its dependencies correctly!");
            getLogger().log(Level.SEVERE, "This error should be reported at https://github.com/plan-player-analytics/Plan/issues");
            onDisable();
        }
    }

    @Override
    public void onEnable() {
        PlanBungeeComponent component = DaggerPlanBungeeComponent.builder()
                .plan(this)
                .abstractionLayer(abstractionLayer)
                .build();
        try {
            system = component.system();
            locale = system.getLocaleSystem().getLocale();
            system.enable();

            new BStatsBungee(
                    this,
                    system.getDatabaseSystem().getDatabase()
            ).registerMetrics();

            logger.info(locale.getString(PluginLang.ENABLED));
        } catch (AbstractMethodError e) {
            logger.error("Plugin ran into AbstractMethodError - Server restart is required. Likely cause is updating the jar without a restart.");
        } catch (EnableException e) {
            logger.error("----------------------------------------");
            logger.error("Error: " + e.getMessage());
            logger.error("----------------------------------------");
            logger.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /planbungee reload");
            onDisable();
        } catch (Exception e) {
            String version = abstractionLayer.getPluginInformation().getVersion();
            Logger.getGlobal().log(Level.SEVERE, e, () -> this.getClass().getSimpleName() + "-v" + version);
            logger.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /planbungee reload");
            logger.error("This error should be reported at https://github.com/plan-player-analytics/Plan/issues");
            onDisable();
        }
        registerCommand(component.planCommand().build());
        if (system != null) {
            system.getProcessing().submitNonCritical(() -> system.getListenerSystem().callEnableEvent(this));
        }
    }

    @Override
    public void onDisable() {
        runnableFactory.cancelAllKnownTasks();
        if (system != null) system.disable();

        logger.info(locale.getString(PluginLang.DISABLED));
    }

    @Override
    public void registerCommand(Subcommand command) {
        if (command == null) {
            logger.warn("Attempted to register a null command!");
            return;
        }
        for (String name : command.getAliases()) {
            getProxy().getPluginManager().registerCommand(this, new BungeeCommand(runnableFactory, system.getErrorLogger(), command, name));
        }
    }

    @Override
    public InputStream getResource(String resource) {
        return getResourceAsStream(resource);
    }

    @Override
    public ColorScheme getColorScheme() {
        return PlanColorScheme.create(system.getConfigSystem().getConfig(), logger);
    }

    @Override
    public PlanSystem getSystem() {
        return system;
    }
}
