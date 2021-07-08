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

import com.djrapitops.plan.commands.use.ColorScheme;
import com.djrapitops.plan.commands.use.SpongeCommand;
import com.djrapitops.plan.commands.use.Subcommand;
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.gathering.ServerShutdownSave;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.settings.theme.PlanColorScheme;
import io.github.slimjar.app.builder.ApplicationBuilder;
import net.playeranalytics.plugin.PlatformAbstractionLayer;
import net.playeranalytics.plugin.SpongePlatformLayer;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.server.PluginLogger;
import org.bstats.sponge.Metrics;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

@Plugin(
        id = "plan",
        name = "Plan",
        version = "@version@",
        description = "Player Analytics Plugin by AuroraLS3",
        authors = {"AuroraLS3"},
        dependencies = {
                @Dependency(id = "griefprevention", optional = true),
                @Dependency(id = "luckperms", optional = true),
                @Dependency(id = "nucleus", optional = true),
                @Dependency(id = "redprotect", optional = true),
                @Dependency(id = "nuvotifier", optional = true)
        }
)
public class PlanSponge implements PlanPlugin {

    private final Metrics metrics;
    private final Logger slf4jLogger;
    private final File dataFolder;

    private PlanSystem system;
    private Locale locale;
    private ServerShutdownSave serverShutdownSave;
    private PluginLogger logger;
    private RunnableFactory runnableFactory;
    private PlatformAbstractionLayer abstractionLayer;

    @com.google.inject.Inject
    public PlanSponge(
            Logger slf4jLogger,
            @ConfigDir(sharedRoot = false) File dataFolder,
            Metrics.Factory metrics
    ) {
        this.slf4jLogger = slf4jLogger;
        this.dataFolder = dataFolder;

        int pluginId = 3086;
        this.metrics = metrics.make(pluginId);
    }

    private final Map<String, CommandMapping> commands = new HashMap<>();

    @Listener
    public void onServerLoad(GamePreInitializationEvent event) {
        onLoad();
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        onEnable();
    }

    @Listener
    public void onServerStop(GameStoppingServerEvent event) {
        onDisable();
    }

    private void onLoad() {
        abstractionLayer = new SpongePlatformLayer(this, dataFolder, slf4jLogger);
        logger = abstractionLayer.getPluginLogger();
        runnableFactory = abstractionLayer.getRunnableFactory();

        logger.info("Loading dependencies, this might take a while...");
        try {
            ApplicationBuilder.appending("Plan").downloadDirectoryPath(Paths.get(getDataFolder().getAbsolutePath()).resolve("libraries")).build();
        } catch (IOException | ReflectiveOperationException | URISyntaxException | NoSuchAlgorithmException e) {
            String version = abstractionLayer.getPluginInformation().getVersion();
            java.util.logging.Logger.getGlobal().log(Level.SEVERE, e, () -> this.getClass().getSimpleName() + "-v" + version);
            logger.error("Plan failed to load its dependencies correctly!");
            logger.error("This error should be reported at https://github.com/plan-player-analytics/Plan/issues");
            onDisable();
        }
    }

    public void onEnable() {
        PlanSpongeComponent component = DaggerPlanSpongeComponent.builder()
                .plan(this)
                .abstractionLayer(abstractionLayer)
                .game(Sponge.getGame())
                .build();
        try {
            system = component.system();
            serverShutdownSave = component.serverShutdownSave();
            locale = system.getLocaleSystem().getLocale();
            system.enable();

            new BStatsSponge(
                    metrics, system.getDatabaseSystem().getDatabase()
            ).registerMetrics();

            logger.info(locale.getString(PluginLang.ENABLED));
        } catch (AbstractMethodError e) {
            logger.error("Plugin ran into AbstractMethodError - Server restart is required. Likely cause is updating the jar without a restart.");
        } catch (EnableException e) {
            logger.error("----------------------------------------");
            logger.error("Error: " + e.getMessage());
            logger.error("----------------------------------------");
            logger.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /plan reload");
            onDisable();
        } catch (Exception e) {
            String version = abstractionLayer.getPluginInformation().getVersion();
            java.util.logging.Logger.getGlobal().log(Level.SEVERE, e, () -> this.getClass().getSimpleName() + "-v" + version);
            logger.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /plan reload");
            logger.error("This error should be reported at https://github.com/plan-player-analytics/Plan/issues");
            onDisable();
        }
        registerCommand(component.planCommand().build());
        if (system != null) {
            system.getProcessing().submitNonCritical(() -> system.getListenerSystem().callEnableEvent(this));
        }
    }

    public void onDisable() {
        storeSessionsOnShutdown();
        cancelAllTasks();
        if (system != null) system.disable();

        logger.info(Locale.getStringNullSafe(locale, PluginLang.DISABLED));
    }

    private void storeSessionsOnShutdown() {
        if (serverShutdownSave != null) {
            Optional<Future<?>> complete = serverShutdownSave.performSave();
            if (complete.isPresent()) {
                try {
                    complete.get().get(4, TimeUnit.SECONDS); // wait for completion for 4s
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    logger.error("Failed to save sessions to database on shutdown: " + e.getCause().getMessage());
                } catch (TimeoutException e) {
                    logger.info(Locale.getStringNullSafe(locale, PluginLang.DISABLED_UNSAVED_SESSIONS_TIMEOUT));
                }
            }
        }
    }

    public void cancelAllTasks() {
        runnableFactory.cancelAllKnownTasks();
        for (Task task : Sponge.getScheduler().getScheduledTasks(this)) {
            task.cancel();
        }
    }

    @Override
    public InputStream getResource(String resource) {
        return getClass().getResourceAsStream("/" + resource);
    }

    @Override
    public ColorScheme getColorScheme() {
        return PlanColorScheme.create(system.getConfigSystem().getConfig(), logger);
    }

    @Override
    public void registerCommand(Subcommand command) {
        if (command == null) {
            logger.warn("Attempted to register a null command!");
            return;
        }
        for (String name : command.getAliases()) {
            CommandManager commandManager = Sponge.getCommandManager();

            CommandMapping registered = commands.get(name);
            if (registered != null) {
                commandManager.removeMapping(registered);
            }

            Optional<CommandMapping> register = commandManager.register(this, new SpongeCommand(runnableFactory, system.getErrorLogger(), command), name);
            register.ifPresent(commandMapping -> commands.put(name, commandMapping));
        }
    }

    @Override
    public PlanSystem getSystem() {
        return system;
    }

    public Game getGame() {
        return Sponge.getGame();
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }
}
