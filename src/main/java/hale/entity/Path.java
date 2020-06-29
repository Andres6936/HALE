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

package hale.entity;

import java.util.Iterator;
import java.util.List;

import hale.area.Area;
import hale.util.Point;

/**
 * A Path is a list of coordinates for a given area, going from one location
 * to another.
 * <p>
 * This class is immutable
 *
 * @author Jared
 */

public class Path implements Iterable<Location>
{
    private final Area area;

    private final int[] xCoords;
    private final int[] yCoords;

    private final Creature[] aoOs;

    /**
     * Constructs an empty path
     *
     * @param area
     */

    public Path(Area area)
    {
        this.area = area;

        xCoords = new int[0];
        yCoords = new int[0];
        this.aoOs = new Creature[0];
    }

    /**
     * Constructs a path consisting of a single location
     *
     * @param location the location
     */

    public Path(Location location)
    {
        this.area = location.getArea();

        xCoords = new int[1];
        yCoords = new int[1];

        xCoords[0] = location.getX();
        yCoords[0] = location.getY();
        this.aoOs = new Creature[0];
    }

    /**
     * Constructs a path consisting of the specified list of
     * coordinates in the specified area
     *
     * @param area
     * @param points               the list of x, y coordinates
     * @param attacksOfOpportunity the list of creatures getting AoOs against this path, with
     *                             one entry in the list per potential attack
     */

    public Path(Area area, List<Point> points, List<Creature> attacksOfOpportunity)
    {
        this.area = area;

        xCoords = new int[points.size()];
        yCoords = new int[points.size()];

        for (int i = 0; i < points.size(); i++) {
            xCoords[i] = points.get(i).x;
            yCoords[i] = points.get(i).y;
        }

        aoOs = new Creature[attacksOfOpportunity.size()];
        for (int i = 0; i < attacksOfOpportunity.size(); i++) {
            aoOs[i] = attacksOfOpportunity.get(i);
        }
    }

    private Path(Area area, int[] xCoords, int[] yCoords)
    {
        this.area = area;
        this.xCoords = xCoords;
        this.yCoords = yCoords;
        this.aoOs = new Creature[0];
    }

    /**
     * Returns the creature causing the attack for the specified AoO
     *
     * @param index the index in the AoOs list
     * @return the creature causing the attack
     */

    public Creature getAoOAttacker(int index)
    {
        return aoOs[index];
    }

    /**
     * Returns the number of AoOs that a parent creature following this
     * path will be subjected to, based on the faction of the creature
     * used in creating this path
     *
     * @return the number of AoOs associated with this path
     */

    public int getNumAoOs()
    {
        return aoOs.length;
    }

    /**
     * Returns the number of points in this path
     *
     * @return the number of points in this path
     */

    public int length()
    {
        return xCoords.length;
    }

    /**
     * Gets the location corresponding to the point at the specified index in
     * this path
     *
     * @param index the index to retrieve
     * @return the location at the given index
     */

    public Location get(int index)
    {
        return new Location(area, xCoords[index], yCoords[index]);
    }

    /**
     * Gets the movement bonus at the Area tile corresponding to the specified
     * index of this path
     *
     * @param index the index to retrieve
     * @return the area movement bonus at that point
     */

    public int getMovementBonus(int index)
    {
        return area.getMovementBonus(xCoords[index], yCoords[index]);
    }

    /**
     * Returns a Path consisting of the last n elements of this Path, where
     * n is at most maxLength. Note that AoOs associated with this path
     * are not transfered to the new path
     *
     * @param maxLength the maximum length of the Path to return
     * @return a Path with the beginning of this Path, up to maxLength
     */

    public Path truncate(int maxLength)
    {
        if (xCoords.length <= maxLength) {
            return this;
        }

        int[] xCoords = new int[maxLength];
        int[] yCoords = new int[maxLength];

        int offset = this.xCoords.length - maxLength;

        for (int i = this.xCoords.length - 1; i >= offset; i--) {
            xCoords[i - offset] = this.xCoords[i];
            yCoords[i - offset] = this.yCoords[i];
        }

        return new Path(this.area, xCoords, yCoords);
    }

    /**
     * Constructs a new Path which consists of this Path, with the specified
     * coordinates appended as a new starting path entry.  Note that AoOs associated
     * with this path are not transfered to the new path
     *
     * @param x
     * @param y
     * @return a new Path consisting of this Path with the specified coordinates
     * added to the end
     */

    public Path append(int x, int y)
    {
        int[] xCoords = new int[length() + 1];
        int[] yCoords = new int[length() + 1];

        for (int i = 0; i < length(); i++) {
            xCoords[i] = this.xCoords[i];
            yCoords[i] = this.yCoords[i];
        }

        xCoords[length()] = x;
        yCoords[length()] = y;

        return new Path(this.area, xCoords, yCoords);
    }

    @Override
    public Iterator<Location> iterator()
    {
        return new PathIterator();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append(area.getID());
        sb.append(" : { ");

        for (int i = xCoords.length - 1; i >= 0; i--) {
            sb.append("[");
            sb.append(xCoords[i]);
            sb.append(", ");
            sb.append(yCoords[i]);
            sb.append("]");

            if (i == 0) {
                sb.append(" ");
            } else {
                sb.append(", ");
            }
        }

        sb.append("}");

        return sb.toString();
    }

    private class PathIterator implements Iterator<Location>
    {
        private int index = 0;

        @Override
        public boolean hasNext()
        {
            return index < xCoords.length;
        }

        @Override
        public Location next()
        {
            index++;

            return get(index - 1);
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException("Paths are immutable.");
        }
    }
}
