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
package com.djrapitops.plan.extension.implementation;

import com.djrapitops.extension.*;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.ExtensionService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * In charge of registering built in {@link com.djrapitops.plan.extension.DataExtension} implementations.
 *
 * @author Rsl1122
 */
@Singleton
public class ExtensionRegister {

    @Inject
    public ExtensionRegister() {
        /* Required for dagger injection */
    }

    public void registerBuiltInExtensions() {
        // No need to catch exceptions here,
        // registerBuiltInExtensions method will not be called unless Plan has enabled properly
        ExtensionService extensionService = ExtensionService.getInstance();

        new AdvancedAchievementsExtensionFactory().createExtension().ifPresent(extensionService::register);
        new AdvancedBanExtensionFactory().createExtension().ifPresent(extensionService::register);
        new ASkyBlockExtensionFactory().createExtension().ifPresent(extensionService::register);
        new BanManagerExtensionFactory().createExtension().ifPresent(extensionService::register);
        new CoreProtectExtensionFactory().createExtension().ifPresent(extensionService::register);
        new DiscordSRVExtensionFactory().createExtension().ifPresent(extensionService::register);
        registerEssentialsExtension(extensionService);
        new GriefPreventionExtensionFactory().createExtension().ifPresent(extensionService::register);
        new GriefPreventionSpongeExtensionFactory().createExtension().ifPresent(extensionService::register);
        new GriefPreventionPlusExtensionFactory().createExtension().ifPresent(extensionService::register);
        new McMMOExtensionFactory().createExtension().ifPresent(extensionService::register);
        registerMinigameLibExtensions(extensionService);
        new NucleusExtensionFactory().createExtension().ifPresent(extensionService::register);
        new RedProtectExtensionFactory().createExtension().ifPresent(extensionService::register);
        new SpongeEconomyExtensionFactory().createExtension().ifPresent(extensionService::register);
        new VaultExtensionFactory().createExtension().ifPresent(extensionService::register);
    }

    private void registerEssentialsExtension(ExtensionService extensionService) {
        EssentialsExtensionFactory essentials = new EssentialsExtensionFactory();
        essentials.createExtension()
                .flatMap(extensionService::register) // If the extension was registered this is present.
                .ifPresent(essentials::registerUpdateListeners);
    }

    private void registerMinigameLibExtensions(ExtensionService extensionService) {
        for (DataExtension minigame : new MinigameLibExtensionFactory().createExtensions()) {
            extensionService.register(minigame);
        }
    }

}