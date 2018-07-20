package com.djrapitops.plan.common.command.commands;

import com.djrapitops.plan.common.PlanPlugin;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;

public class DisableCommand extends CommandNode {

    private final PlanPlugin plugin;

    public DisableCommand(PlanPlugin planPlugin) {
        super("disable", "plan.reload", CommandType.ALL);
        this.plugin = planPlugin;
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        plugin.onDisable();
        sender.sendMessage(
                "§aPlan systems are now disabled. " +
                        "You can still use /planbungee reload to restart the plugin."
        );
    }
}
