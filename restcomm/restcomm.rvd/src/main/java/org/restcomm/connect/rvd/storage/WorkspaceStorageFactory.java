/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */
package org.restcomm.connect.rvd.storage;

import org.restcomm.connect.rvd.RvdConfiguration;
import org.restcomm.connect.rvd.model.ModelMarshaler;
import org.restcomm.connect.rvd.storage.fs.FsWorkspaceStorage;

/**
 * @author otsakir@gmail.com - Orestis Tsakiridis
 */
public class WorkspaceStorageFactory {
    RvdConfiguration config = null;
    ModelMarshaler marshaler;

    public WorkspaceStorageFactory(RvdConfiguration config, ModelMarshaler marshaler) {
        if (config == null || marshaler == null)
            throw new IllegalArgumentException();
        this.config = config;
        this.marshaler = marshaler;
    }

    FsWorkspaceStorage createFsWorkspaceStorage() {
        return new FsWorkspaceStorage(config.getWorkspaceBasePath(), marshaler);
    }

    public WorkspaceStorage create() {
        // by default we return FsWorkspaceStorage since this is the single implementation we have
        // when other implementation are added, some sort of decision based on configuration should be done
        return createFsWorkspaceStorage();
    }
}
