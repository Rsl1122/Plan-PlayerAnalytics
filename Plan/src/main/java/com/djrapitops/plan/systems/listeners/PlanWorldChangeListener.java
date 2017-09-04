package main.java.com.djrapitops.plan.systems.listeners;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.Optional;
import java.util.UUID;

public class PlanWorldChangeListener implements Listener {
    private final Plan plugin;

    public PlanWorldChangeListener(Plan plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player p = event.getPlayer();
        String worldName = p.getWorld().getName();

        UUID uuid = p.getUniqueId();
        String gameMode = p.getGameMode().name();
        long time = MiscUtils.getTime();

        Optional<Session> cachedSession = plugin.getDataCache().getCachedSession(uuid);
        cachedSession.ifPresent(session -> session.changeState(worldName, gameMode, time));
    }
}
