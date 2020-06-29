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

import hale.area.Area;

/**
 * An immutable version of the Point class, with efficient implementations of hashing, etc
 *
 * @author Jared
 */

public class PointImmutable
{
    /**
     * The x coordinate of this Point
     */

    public final short x;

    /**
     * The y coordinate of this Point
     */

    public final short y;

    /**
     * Creates a new Point with the specified x and y coordinates
     *
     * @param x
     * @param y
     */

    public PointImmutable(int x, int y)
    {
        if (x > Short.MAX_VALUE || y > Short.MAX_VALUE) {
            throw new IllegalArgumentException("PointImmutable can only accept arguments less than 2^15");
        }

        this.x = (short)x;
        this.y = (short)y;
    }

    /**
     * Creates a new PointImmutable from the specified mutable point
     *
     * @param p
     */

    public PointImmutable(Point p)
    {
        this(p.x, p.y);
    }

    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof PointImmutable)) return false;

        return hashCode() == other.hashCode();
    }

    @Override
    public int hashCode()
    {
        return x + (((int)y) << 16);
    }

    @Override
    public String toString()
    {
        return x + ", " + y;
    }

    /**
     * Returns true if and only if this point is within the grid bounds of the specified area
     *
     * @param area
     * @return whether this point is within the grid bounds of the area
     */

    public boolean isWithinBounds(Area area)
    {
        if (x < 0 || y < 0) return false;

        if (x >= area.getWidth() || y >= area.getHeight()) return false;

        return true;
    }
}
