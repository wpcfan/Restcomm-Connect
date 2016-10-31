/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2016, Telestax Inc and individual contributors
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
package org.restcomm.connect.rvd.storage.daos;

import org.restcomm.connect.rvd.model.CallControlInfo;
import org.restcomm.connect.rvd.storage.WorkspaceStorage;
import org.restcomm.connect.rvd.storage.exceptions.StorageException;

public class FsCallControlInfoDao {

    public static CallControlInfo loadInfo(String projectName, WorkspaceStorage workspaceStorage) throws StorageException {
        return workspaceStorage.loadEntity("cc", projectName, CallControlInfo.class);
        //return storageBase.loadModelFromProjectFile(projectName, "", "cc", CallControlInfo.class);
    }

    public static void storeInfo(CallControlInfo info, String projectName, WorkspaceStorage workspaceStorage) throws StorageException {
        workspaceStorage.storeEntity(info, CallControlInfo.class, "cc", projectName);
    }

    public static void clearInfo(String projectName, WorkspaceStorage workspaceStorage) throws StorageException {
        workspaceStorage.removeEntity("cc", projectName);
    }

}
