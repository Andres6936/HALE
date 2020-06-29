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

import java.text.NumberFormat;

/**
 * A class for representing the weight of an item.  This class is immutable
 *
 * @author Jared
 */

public class Weight
{
    /**
     * The number of grams in this weight
     */

    public final int grams;

    /**
     * Initializes a new weight object with a weight of zero
     */

    public Weight()
    {
        this.grams = 0;
    }

    /**
     * Initializes a new weight object to the specified weight in grams
     *
     * @param grams
     */

    public Weight(int grams)
    {
        this.grams = grams;
    }

    /**
     * initializes a new weight object to exactly the same weight as the
     * specified other weight
     *
     * @param other
     */

    public Weight(Weight other)
    {
        this.grams = other.grams;
    }

    /**
     * Returns a string representing the weight of this object in grams
     *
     * @return a string representing the weight of this object in grams
     */

    public String toStringGrams()
    {
        return grams + "";
    }

    /**
     * Returns a formatted string representing the weight of this object in kilograms
     *
     * @return a String representing the weight of this object in kilograms
     */

    public String toStringKilograms()
    {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        return nf.format(grams / 1000.0);
    }

    /**
     * Returns a formatted string representing the specified weight in kilograms
     *
     * @param grams
     * @return a String with the weight in kilograms
     */

    public static String toStringKilograms(int grams)
    {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        return nf.format(grams / 1000.0);
    }
}
