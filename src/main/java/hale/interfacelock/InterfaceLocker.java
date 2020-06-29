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

package main.java.hale.interfacelock;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import main.java.hale.Game;
import main.java.hale.entity.Creature;
import main.java.hale.entity.Path;

/**
 * The global class responsible for locking the interface, preventing player
 * ui interactions while other events are playing, such as during an animation,
 * movement, or while the AI is active.
 * <p>
 * Most control over this class occurs asynchronously.  The {@link #checkTime(long)}
 * method will update the internal state and needs to be called at regular intervals
 * for this class to work properly
 *
 * @author Jared Stephen
 */

public class InterfaceLocker
{
    private final MovementHandler movementHandler;

    private boolean interfaceLocked;
    private final TreeSet<InterfaceLock> locks;
    private final List<InterfaceLock> queuedLocks;
    private Iterator<InterfaceLock> iter = null;

    /**
     * Initializes this InterfaceLocker to a clean, unlocked state.
     */

    public InterfaceLocker()
    {
        locks = new TreeSet<>();
        queuedLocks = new LinkedList<>();
        interfaceLocked = false;

        movementHandler = new MovementHandler();
    }

    /**
     * Returns the current movement mode for moves
     *
     * @return the current movement mode
     */

    public MovementHandler.Mode getMovementMode()
    {
        return movementHandler.getMovementMode();
    }

    /**
     * Sets the current movement mode to the specified mode
     *
     * @param mode the mode to set
     */

    public void setMovementMode(MovementHandler.Mode mode)
    {
        movementHandler.setMovementMode(mode);
    }

    /**
     * Removes all InterfaceLocks currently in this InterfaceLocker.  After this
     * method returns, the interface will be unlocked
     */

    public void clear()
    {
        locks.clear();
        queuedLocks.clear();
        interfaceLocked = false;
        movementHandler.clear();
        movementHandler.setMovementMode(MovementHandler.Mode.Party);
    }

    /**
     * Interrupts and cancels all current movement
     */

    public void interruptMovement()
    {
        movementHandler.interrupt();
    }

    /**
     * Adds a movement path for the specified creature to be completed
     * by the movement handler
     *
     * @param mover the creature that is moving
     * @param path  the path that the creature is moving
     * @return the move created for this movement
     */

    public MovementHandler.Mover addMove(Creature mover, Path path, boolean provokeAoOs)
    {
        interfaceLocked = true;

        return movementHandler.addMove(mover, path, provokeAoOs);
    }

    /**
     * Returns true if and only if the list of current active InterfaceLocks contains
     * the specified lock
     *
     * @param lock the lock to search for
     * @return whether the specified lock is currently in the active list
     */

    public boolean hasActiveLock(InterfaceLock lock)
    {
        return locks.contains(lock);
    }

    /**
     * Returns true if there is at least one active lock and the the interface is locked,
     * false otherwise
     *
     * @return true if and only if the interface is locked
     */

    public boolean locked()
    {
        return interfaceLocked;
    }

    /**
     * Adds the specified InterfaceLock to the list of Locks that are active.  This lock
     * will be queried on future {@link #checkTime(long)} calls
     * Note that the lock will not actually be set to active until the next checkTime() call.
     *
     * @param interfaceLock the InterfaceLock to add
     */

    public void add(InterfaceLock interfaceLock)
    {
        synchronized (queuedLocks) {
            queuedLocks.add(interfaceLock);
        }

        if (iter == null) addQueued();
    }

    private void addQueued()
    {
        if (queuedLocks.size() > 0) {
            synchronized (queuedLocks) {
                Iterator<InterfaceLock> iter = queuedLocks.iterator();
                while (iter.hasNext()) {
                    InterfaceLock lock = iter.next();

                    locks.add(lock);
                    iter.remove();
                }
            }

            interfaceLocked = true;

            // recompute mouse state including check pathing
            Game.areaListener.computeMouseState();
            Game.mainViewer.updateInterface();
        }
    }

    /**
     * Tells this InterfaceLocker to update the currentTime to the specified time.  All changes
     * in state from outstanding calls to {@link #add(InterfaceLock)} are applied.  Active locks
     * supplied the specified current time.  Any locks that have finished are removed from the
     * list of active locks.  Finally, if there are no active locks left after performing all
     * updates, the interface is unlocked.
     * <p>
     * Note that all active InterfaceLocks may not be removed from one call to this method.
     * This is because the method only performs time updates to locks with the current highest
     * priority (@link {@link InterfaceLock#getPriority()}).  Once all locks of a given priority
     * have been removed, the next call to checkTime() will then query locks of the next lower
     * priority, and so on.
     *
     * @param curTime the current time to pass to each active lock
     */

    public void checkTime(long curTime)
    {
        if (locks.size() > 0 && !movementHandler.isLocked()) {
            // only pop locks after the movement handler is unlocked

            int priority = locks.first().getPriority();

            iter = locks.iterator();
            while (iter.hasNext()) {
                InterfaceLock current = iter.next();
                if (current.getPriority() != priority) break;

                current.setCurrentTime(curTime);
                if (current.checkFinished()) {
                    synchronized (current) {
                        current.notifyAll();
                    }
                    iter.remove();
                }
            }
        }

        addQueued();

        movementHandler.update(curTime);

        checkUnlockInterface();
    }

    private void checkUnlockInterface()
    {
        if (interfaceLocked && locks.size() == 0 && !movementHandler.isLocked()) {
            interfaceLocked = false;

            synchronized (this) {
                this.notifyAll();
            }

            // recompute mouse state including check pathing
            Game.areaListener.computeMouseState();
            Game.mainViewer.updateInterface();
        }
    }
}
