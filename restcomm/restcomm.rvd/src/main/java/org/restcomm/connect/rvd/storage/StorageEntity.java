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
package org.restcomm.connect.rvd.storage;

import java.util.Date;

/**
 * An abstraction layer over the file. It help decouple storage layer from filesystem.
 *
 * @author otsakir@gmail.com - Orestis Tsakiridis
 */
public class StorageEntity {
    String name;
    Boolean isDirectory;
    Date lastModified;

    public StorageEntity(String name, Boolean isDirectory) {
        this.name = name;
        this.isDirectory = isDirectory;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDirectory(Boolean directory) {
        isDirectory = directory;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
}
