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

package hale.defaultability;

import hale.Game;
import hale.area.Transition;
import hale.entity.Creature;
import hale.entity.Location;
import hale.entity.PC;
import hale.interfacelock.InterfaceCallbackLock;

/**
 * Default ability for activating an area transition and traveling to a new area.  If the
 * parent is not adjacent to the area transition, the parent is first moved.
 *
 * @author Jared Stephen
 */

public class Travel implements DefaultAbility
{
    // storage for movement properties if movement is needed
    private Move move;
    private Transition target;

    @Override
    public String getActionName()
    {
        return "Travel";
    }

    @Override
    public boolean canActivate(PC parent, Location targetPosition)
    {
        target = targetPosition.getAreaTransition();

        // if target is an active (visible) area transition
        if (target != null && target.isActivated()) {
            move = new Move();

            // allow any member of the player character party to travel
            for (PC character : Game.curCampaign.party) {
                if (targetPosition.getDistance(character) <= 1) {
                    return true;
                }
            }

            // need to move towards the transition before traveling
            return move.canMove(parent, targetPosition, 1);
        }

        return false;
    }

    @Override
    public void activate(PC parent, Location targetPosition)
    {
        boolean needToMove = true;

        // allow any member of the player character party to travel
        for (PC character : Game.curCampaign.party) {
            if (targetPosition.getDistance(character) <= 1) {
                needToMove = false;
                break;
            }
        }

        if (needToMove) {
            // move towards the transition then travel
            move.addCallback(new TravelCallback(parent, targetPosition));
            move.moveTowards(parent, targetPosition, 1);
        } else {
            Game.curCampaign.transition(target, false);
        }

        Game.areaListener.computeMouseState();
    }

    @Override
    public DefaultAbility getInstance()
    {
        return new Travel();
    }

    /*
     * Callback used to Travel after movement
     */

    private class TravelCallback implements Runnable
    {
        private Creature parent;
        private Location targetPosition;
        private boolean alreadyLocked;

        private TravelCallback(Creature parent, Location targetPosition)
        {
            this.parent = parent;
            this.targetPosition = targetPosition;
        }

        @Override
        public void run()
        {
            // if the interface is currently locked (from outstanding movement for example,
            // wait until it unlocks and then call this callback again
            // only do this at most once
            if (!alreadyLocked && Game.interfaceLocker.locked()) {
                InterfaceCallbackLock lock = new InterfaceCallbackLock(parent, Game.config.getCombatDelay());
                lock.addCallback(this);
                Game.interfaceLocker.add(lock);
                alreadyLocked = true;
                return;
            }

            if (targetPosition.getDistance(parent) <= 1) {
                Game.curCampaign.transition(target, false);
            }
        }
    }

}
