package com.djrapitops.plan;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.command.PlanCommand;
import com.djrapitops.plan.system.SpongeSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.PluginLang;
import com.djrapitops.plan.system.settings.theme.PlanColorScheme;
import com.djrapitops.plan.utilities.metrics.BStatsSponge;
import com.djrapitops.plugin.SpongePlugin;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.utility.log.DebugLog;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.settings.ColorScheme;
import com.google.inject.Inject;
import org.bstats.sponge.Metrics;
import org.slf4j.Logger;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

import java.io.File;
import java.io.InputStream;

@Plugin(
        id = "plan",
        name = "Plan",
        version = "4.4.6",
        description = "Player Analytics Plugin by Rsl1122",
        authors = {"Rsl1122"},
        dependencies = {
                @Dependency(id = "nucleus", optional = true),
                @Dependency(id = "luckperms", optional = true)
        }
)
public class PlanSponge extends SpongePlugin implements PlanPlugin {

    @Inject
    private Metrics metrics;

    @Inject
    private Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private File dataFolder;
    private SpongeSystem system;
    private Locale locale;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        onEnable();
    }

    @Listener
    public void onServerStop(GameStoppingServerEvent event) {
        onDisable();
    }

    public static PlanSponge getInstance() {
        return (PlanSponge) StaticHolder.getInstance(PlanSponge.class);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        try {
            system = new SpongeSystem(this);
            locale = system.getLocaleSystem().getLocale();
            system.enable();

            new BStatsSponge(metrics).registerMetrics();

            Log.info(locale.getString(PluginLang.ENABLED));
        } catch (AbstractMethodError e) {
            Log.error("Plugin ran into AbstractMethodError - Server restart is required. Likely cause is updating the jar without a restart.");
        } catch (EnableException e) {
            Log.error("----------------------------------------");
            Log.error("Error: " + e.getMessage());
            Log.error("----------------------------------------");
            Log.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /plan reload");
            onDisable();
        } catch (Exception e) {
            Log.toLog(this.getClass().getSimpleName() + "-v" + getVersion(), e);
            Log.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /plan reload");
            Log.error("This error should be reported at https://github.com/Rsl1122/Plan-PlayerAnalytics/issues");
            onDisable();
        }
        registerCommand("plan", new PlanCommand(this));
    }

    @Override
    public void onDisable() {
        if (system != null) {
            system.disable();
        }

        Log.info(locale.getString(PluginLang.DISABLED));
        Benchmark.pluginDisabled(PlanSponge.class);
        DebugLog.pluginDisabled(PlanSponge.class);
    }

    @Override
    public InputStream getResource(String resource) {
        return getClass().getResourceAsStream("/" + resource);
    }

    @Override
    public ColorScheme getColorScheme() {
        return PlanColorScheme.create();
    }

    @Override
    public void onReload() {
        // Nothing to be done, systems are disabled
    }

    @Override
    public boolean isReloading() {
        return false;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public String getVersion() {
        return getClass().getAnnotation(Plugin.class).version();
    }

    @Override
    public SpongeSystem getSystem() {
        return system;
    }
}
