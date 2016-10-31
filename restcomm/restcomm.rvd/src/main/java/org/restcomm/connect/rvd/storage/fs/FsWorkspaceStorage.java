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
package org.restcomm.connect.rvd.storage.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.restcomm.connect.rvd.model.CallControlInfo;
import org.restcomm.connect.rvd.model.ModelMarshaler;
import org.restcomm.connect.rvd.storage.StorageEntity;
import org.restcomm.connect.rvd.storage.WorkspaceStorage;
import org.restcomm.connect.rvd.storage.exceptions.StorageEntityNotFound;
import org.restcomm.connect.rvd.storage.exceptions.StorageException;

public class FsWorkspaceStorage implements WorkspaceStorage {
    String rootPath;
    ModelMarshaler marshaler;

    public FsWorkspaceStorage(String rootPath, ModelMarshaler marshaler ) {
        this.rootPath = rootPath;
        this.marshaler = marshaler;
    }

    @Override
    public boolean entityExists(String entityName, String relativePath) {
        if ( !relativePath.startsWith( "/") )
            relativePath = File.separator + relativePath;
        String pathname = rootPath + relativePath + File.separator + entityName;
        File file = new File(pathname);
        return file.exists();
    }

    @Override
    public <T> T loadEntity(String entityName, String relativePath, Class<T> entityClass) throws StorageException {
        // make sure relativePaths (path within the workspace) start with "/"
        if ( !relativePath.startsWith( "/") )
            relativePath = File.separator + relativePath;

        String pathname = rootPath + relativePath + File.separator + entityName;

        File file = new File(pathname);
        if ( !file.exists() )
            throw new StorageEntityNotFound("File " + file.getPath() + " does not exist");

        String data;
        try {
            data = FileUtils.readFileToString(file, Charset.forName("UTF-8"));
            T instance = marshaler.toModel(data, entityClass);
            return instance;
        } catch (IOException e) {
            throw new StorageException("Error loading file " + file.getPath(), e);
        }
    }

    @Override
    public <T> T loadEntity(String entityName, String relativePath, Type gsonType) throws StorageException {
        // make sure relativePaths (path within the workspace) start with "/"
        if ( !relativePath.startsWith( "/") )
            relativePath = File.separator + relativePath;

        String pathname = rootPath + relativePath + File.separator + entityName;

        File file = new File(pathname);
        if ( !file.exists() )
            throw new StorageEntityNotFound("File " + file.getPath() + " does not exist");

        String data;
        try {
            data = FileUtils.readFileToString(file, Charset.forName("UTF-8"));
            T instance = marshaler.toModel(data, gsonType);
            return instance;
        } catch (IOException e) {
            throw new StorageException("Error loading file " + file.getPath(), e);
        }
    }

    @Override
    public InputStream loadStream(String entityName, String relativePath) throws StorageException {
        if ( !relativePath.startsWith( "/") )
            relativePath = File.separator + relativePath;
        String pathname = rootPath + relativePath + File.separator + entityName;

        File file = new File(pathname);
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new StorageEntityNotFound("File " + file.getPath() + " does not exist");
        }
    }



    @Override
    public void storeEntity(Object entity, Class<?> entityClass, String entityName, String relativePath) throws StorageException {
        if ( !relativePath.startsWith("/") )
            relativePath = "/" + relativePath;

        String pathname = rootPath + relativePath + File.separator + entityName;
        File file = new File(pathname);
        String data = marshaler.getGson().toJson(entity, entityClass);
        try {
            FileUtils.writeStringToFile(file, data, "UTF-8");
        } catch (IOException e) {
            throw new StorageException("Error creating file in storage: " + file, e);
        }
    }

    @Override
    public void storeEntity(Object entity, String entityName, String relativePath) throws StorageException {
        if ( !relativePath.startsWith("/") )
            relativePath = "/" + relativePath;

        String pathname = rootPath + relativePath + File.separator + entityName;
        File file = new File(pathname);
        String data = marshaler.getGson().toJson(entity);
        try {
            FileUtils.writeStringToFile(file, data, "UTF-8");
        } catch (IOException e) {
            throw new StorageException("Error creating file in storage: " + file, e);
        }
    }

    @Override
    public void removeEntity(String entityName, String relativePath) {
        if ( !relativePath.startsWith("/") )
            relativePath = "/" + relativePath;
        String pathname = rootPath + relativePath + File.separator + entityName;
        File file = new File(pathname);
        FileUtils.deleteQuietly(file);
    }

    @Override
    public List<StorageEntity> listEntities(String relativePath) {

        return null;
    }


    @Override
    public void storeFile(Object item, Class<?> itemClass, File file) throws StorageException {
        String data;
        data = marshaler.getGson().toJson(item, itemClass);

        try {
            FileUtils.writeStringToFile(file, data, "UTF-8");
        } catch (IOException e) {
            throw new StorageException("Error creating file in storage: " + file, e);
        }
    }

    @Override
    public String loadEntityString(String entityName, String relativePath) throws StorageException {
        if ( !relativePath.startsWith( "/") )
            relativePath = File.separator + relativePath;

        String pathname = rootPath + relativePath + File.separator + entityName;

        File file = new File(pathname);
        if ( !file.exists() )
            throw new StorageEntityNotFound("File " + file.getPath() + " does not exist");

        String data;
        try {
            data = FileUtils.readFileToString(file, Charset.forName("UTF-8"));
            return data;
        } catch (IOException e) {
            throw new StorageException("Error loading file " + file.getPath(), e);
        }
    }

    @Override
    public void storeEntityString(String entityString, String entityName, String relativePath) throws StorageException {
        if ( !relativePath.startsWith("/") )
            relativePath = "/" + relativePath;

        String pathname = rootPath + relativePath + File.separator + entityName;
        File file = new File(pathname);
        try {
            FileUtils.writeStringToFile(file, entityString, "UTF-8");
        } catch (IOException e) {
            throw new StorageException("Error creating file in storage: " + file, e);
        }
    }

    @Override
    public void storeBinaryFile(File sourceFile, String entityName, String relativePath) throws StorageException {
        if ( !relativePath.startsWith("/") )
            relativePath = "/" + relativePath;

        String pathname = rootPath + relativePath + File.separator + entityName;
        //File destFile = new File(getProjectBasePath(projectName) + File.separator + RvdConfiguration.PACKAGING_DIRECTORY_NAME + File.separator + "app.zip");
        File destFile = new File( pathname );
        try {
            FileUtils.copyFile(sourceFile, destFile);
            FileUtils.deleteQuietly(sourceFile);
        } catch (IOException e) {
            throw new StorageException("Error copying binary file into project", e);
        }
    }

    @Override
    public InputStream loadBinaryFile(String projectName, String entityName, String relativePath) throws FileNotFoundException {
        if ( !relativePath.startsWith("/") )
            relativePath = "/" + relativePath;
        String pathname = rootPath + relativePath + File.separator + entityName;

        File packageFile = new File( pathname );
        return new FileInputStream(packageFile);
    }

    /*
    public static void storeInfo(CallControlInfo info, String projectName, WorkspaceStorage workspaceStorage) throws StorageException {
        workspaceStorage.storeEntity(info, CallControlInfo.class, "cc", projectName);
    }*/

    @Override
    public <T> T loadModelFromXMLFile(String filepath, Class<T> modelClass) throws StorageException {
        File file = new File(filepath);
        return loadModelFromXMLFile(file, modelClass);
    }

    // CAUTION! what happens if the typecasting fails? solve this..
    @Override
    public <T> T loadModelFromXMLFile(File file, Class<T> modelClass) throws StorageException {
        if ( !file.exists() )
            throw new StorageEntityNotFound("Cannot find file: " + file.getPath() );

        try {
            String data = FileUtils.readFileToString(file, "UTF-8");
            T instance = (T) marshaler.getXStream().fromXML(data);
            return instance;

        } catch (IOException e) {
            throw new StorageException("Error loading model from file '" + file + "'", e);
        }
    }

    @Override
    public <T> T loadModelFromFile(String filepath, Type gsonType) throws StorageException {
        File file = new File(filepath);
        return loadModelFromFile(file, gsonType);
    }

    @Override
    public <T> T loadModelFromFile(File file, Type gsonType) throws StorageException {
        if ( !file.exists() )
            throw new StorageEntityNotFound("Cannot find file: " + file.getPath() );

        try {
            String data = FileUtils.readFileToString(file, "UTF-8");
            T instance = marshaler.getGson().fromJson(data, gsonType);
            return instance;

        } catch (IOException e) {
            throw new StorageException("Error loading model from file '" + file + "'", e);
        }
    }



}
