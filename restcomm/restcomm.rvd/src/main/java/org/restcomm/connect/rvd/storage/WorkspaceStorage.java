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


import org.restcomm.connect.rvd.storage.exceptions.StorageException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author otsakir@gmail.com - Orestis Tsakiridis
 */
public interface WorkspaceStorage {
    boolean entityExists(String entityName, String relativePath);

    <T> T loadEntity(String entityName, String relativePath, Class<T> entityClass) throws StorageException;

    <T> T loadEntity(String entityName, String relativePath, Type gsonType) throws StorageException;

    InputStream loadStream(String entityName, String relativePath) throws StorageException;

    void storeEntity(Object entity, Class<?> entityClass, String entityName, String relativePath) throws StorageException;

    void storeEntity(Object entity, String entityName, String relativePath) throws StorageException;

    void removeEntity(String entityName, String relativePath);

    List<StorageEntity> listEntities(String relativePath);

    void storeFile(Object item, Class<?> itemClass, File file) throws StorageException;

    String loadEntityString(String entityName, String relativePath) throws StorageException;

    void storeEntityString(String entityString, String entityName, String relativePath) throws StorageException;

    void storeBinaryFile(File sourceFile, String entityName, String relativePath) throws StorageException;

    InputStream loadBinaryFile(String projectName, String entityName, String relativePath) throws FileNotFoundException;

    <T> T loadModelFromXMLFile(String filepath, Class<T> modelClass) throws StorageException;

    // CAUTION! what happens if the typecasting fails? solve this..
    <T> T loadModelFromXMLFile(File file, Class<T> modelClass) throws StorageException;

    <T> T loadModelFromFile(String filepath, Type gsonType) throws StorageException;

    <T> T loadModelFromFile(File file, Type gsonType) throws StorageException;
}
