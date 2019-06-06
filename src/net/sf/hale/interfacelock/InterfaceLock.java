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

package net.sf.hale.interfacelock;

import net.sf.hale.entity.Creature;

/**
 * An InterfaceLock is an Object that is added to the global InterfaceLocker (Game.interfaceLocker).
 * While any InterfaceLock is active, many actions by the player (such as moving, attacking, etc)
 * will not be possible.  This allows for the locking out of the interface during events where
 * player actions could cause problems, such as animations.
 * <p>
 * InterfaceLocks have a priority which they are sorted by.  Locks with a lower priority number
 * will be checked first by the InterfaceLocker when determining if a lock should be removed.
 * <p>
 * Each InterfaceLock has a parent Creature that it is associated with.  The parent Creature usually
 * indicates who did the action that was responsible for the creation of the InterfaceLock.
 *
 * @author Jared Stephen
 */

public class InterfaceLock implements Comparable< InterfaceLock >
{
    private long unlockTime;
    private Creature locker;
    private long curTime;
    private int priority;

    /**
     * Creates a new InterfaceLock with the specified parent lasting for the specified duration.
     *
     * @param locker   the parent Creature for this InterfaceLock.
     * @param duration the length of time that the InterfaceLock will be in effect, in
     *                 milliseconds
     */

    public InterfaceLock( Creature locker, long duration )
    {
        this.unlockTime = duration + System.currentTimeMillis( );
        this.locker = locker;
        this.priority = 0;
    }

    /**
     * Sets the current time for this InterfaceLocker.  This is used in
     * determining whether this lock is finished based on the start time.
     *
     * @param curTime the current time in milliseconds
     */

    public void setCurrentTime( long curTime ) { this.curTime = curTime; }


    /**
     * Sets the unlock time for this interface locker to the specified time.
     * This is used by some lockers to shorten or extend the interface lock
     * if some external event occurs.
     *
     * @param unlockTime the unlock time in milliseconds
     */

    public void setUnlockTime( long unlockTime ) { this.unlockTime = unlockTime; }

    /**
     * Returns true if and only if the current time for this InterfaceLock is greater than
     * the unlock time.  Once finished, an InterfaceLock is generally removed
     * from the InterfaceLocker.  This function is overloaded by child classes to perform
     * finish actions.
     *
     * @return true if and only if the InterfaceLocker is finished and should be removed
     */

    protected boolean checkFinished( )
    {
        return curTime >= unlockTime;
    }

    /**
     * Returns true if and only if the current time for this InterfaceLock is greater than
     * the unlock time.
     *
     * @return true if and only if the InterfaceLocker is finished and should be removed
     */

    public final boolean isFinished( )
    {
        return curTime >= unlockTime;
    }

    /**
     * Returns the parent Creature for this InterfaceLock
     *
     * @return the parent Creature for this InterfaceLock
     */

    public Creature getLocker( ) { return locker; }

    @Override
    public String toString( ) { return locker + " locked with unlock time: " + unlockTime; }

    /**
     * Returns the priority for this InterfaceLock.  InterfaceLocks are sorted by priority.  Locks
     * with a lower priority will be checked for finishing first.
     *
     * @return the priority for this InterfaceLock
     */

    public int getPriority( ) { return priority; }

    @Override
    public int compareTo( InterfaceLock other )
    {
        if ( this.getPriority( ) == other.getPriority( ) )
        { return this.hashCode( ) - other.hashCode( ); }

        return this.getPriority( ) - other.getPriority( );
    }
}
