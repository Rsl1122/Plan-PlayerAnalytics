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
package com.djrapitops.plan.storage.upkeep;

import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.scheduling.TimeAmount;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

@Singleton
public class OldDependencyCacheDeletionTask extends TaskSystem.Task {

    private final File oldDependencyCache;
    private final File dependencyCache;

    private final ErrorLogger errorLogger;

    @Inject
    public OldDependencyCacheDeletionTask(
            PlanFiles files,
            ErrorLogger errorLogger
    ) {
        oldDependencyCache = files.getDataDirectory().resolve("dependency_cache").toFile();
        dependencyCache = files.getDataDirectory().resolve("dep_cache").toFile();
        this.errorLogger = errorLogger;
    }

    @Override
    public void register(RunnableFactory runnableFactory) {
        long delay = TimeAmount.toTicks(10L, TimeUnit.SECONDS);
        runnableFactory.create(this).runTaskLaterAsynchronously(delay);
    }

    @Override
    public void run() {
        tryToDeleteDirectory(oldDependencyCache);
        tryToDeleteDirectory(dependencyCache);
    }

    private void tryToDeleteDirectory(File directory) {
        try {
            if (directory.exists() && directory.isDirectory()) {
                deleteDirectory(directory);
            }
        } catch (IOException e) {
            errorLogger.error(e, ErrorContext.builder()
                    .whatToDo("Failed to delete '" + directory.getAbsolutePath() + "' - Delete it manually.")
                    .build());
        }
    }

    private void deleteDirectory(File directory) throws IOException {
        File[] files = directory.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                deleteDirectory(file);
            } else {
                Files.delete(file.toPath());
            }
        }
        Files.delete(directory.toPath());
    }
}
