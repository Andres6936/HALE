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

package hale;

import java.util.LinkedList;

import hale.*;
import hale.ability.AbilitySlot;
import hale.ability.Targeter;

/**
 * Class for controlling the currently active Targeter and maintaining the
 * queued list of Targeters.
 *
 * @author Jared Stephen
 */

public class TargeterManager
{

    private Targeter currentTargeter;
    private LinkedList<Targeter> queuedTargeters;

    /**
     * Creates a new TargeterManager with no current Targeters running
     */

    public TargeterManager()
    {
        queuedTargeters = new LinkedList<Targeter>();
    }

    /**
     * Returns true if the interface is currently controlled by a Targeter,
     * false otherwise
     *
     * @return whether the interface is currently controlled by a Targeter
     */

    public boolean isInTargetMode()
    {
        return currentTargeter != null;
    }

    /**
     * If the interface is currently not controlled by a Targeter, the specified
     * Targeter is added as the current Targeter and gains control of the interface.
     * <p>
     * Otherwise, the targeter is added to the queue to gain control when it becomes
     * available.
     *
     * @param targeter the Targeter to add
     */

    public void addTargeter(Targeter targeter)
    {
        // check if a targeter has already been added for this targeter's slot
        // don't allow two targeters for the same slot
        AbilitySlot slot = targeter.getSlot();
        if (slot != null && isInTargetMode()) {
            if (slot == currentTargeter.getSlot()) return;

            for (Targeter t : queuedTargeters) {
                if (slot == t.getSlot()) return;
            }
        }

        if (isInTargetMode()) {
            queuedTargeters.add(targeter);
        } else {
            setTargeter(targeter);
        }
    }

    /**
     * Checks if the current targeter has a valid selection of targets.  If so,
     * exits the current targeter and calls its callback.  If there is another
     * targeter in the queue, the first in queue becomes the new targeter.  If
     * there are no targeters in the queue, then the interface exits target mode.
     */

    public void endCurrentTargeter()
    {
        if (currentTargeter == null) return;

        if (!currentTargeter.hasSelectedTargets()) return;

        nextTargeter();
    }

    /**
     * Checks if the current targeter is cancelable.  If so, exits the current
     * targeter without calling its callback.  If there is another targeter in
     * the queue, the first in line becomes the new targeter.  Otherwise, the
     * interface will exit target mode.
     */

    public void cancelCurrentTargeter()
    {
        if (currentTargeter == null) return;

        if (!currentTargeter.isCancelable()) return;

        nextTargeter();
    }

    /**
     * Checks if the current targeter is valid.  If it is not, attempts to cancel
     * it
     */

    public void checkCurrentTargeter()
    {
        if (currentTargeter != null && !currentTargeter.checkValid()) {
            cancelCurrentTargeter();
        }
    }

    private void nextTargeter()
    {
        while (true) {
            if (queuedTargeters.size() > 0) {
                boolean setSuccess = setTargeter(queuedTargeters.getFirst());

                queuedTargeters.removeFirst();
                // if the targeter was set, we are done
                if (setSuccess) break;
            } else {
                setTargeter(null);

                synchronized (this) {
                    this.notifyAll();
                }

                Game.areaListener.computeMouseState();

                break;
            }
        }
    }

    private boolean setTargeter(Targeter targeter)
    {
        if (targeter == null) {
            currentTargeter = null;
        } else {

            // check if the targeter is still valid to be set
            if (!targeter.checkValid()) return false;

            currentTargeter = targeter;

            targeter.setMousePosition(Game.areaListener.getLastMouseX(),
                    Game.areaListener.getLastMouseY(), Game.mainViewer.getMouseGridPoint());
        }

        Game.mainViewer.updateInterface();
        Game.areaListener.computeMouseState();

        return true;
    }

    /**
     * Returns the currently active targeter.  Returns null if there is no
     * active targeter.
     *
     * @return the currently active targeter.
     */

    public Targeter getCurrentTargeter()
    {
        return currentTargeter;
    }
}
