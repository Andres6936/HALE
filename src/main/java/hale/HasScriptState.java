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

package main.java.hale;

/**
 * interface to be implemented by any class with a script state
 *
 * @author Jared
 */

public interface HasScriptState
{
    /**
     * Gets the value stored by the specified key
     *
     * @param key the key to get the value for
     * @return the value for the specified key
     */

    public Object get(String key);

    /**
     * Puts the specified key value pair in the script state
     *
     * @param key   the key to associated with the value
     * @param value the value to put
     */

    public void put(String key, Object value);
}
