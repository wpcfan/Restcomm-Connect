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


import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.restcomm.connect.rvd.RvdConfiguration;
import org.restcomm.connect.rvd.exceptions.ProjectDoesNotExist;
import org.restcomm.connect.rvd.exceptions.RvdException;
import org.restcomm.connect.rvd.exceptions.packaging.AppPackageDoesNotExist;
import org.restcomm.connect.rvd.model.packaging.Rapp;
import org.restcomm.connect.rvd.storage.WorkspaceStorage;
import org.restcomm.connect.rvd.storage.exceptions.StorageException;

/**
 * @author otsakir@gmail.com - Orestis Tsakiridis
 */
public class FsPackagingDao {

    public static Rapp loadRapp(String projectName, WorkspaceStorage storage) throws StorageException {
        //return storageBase.loadModelFromProjectFile(projectName, RvdConfiguration.PACKAGING_DIRECTORY_NAME, "rapp" , Rapp.class);
        return storage.loadEntity("rapp", projectName+"/" + RvdConfiguration.PACKAGING_DIRECTORY_NAME, Rapp.class);
    }

    public static void storeRapp(Rapp rapp, String projectName, WorkspaceStorage storage) throws StorageException {
        //storageBase.storeFileToProject(rapp, rapp.getClass(), projectName, RvdConfiguration.PACKAGING_DIRECTORY_NAME, "rapp");
        storage.storeEntity(rapp, rapp.getClass(), "rapp", projectName+"/"+RvdConfiguration.PACKAGING_DIRECTORY_NAME);
    }

    public static void storeRappBinary(File sourceFile, String projectName, WorkspaceStorage storage ) throws RvdException {
        //storageBase.storeProjectBinaryFile(sourceFile, projectName, RvdConfiguration.PACKAGING_DIRECTORY_NAME, "app.zip");
        storage.storeBinaryFile(sourceFile, "app.zip", projectName+"/"+RvdConfiguration.PACKAGING_DIRECTORY_NAME);
    }

    public static InputStream getRappBinary(String projectName, WorkspaceStorage storage) throws AppPackageDoesNotExist {
        try {
            //return storageBase.getProjectBinaryFile(projectName, RvdConfiguration.PACKAGING_DIRECTORY_NAME, "app.zip");
            return storage.loadBinaryFile(projectName, "app.zip", projectName+"/"+RvdConfiguration.PACKAGING_DIRECTORY_NAME );
        } catch (FileNotFoundException e) {
            throw new AppPackageDoesNotExist("Binary package does not exist for project " + projectName);
        }
    }

    public static boolean hasPackaging(String projectName, WorkspaceStorage storage) throws ProjectDoesNotExist {
        return storage.entityExists(RvdConfiguration.PACKAGING_DIRECTORY_NAME, projectName);
        //return storageBase.projectPathExists(projectName, RvdConfiguration.PACKAGING_DIRECTORY_NAME);
    }

    public static boolean binaryAvailable(String projectName, WorkspaceStorage storage) {
        //return storageBase.projectFileExists(projectName, RvdConfiguration.PACKAGING_DIRECTORY_NAME, "app.zip");
        return storage.entityExists("app.zip", projectName+"/"+RvdConfiguration.PACKAGING_DIRECTORY_NAME);
    }

}
