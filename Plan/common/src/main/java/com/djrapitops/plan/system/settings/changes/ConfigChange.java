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
package com.djrapitops.plan.system.settings.changes;

import com.djrapitops.plan.system.settings.config.Config;

/**
 * Represents a change made to the config structure.
 *
 * @author Rsl1122
 */
public interface ConfigChange {

    boolean hasBeenApplied(Config config);

    void apply(Config config);

    String getAppliedMessage();

    class Moved extends Removed {

        final String newPath;

        public Moved(String oldPath, String newPath) {
            super(oldPath);
            this.newPath = newPath;
        }

        @Override
        public synchronized void apply(Config config) {
            if (!config.moveChild(oldPath, newPath)) {
                throw new IllegalStateException("Failed to move config node from '" + oldPath + "' to '" + newPath + "'");
            }
        }

        @Override
        public String getAppliedMessage() {
            return "Moved " + oldPath + " to " + newPath;
        }
    }

    class Copied extends Removed {

        final String newPath;

        public Copied(String oldPath, String newPath) {
            super(oldPath);
            this.newPath = newPath;
        }

        @Override
        public synchronized void apply(Config config) {
            config.getNode(oldPath).ifPresent(oldNode -> config.addNode(newPath).copyAll(oldNode));
        }

        @Override
        public String getAppliedMessage() {
            return "Copied value of " + oldPath + " to " + newPath;
        }
    }

    class Removed implements ConfigChange {
        String oldPath;

        public Removed(String oldPath) {
            this.oldPath = oldPath;
        }

        @Override
        public boolean hasBeenApplied(Config config) {
            return !config.getNode(oldPath).isPresent();
        }

        @Override
        public synchronized void apply(Config config) {
            if (!config.removeNode(oldPath)) {
                throw new IllegalStateException("Failed to remove config node from '" + oldPath + "'");
            }
        }

        @Override
        public String getAppliedMessage() {
            return "Removed " + oldPath;
        }
    }
}
