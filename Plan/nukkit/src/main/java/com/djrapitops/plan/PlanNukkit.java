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

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.plugin.PluginBase;
import com.djrapitops.plan.addons.placeholderapi.NukkitPlaceholderRegistrar;
import com.djrapitops.plan.commands.use.*;
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.gathering.ServerShutdownSave;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.settings.theme.PlanColorScheme;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import io.github.slimjar.app.builder.ApplicationBuilder;
import net.playeranalytics.plugin.NukkitPlatformLayer;
import net.playeranalytics.plugin.PlatformAbstractionLayer;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.server.PluginLogger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class for Nukkit that manages the plugin.
 *
 * @author AuroraLS3
 */
public class PlanNukkit extends PluginBase implements PlanPlugin {

    private PlanSystem system;
    private Locale locale;
    private ServerShutdownSave serverShutdownSave;

    private final Map<String, Subcommand> commands = new HashMap<>();
    private PluginLogger logger;
    private RunnableFactory runnableFactory;
    private PlatformAbstractionLayer abstractionLayer;

    @Override
    public void onLoad() {
        abstractionLayer = new NukkitPlatformLayer(this);
        logger = abstractionLayer.getPluginLogger();
        runnableFactory = abstractionLayer.getRunnableFactory();

        getLogger().info( "Loading dependencies, this might take a while...");
        try {
            ApplicationBuilder.appending("Plan").downloadDirectoryPath(Paths.get(getDataFolder().getAbsolutePath()).resolve("libraries")).build();
        } catch (IOException | ReflectiveOperationException | URISyntaxException | NoSuchAlgorithmException e) {
            String version = abstractionLayer.getPluginInformation().getVersion();
            Logger.getGlobal().log(Level.SEVERE, e, () -> this.getClass().getSimpleName() + "-v" + version);
            getLogger().error("Plan failed to load its dependencies correctly!");
            getLogger().error( "This error should be reported at https://github.com/plan-player-analytics/Plan/issues");
            onDisable();
        }
    }

    @Override
    public void onEnable() {
        PlanNukkitComponent component = DaggerPlanNukkitComponent.builder()
                .plan(this)
                .abstractionLayer(abstractionLayer)
                .build();
        try {
            system = component.system();
            serverShutdownSave = component.serverShutdownSave();
            locale = system.getLocaleSystem().getLocale();
            system.enable();

            registerPlaceholderAPI(component.placeholders());

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
            Logger.getGlobal().log(Level.SEVERE, e, () -> this.getClass().getSimpleName() + "-v" + version);
            logger.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /plan reload");
            logger.error("This error should be reported at https://github.com/plan-player-analytics/Plan/issues");
            onDisable();
        }

        registerCommand(component.planCommand().build());
        if (system != null) {
            system.getProcessing().submitNonCritical(() -> system.getListenerSystem().callEnableEvent(this));
        }
    }

    @Override
    public ColorScheme getColorScheme() {
        return PlanColorScheme.create(system.getConfigSystem().getConfig(), logger);
    }

    @Override
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
        Optional.ofNullable(Server.getInstance().getScheduler()).ifPresent(scheduler -> scheduler.cancelTask(this));
    }

    @Override
    public boolean onCommand(CommandSender actualSender, Command actualCommand, String label, String[] args) {
        String name = actualCommand.getName();
        Subcommand command = commands.get(name);
        if (command == null) return false;

        CMDSender sender;
        if (actualSender instanceof Player) {
            sender = new NukkitPlayerCMDSender((Player) actualSender);
        } else {
            sender = new NukkitCMDSender(actualSender);
        }

        runnableFactory.create(() -> {
            try {
                command.getExecutor().accept(sender, new Arguments(args));
            } catch (Exception e) {
                system.getErrorLogger().error(e, ErrorContext.builder()
                        .related(sender.getClass())
                        .related(label + " " + Arrays.toString(args))
                        .build());
            }
        }).runTaskAsynchronously();
        return true;
    }

    @Override
    public void registerCommand(Subcommand command) {
        if (command == null) {
            logger.warn("Attempted to register a null command!");
            return;
        }
        for (String name : command.getAliases()) {
            commands.put(name, command);
        }
    }

    @Override
    public PlanSystem getSystem() {
        return system;
    }

    private void registerPlaceholderAPI(NukkitPlaceholderRegistrar placeholders) {
        if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            runnableFactory.create(() -> {
                try {
                    placeholders.register();
                } catch (Exception | NoClassDefFoundError | NoSuchMethodError failed) {
                    logger.warn("Failed to register PlaceholderAPI placeholders: " + failed.toString());
                }
            }).runTask();
        }
    }
}