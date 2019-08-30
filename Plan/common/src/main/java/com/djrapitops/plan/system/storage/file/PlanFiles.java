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
package com.djrapitops.plan.system.storage.file;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plugin.utilities.Verify;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Abstracts File methods of Plugin classes so that they can be tested without Mocks.
 *
 * @author Rsl1122
 */
@Singleton
public class PlanFiles implements SubSystem {

    protected final PlanPlugin plugin;

    private final File dataFolder;
    private final File configFile;

    @Inject
    public PlanFiles(PlanPlugin plugin) {
        this.dataFolder = plugin.getDataFolder();
        this.plugin = plugin;
        this.configFile = getFileFromPluginFolder("config.yml");
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public File getLogsFolder() {
        File folder = getFileFromPluginFolder("logs");
        folder.mkdirs();
        return folder;
    }

    public File getConfigFile() {
        return configFile;
    }

    public File getLocaleFile() {
        return getFileFromPluginFolder("locale.txt");
    }

    public File getFileFromPluginFolder(String name) {
        return new File(dataFolder, name.replace("/", File.separator));
    }

    @Override
    public void enable() throws EnableException {
        Verify.isTrue((dataFolder.exists() && dataFolder.isDirectory()) || dataFolder.mkdirs(),
                () -> new EnableException("Could not create data folder at " + dataFolder.getAbsolutePath()));
        try {
            Verify.isTrue((configFile.exists() && configFile.isFile()) || configFile.createNewFile(),
                    () -> new EnableException("Could not create config file at " + configFile.getAbsolutePath()));
        } catch (IOException e) {
            throw new EnableException("Failed to create config.yml", e);
        }
    }

    @Override
    public void disable() {
        // No disable actions necessary.
    }

    /**
     * Get a file in the jar as a {@link Resource}.
     *
     * @param resourceName Path to the file inside jar/assets/plan/ folder.
     * @return a {@link Resource} for accessing the resource.
     */
    public Resource getResourceFromJar(String resourceName) {
        return new JarResource("assets/plan/" + resourceName, () -> plugin.getResource("assets/plan/" + resourceName));
    }

    /**
     * Get a file from plugin folder as a {@link Resource}.
     *
     * @param resourceName Path to the file inside the plugin folder.
     * @return a {@link Resource} for accessing the resource.
     */
    public Resource getResourceFromPluginFolder(String resourceName) {
        return new FileResource(resourceName, getFileFromPluginFolder(resourceName));
    }

    /**
     * Get a customizable resource from the plugin files or from the jar if one doesn't exist.
     *
     * @param resourceName Path to the file inside the plugin folder.
     * @return a {@link Resource} for accessing the resource, either from the plugin folder or jar.
     */
    public Resource getCustomizableResourceOrDefault(String resourceName) {
        return attemptToFind(resourceName).map(file -> (Resource) new FileResource(resourceName, file)).orElse(getResourceFromJar(resourceName));
    }

    private Optional<File> attemptToFind(String resourceName) {
        if (dataFolder.exists() && dataFolder.isDirectory()) {

            String[] path = StringUtils.split(resourceName, '/');

            Path toFile = dataFolder.getAbsoluteFile().toPath().toAbsolutePath();
            for (String next : path) {
                toFile = toFile.resolve(next);
            }

            File found = toFile.toFile();
            if (found.exists()) {
                return Optional.of(found);
            }
        }
        return Optional.empty();
    }
}
