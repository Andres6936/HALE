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
 * A specific type of armor such as light, medium, or heavy
 *
 * @author Jared
 */

public class ArmorType
{
    private final String name;

    /**
     * Creates a new armor type with the specified name
     *
     * @param name the name for the armorType
     */

    public ArmorType(String name)
    {
        this.name = name;
    }

    /**
     * Returns the name of this ArmorType
     *
     * @return the name
     */

    public String getName()
    {
        return name;
    }
}
