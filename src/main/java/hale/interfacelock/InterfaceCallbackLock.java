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

package hale.interfacelock;

import java.util.ArrayList;
import java.util.List;

import hale.entity.Creature;

/**
 * An {@link InterfaceLock} that calls zero, one, or more callbacks upon
 * finishing.  The callbacks can be any object implementing the Runnable interface.
 *
 * @author Jared Stephen
 */

public class InterfaceCallbackLock extends InterfaceLock
{
    private int priority;
    private final List<Runnable> callbacks;

    /**
     * Creates a new InterfaceLock with the specified parent lasting for the specified duration.
     *
     * @param locker   the parent Creature for this InterfaceLock.
     * @param duration the length of time that the InterfaceLock will be in effect, in
     *                 milliseconds
     */

    public InterfaceCallbackLock(Creature locker, long duration)
    {
        super(locker, duration);

        priority = 40;
        callbacks = new ArrayList<Runnable>();
    }

    /**
     * Sets the unlock priority of this InterfaceLock.  Lower priorities are
     * unlocked first.
     *
     * @param priority the unlock priority.
     */

    public void setPriority(int priority)
    {
        this.priority = priority;
    }

    /**
     * Add the specified callback to the list of callbacks that will be run when
     * this InterfaceLock finishes.
     *
     * @param callback the Runnable callback to add
     */

    public void addCallback(Runnable callback)
    {
        callbacks.add(callback);
    }

    /**
     * Adds all of the specified callbacks in the list to the callbacks that will
     * be run when this InterfaceLock finishes
     *
     * @param callbacks the list of callbacks to add
     */

    public void addCallbacks(List<Runnable> callbacks)
    {
        for (Runnable callback : callbacks) {
            this.callbacks.add(callback);
        }
    }

    @Override
    protected boolean checkFinished()
    {
        if (super.checkFinished()) {

            runCallbacks();

            return true;
        } else {
            return false;
        }
    }

    private void runCallbacks()
    {
        for (Runnable callback : callbacks) {
            callback.run();
        }
    }

    @Override
    public int getPriority()
    {
        return priority;
    }
}
