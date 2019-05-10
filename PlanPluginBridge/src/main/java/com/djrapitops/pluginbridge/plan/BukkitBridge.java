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
package com.djrapitops.pluginbridge.plan;

import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.pluginbridge.plan.aac.AdvancedAntiCheatHook;
import com.djrapitops.pluginbridge.plan.buycraft.BuyCraftHook;
import com.djrapitops.pluginbridge.plan.factions.FactionsHook;
import com.djrapitops.pluginbridge.plan.jobs.JobsHook;
import com.djrapitops.pluginbridge.plan.kingdoms.KingdomsHook;
import com.djrapitops.pluginbridge.plan.litebans.LiteBansBukkitHook;
import com.djrapitops.pluginbridge.plan.luckperms.LuckPermsHook;
import com.djrapitops.pluginbridge.plan.protocolsupport.ProtocolSupportHook;
import com.djrapitops.pluginbridge.plan.superbvote.SuperbVoteHook;
import com.djrapitops.pluginbridge.plan.towny.TownyHook;
import com.djrapitops.pluginbridge.plan.viaversion.ViaVersionBukkitHook;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Plugin bridge for Bukkit plugins.
 *
 * @author Rsl1122
 */
@Singleton
public class BukkitBridge extends AbstractBridge {

    private final AdvancedAntiCheatHook advancedAntiCheatHook;
    private final BuyCraftHook buyCraftHook;
    private final FactionsHook factionsHook;
    private final JobsHook jobsHook;
    private final KingdomsHook kingdomsHook;
    private final LiteBansBukkitHook liteBansHook;
    private final LuckPermsHook luckPermsHook;
private final ProtocolSupportHook protocolSupportHook;
    private final SuperbVoteHook superbVoteHook;
    private final TownyHook townyHook;
    private final ViaVersionBukkitHook viaVersionHook;

    @Inject
    public BukkitBridge(
            PlanConfig config,
            ErrorHandler errorHandler,

            AdvancedAntiCheatHook advancedAntiCheatHook,
            BuyCraftHook buyCraftHook,
            FactionsHook factionsHook,
            JobsHook jobsHook,
            KingdomsHook kingdomsHook,
            LiteBansBukkitHook liteBansHook,
            LuckPermsHook luckPermsHook,
            ProtocolSupportHook protocolSupportHook,
            SuperbVoteHook superbVoteHook,
            TownyHook townyHook,
            ViaVersionBukkitHook viaVersionHook
    ) {
        super(config, errorHandler);
        this.advancedAntiCheatHook = advancedAntiCheatHook;
        this.buyCraftHook = buyCraftHook;
        this.factionsHook = factionsHook;
        this.jobsHook = jobsHook;
        this.kingdomsHook = kingdomsHook;
        this.liteBansHook = liteBansHook;
        this.luckPermsHook = luckPermsHook;
        this.protocolSupportHook = protocolSupportHook;
        this.superbVoteHook = superbVoteHook;
        this.townyHook = townyHook;
        this.viaVersionHook = viaVersionHook;
    }

    @Override
    Hook[] getHooks() {
        return new Hook[]{
                advancedAntiCheatHook,
                buyCraftHook,
                factionsHook,
                jobsHook,
                kingdomsHook,
                liteBansHook,
                luckPermsHook,
                protocolSupportHook,
                superbVoteHook,
                townyHook,
                viaVersionHook
        };
    }
}