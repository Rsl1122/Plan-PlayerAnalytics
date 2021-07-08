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

import com.djrapitops.plan.addons.placeholderapi.BukkitPlaceholderRegistrar;
import com.djrapitops.plan.commands.use.BukkitCommand;
import com.djrapitops.plan.commands.use.ColorScheme;
import com.djrapitops.plan.commands.use.Subcommand;
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.gathering.ServerShutdownSave;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.settings.theme.PlanColorScheme;
import io.github.slimjar.app.builder.ApplicationBuilder;
import net.playeranalytics.plugin.BukkitPlatformLayer;
import net.playeranalytics.plugin.PlatformAbstractionLayer;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.server.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

/**
 * Main class for Bukkit that manages the plugin.
 *
 * @author AuroraLS3
 */
public class Plan extends JavaPlugin implements PlanPlugin {

    private PlanSystem system;
    private Locale locale;
    private ServerShutdownSave serverShutdownSave;

    private PluginLogger pluginLogger;
    private RunnableFactory runnableFactory;
    private PlatformAbstractionLayer abstractionLayer;

    @Override
    public void onLoad() {
        abstractionLayer = new BukkitPlatformLayer(this);
        pluginLogger = abstractionLayer.getPluginLogger();
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
        PlanBukkitComponent component = DaggerPlanBukkitComponent.builder()
                .plan(this)
                .abstractionLayer(abstractionLayer)
                .server(getServer())
                .build();
        try {
            system = component.system();
            serverShutdownSave = component.serverShutdownSave();
            locale = system.getLocaleSystem().getLocale();
            system.enable();

            registerMetrics();
            registerPlaceholderAPIExtension(component.placeholders());

            pluginLogger.info(locale.getString(PluginLang.ENABLED));
        } catch (AbstractMethodError e) {
            pluginLogger.error("Plugin ran into AbstractMethodError - Server restart is required. Likely cause is updating the jar without a restart.");
        } catch (EnableException e) {
            pluginLogger.error("----------------------------------------");
            pluginLogger.error("Error: " + e.getMessage());
            pluginLogger.error("----------------------------------------");
            pluginLogger.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /plan reload");
            onDisable();
        } catch (Exception e) {
            String version = abstractionLayer.getPluginInformation().getVersion();
            getLogger().log(Level.SEVERE, e, () -> this.getClass().getSimpleName() + "-v" + version);
            pluginLogger.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /plan reload");
            pluginLogger.error("This error should be reported at https://github.com/plan-player-analytics/Plan/issues");
            onDisable();
        }
        registerCommand(component.planCommand().build());
        if (system != null) {
            system.getProcessing().submitNonCritical(() -> system.getListenerSystem().callEnableEvent(this));
        }
    }

    private void registerPlaceholderAPIExtension(BukkitPlaceholderRegistrar placeholders) {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            runnableFactory.create(() -> {
                        try {
                            placeholders.register();
                        } catch (Exception | NoClassDefFoundError | NoSuchMethodError failed) {
                            pluginLogger.warn("Failed to register PlaceholderAPI placeholders: " + failed.toString());
                        }
                    }
            ).runTask();
        }
    }

    private void registerMetrics() {
        Plan plugin = this;
        // Spigot 1.14 requires Sync events to be fired from a server thread.
        // Registering a service fires a sync event, and bStats registers a service,
        // so this has to be run on the server thread.
        runnableFactory.create(() -> new BStatsBukkit(plugin).registerMetrics()).runTask();
    }

    @Override
    public ColorScheme getColorScheme() {
        return PlanColorScheme.create(system.getConfigSystem().getConfig(), pluginLogger);
    }

    @Override
    public void onDisable() {
        storeSessionsOnShutdown();
        cancelAllTasks();
        if (system != null) system.disable();

        pluginLogger.info(Locale.getStringNullSafe(locale, PluginLang.DISABLED));
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
                    pluginLogger.error("Failed to save sessions to database on shutdown: " + e.getCause().getMessage());
                } catch (TimeoutException e) {
                    pluginLogger.info(Locale.getStringNullSafe(locale, PluginLang.DISABLED_UNSAVED_SESSIONS_TIMEOUT));
                }
            }
        }
    }

    public void cancelAllTasks() {
        runnableFactory.cancelAllKnownTasks();
        Bukkit.getScheduler().cancelTasks(this);
    }

    @Override
    public void registerCommand(Subcommand command) {
        if (command == null) {
            pluginLogger.warn("Attempted to register a null command!");
            return;
        }
        for (String name : command.getAliases()) {
            PluginCommand registering = getCommand(name);
            if (registering == null) {
                pluginLogger.warn("Attempted to register '" + name + "'-command, but it is not in plugin.yml!");
                continue;
            }
            registering.setExecutor(new BukkitCommand(runnableFactory, system.getErrorLogger(), command));
        }
    }

    /**
     * @deprecated Deprecated due to use of APF Config
     */
    @Override
    @Deprecated
    public void reloadConfig() {
        throw new IllegalStateException("This method should be used on this plugin. Use onReload() instead");
    }

    /**
     * @deprecated Deprecated due to use of APF Config
     */
    @Override
    @Deprecated
    public FileConfiguration getConfig() {
        throw new IllegalStateException("This method should be used on this plugin. Use getMainConfig() instead");
    }

    /**
     * @deprecated Deprecated due to use of APF Config
     */
    @Override
    @Deprecated
    public void saveConfig() {
        throw new IllegalStateException("This method should be used on this plugin. Use getMainConfig().save() instead");
    }

    /**
     * @deprecated Deprecated due to use of APF Config
     */
    @Override
    @Deprecated
    public void saveDefaultConfig() {
        throw new IllegalStateException("This method should be used on this plugin.");
    }

    @Override
    public PlanSystem getSystem() {
        return system;
    }
}