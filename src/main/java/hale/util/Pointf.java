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

package hale.util;

/**
 * A point consisting of two floating point numbers.  Note that
 * unlike {@link Point}, this class is immutable.
 *
 * @author Jared Stephen
 */

public class Pointf
{
    /**
     * The x coordinate of this Pointf
     */
    public final float x;

    /**
     * The y coordinate of this Pointf
     */
    public final float y;

    /**
     * Creates a new Pointf with the specified x and y coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */

    public Pointf(float x, float y)
    {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString()
    {
        return "(" + x + ", " + y + ")";
    }
}
