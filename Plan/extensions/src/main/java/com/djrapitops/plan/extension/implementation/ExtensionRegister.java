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
import com.djrapitops.plan.extension.Caller;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.ExtensionService;
import com.djrapitops.plan.extension.extractor.ExtensionExtractor;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * In charge of registering built in {@link com.djrapitops.plan.extension.DataExtension} implementations.
 *
 * @author Rsl1122
 */
@Singleton
public class ExtensionRegister {

    private IllegalStateException registerException;
    private Set<String> disabledExtensions;
    private ExtensionService extensionService;

    @Inject
    public ExtensionRegister() {
        /* Required for dagger injection */
    }

    public void registerBuiltInExtensions(Set<String> disabledExtensions) {
        this.disabledExtensions = disabledExtensions;
        extensionService = ExtensionService.getInstance();

        register(new AACExtensionFactory(), AACExtensionFactory::createExtension);
        register(new AdvancedAchievementsExtensionFactory(), AdvancedAchievementsExtensionFactory::createExtension);
        register(new AdvancedBanExtensionFactory(), AdvancedBanExtensionFactory::createExtension, AdvancedBanExtensionFactory::registerListener);
        register(new ASkyBlockExtensionFactory(), ASkyBlockExtensionFactory::createExtension);
        register(new BanManagerExtensionFactory(), BanManagerExtensionFactory::createExtension);
        register(new CoreProtectExtensionFactory(), CoreProtectExtensionFactory::createExtension);
        register(new DiscordSRVExtensionFactory(), DiscordSRVExtensionFactory::createExtension);
        register(new EssentialsExtensionFactory(), EssentialsExtensionFactory::createExtension, EssentialsExtensionFactory::registerUpdateListeners);
        register(new GriefPreventionExtensionFactory(), GriefPreventionExtensionFactory::createExtension);
        register(new GriefPreventionSpongeExtensionFactory(), GriefPreventionSpongeExtensionFactory::createExtension);
        register(new GriefPreventionPlusExtensionFactory(), GriefPreventionPlusExtensionFactory::createExtension);
        register(new McMMOExtensionFactory(), McMMOExtensionFactory::createExtension);
        registerMinigameLibExtensions();
        register(new NucleusExtensionFactory(), NucleusExtensionFactory::createExtension);
        register(new NuVotifierExtensionFactory(), NuVotifierExtensionFactory::createExtension);
        register(new ProtocolSupportExtensionFactory(), ProtocolSupportExtensionFactory::createExtension);
        register(new RedProtectExtensionFactory(), RedProtectExtensionFactory::createExtension);
        register(new SpongeEconomyExtensionFactory(), SpongeEconomyExtensionFactory::createExtension);
        register(new SuperbVoteExtensionFactory(), SuperbVoteExtensionFactory::createExtension);
        register(new VaultExtensionFactory(), VaultExtensionFactory::createExtension);
        register(new ViaVersionExtensionFactory(), ViaVersionExtensionFactory::createExtension);

        if (registerException != null) throw registerException;
    }

    private void registerMinigameLibExtensions() {
        for (DataExtension minigame : new MinigameLibExtensionFactory().createExtensions()) {
            register(minigame);
        }
    }

    private void suppressException(Class factory, Throwable e) {
        // Places all exceptions to one exception with plugin information so that they can be reported.
        if (registerException == null) {
            registerException = new IllegalStateException("One or more extensions failed to register:");
            registerException.setStackTrace(new StackTraceElement[0]);
        }
        IllegalStateException info = new IllegalStateException(factory.getSimpleName() + " ran into exception when creating Extension", e);
        info.setStackTrace(new StackTraceElement[0]);
        registerException.addSuppressed(info);
    }

    private <T> void register(
            T factory,
            Function<T, Optional<DataExtension>> createExtension
    ) {
        try {
            // Creates the extension with factory and registers it
            createExtension.apply(factory).flatMap(this::register);
        } catch (IllegalStateException | NoClassDefFoundError | IncompatibleClassChangeError e) {
            // Places all exceptions to one exception with plugin information so that they can be reported.
            suppressException(factory.getClass(), e);
        }
    }

    private <T> void register(
            T factory,
            Function<T, Optional<DataExtension>> createExtension,
            BiConsumer<T, Caller> registerListener
    ) {
        try {
            // Creates the extension with factory and registers it, then registers listener
            createExtension.apply(factory)
                    .flatMap(this::register)
                    .ifPresent(caller -> registerListener.accept(factory, caller));
        } catch (IllegalStateException | NoClassDefFoundError | IncompatibleClassChangeError e) {
            // Places all exceptions to one exception with plugin information so that they can be reported.
            suppressException(factory.getClass(), e);
        }
    }

    private Optional<Caller> register(DataExtension dataExtension) {
        String extensionName = ExtensionExtractor.getPluginName(dataExtension.getClass());
        if (disabledExtensions.contains(extensionName)) return Optional.empty();

        return extensionService.register(dataExtension);
    }
}