/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2011 Jared Stephen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package hale.resource;

/**
 * The set of all valid Resource types currently used.
 * When requesting a resource from a package, the base
 * resource name (no extension) and the resourceType
 * can be used to obtain the resource.
 *
 * @author Jared Stephen
 */

public enum ResourceType
{
    PNG(".png"),
    JPEG(".jpg"),
    Text(".txt"),
    JavaScript(".js"),
    JSON(".json"),
    HTML(".html"),
    Zip(".zip"),
    SaveGame(".json.gz");

    private final String extension;
    private final int length;

    /**
     * Creates a new ResourceType with the specified String extension.
     *
     * @param extension the file extension of this resource Type.
     */

    ResourceType(String extension)
    {
        this.extension = extension;
        this.length = extension.length();
    }

    /**
     * Returns the String file name extension of this ResourceType.
     *
     * @return the filename extension of this ResourceType.
     */

    public String getExtension()
    {
        return extension;
    }

    /**
     * Returns the String length of the filename extension of this ResourceType
     *
     * @return the length of the filename extension of this ResourceType.
     */

    public int getLength()
    {
        return length;
    }
}
