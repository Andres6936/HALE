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

package hale.characterbuilder;

import java.util.LinkedList;
import java.util.List;

/**
 * A Model that implements a shared pool of points that can be allocated to
 * several possible clients.  Keeps track of the list of clients and tells them
 * how many points are remaining.
 *
 * @author Jared Stephen
 */

public class PointAllocatorModel
{
    private List<Listener> listeners;
    private double pointsRemaining;

    /**
     * Create a new PointsAllocaterModel with the specified number of total
     * points to allocate.
     *
     * @param pointsRemaining the total number of points to allocate
     */

    public PointAllocatorModel(int pointsRemaining)
    {
        this.pointsRemaining = pointsRemaining;

        this.listeners = new LinkedList<Listener>();
    }

    /**
     * Sets the total number of allocatable points in this PointAllocator
     * to the specified value.
     *
     * @param points the total number of allocatable points to be set
     */

    public void setPointsRemaining(int points)
    {
        this.pointsRemaining = points;
    }

    /**
     * Adds the specified Listener to the list of Listeners
     * that is notified when the number of allocated points changes
     *
     * @param listener the listener to add
     */

    public void addListener(Listener listener)
    {
        listeners.add(listener);
    }

    /**
     * Removes the specified Listener from the List of Listeners that
     * are notified when the number of allocated points changes.  If the
     * listener is not present in that list, nothing is done.
     *
     * @param listener the listener to remove
     */

    public void removeListener(Listener listener)
    {
        listeners.remove(listener);
    }

    /**
     * Allocates the specified number of points.  The number of available
     * points is decreased by this amount.  If points is negative, the number
     * of points is instead unallocated by the specified amount.
     *
     * @param points the number of points to allocate
     */

    public void allocatePoints(double points)
    {
        pointsRemaining -= points;

        for (Listener listener : listeners) {
            listener.allocatorModelUpdated();
        }
    }

    /**
     * Returns the number of points that have yet to be allocated
     *
     * @return the number of points that have yet to be allocated
     */

    public double getRemainingPoints()
    {
        return pointsRemaining;
    }

    /**
     * The interface for an object that wants to be notified whenever the number
     * of allocated points has changed.
     *
     * @author Jared Stephen
     */

    public interface Listener
    {
        /**
         * Method called whenever the number of allocated points has changed.
         */
        public void allocatorModelUpdated();
    }
}
