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

package main.java.hale.rules;

/**
 * A class representing a racial type - Most races have one or more racial types, indicating
 * they have something in common with other similar races.  Creatures can have bonuses against
 * specific racial types.
 * <p>
 * This class is immutable
 *
 * @author Jared
 */

public class RacialType
{
    private String name;

    /**
     * Creates a new RacialType with the specified name
     *
     * @param name
     */

    public RacialType(String name)
    {
        this.name = name;
    }

    /**
     * Returns the name of this RacialType
     *
     * @return the name of this RacialType
     */

    public String getName()
    {
        return name;
    }
}
