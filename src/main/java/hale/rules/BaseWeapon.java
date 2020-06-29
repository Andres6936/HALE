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

package hale.rules;

/**
 * A BaseWeapon represents a particular class of weapon, such
 * as short sword, longsword, bow, or battleaxe
 *
 * @author Jared
 */

public class BaseWeapon
{
    private final String name;

    /**
     * Creates a new BaseWeapon
     *
     * @param name the name for the BaseWeapon
     */

    public BaseWeapon(String name)
    {
        this.name = name;
    }

    /**
     * Returns the name of this BaseWeapon
     *
     * @return the name
     */

    public String getName()
    {
        return name;
    }
}
