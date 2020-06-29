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
 * A mutable point class containing x and y coordinates.  This is useful in numerically
 * intensive places when speed is important
 *
 * @author Jared
 */

public class Point
{

    /**
     * The x coordinate
     */

    public int x;

    /**
     * The y coordinate
     */

    public int y;

    /**
     * Creates a new Point with the specified coordinates
     *
     * @param x
     * @param y
     */

    public Point(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * Creates a new Point with x and y coordinates equal to zero
     */

    public Point()
    {
        this.x = 0;
        this.y = 0;
    }

    /**
     * Creates a new Point with the same coordinates as the specified Point
     *
     * @param other the other Point
     */

    public Point(Point other)
    {
        this.x = other.x;
        this.y = other.y;
    }

    /**
     * Two points are equal only if their coordinates are equal.  Note that since
     * Points are mutable, this can change
     */

    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof Point)) return false;

        return ((Point)other).x == this.x && ((Point)other).y == this.y;
    }

    @Override
    public int hashCode()
    {
        return x + (y << 16);
    }

    /**
     * Returns the angle between this point and the specified other point, assuming both points
     * are in grid coordinates
     *
     * @param otherGrid
     * @return the angle between this point and the specified point, in radians
     */

    public double angleTo(Point otherGrid)
    {
        Point aScreen = AreaUtil.convertGridToScreen(this);
        Point bScreen = AreaUtil.convertGridToScreen(otherGrid);

        int xDiff = bScreen.x - aScreen.x;

        int yDiff = bScreen.y - aScreen.y;

        if (xDiff == 0) {
            return Math.signum(yDiff) * Math.PI / 2;
        } else
            if (xDiff > 0) {
                return Math.atan((double)yDiff / (double)xDiff);
            } else {
                return Math.atan((double)yDiff / (double)xDiff) + Math.PI;
            }
    }

    /**
     * Returns the distance between this point and the specified point, in screen pixels.
     * This assumes that both this point and the specified point are in grid coordinates
     *
     * @param otherGrid
     * @return the distance between this grid point and the specified grid point, in pixels
     */

    public double screenDistance(Point otherGrid)
    {
        Point aScreen = AreaUtil.convertGridToScreen(this);
        Point bScreen = AreaUtil.convertGridToScreen(otherGrid);

        int distSquared = AreaUtil.euclideanDistance2(aScreen.x, aScreen.y, bScreen.x, bScreen.y);
        return Math.sqrt(distSquared);
    }

    @Override
    public String toString()
    {
        return "(" + x + ", " + y + ")";
    }
}
