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
 * A class representing a damage type, such as fire, piercing, etc.
 * <p>
 * All damage types are classified as either physical or energy.
 * <p>
 * This class is immutable
 *
 * @author Jared
 */

public class DamageType
{
    private String name;
    private boolean isEnergyType;

    /**
     * Creates a new DamageType
     *
     * @param name         the name of the damage type
     * @param isEnergyType whether this is an energy damage type (true), or a physical damage type (false)
     */

    public DamageType(String name, boolean isEnergyType)
    {
        this.name = name;
        this.isEnergyType = isEnergyType;
    }

    /**
     * Returns the identifying name of this damage type
     *
     * @return the name
     */

    public String getName()
    {
        return name;
    }

    /**
     * Returns true if and only if this is an energy damage type
     *
     * @return whether this is an energy damage type
     */

    public boolean isEnergy()
    {
        return isEnergyType;
    }

    /**
     * Returns true if and only if this is a physical damage type
     *
     * @return whether this is a physical damage type
     */

    public boolean isPhysical()
    {
        return !isEnergyType;
    }
}
