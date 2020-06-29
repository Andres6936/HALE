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

package main.java.hale.defaultability;

import main.java.hale.Game;
import main.java.hale.entity.Creature;
import main.java.hale.entity.Location;
import main.java.hale.entity.PC;

/**
 * A default ability for talking to a creature.  If the parent is not within range,
 * the parent is first moved.
 *
 * @author Jared Stephen
 */

public class Talk implements DefaultAbility
{
    // storage for movement properties if movement is needed
    private Move move;
    private Creature target;

    @Override
    public String getActionName()
    {
        return "Talk";
    }

    @Override
    public boolean canActivate(PC parent, Location targetPosition)
    {
        if (Game.isInTurnMode()) return false;

        if (!parent.hasVisibility(targetPosition)) return false;

        target = targetPosition.getCreature();

        // if target is a valid conversation target
        if (target != null && target.getTemplate().getConversation() != null) {
            move = new Move();

            if (targetPosition.getDistance(parent) > 3) {
                // need to move towards the target before talking
                return move.canMove(parent, targetPosition, 3);
            }

            return true;
        }

        return false;
    }

    @Override
    public void activate(PC parent, Location targetPosition)
    {
        if (targetPosition.getDistance(parent) > 3) {
            // move towards the target then talk
            move.addCallback(new TalkCallback(parent));
            move.moveTowards(parent, targetPosition, 3);
        } else {
            target.startConversation(parent);
        }

        Game.areaListener.computeMouseState();
    }

    @Override
    public DefaultAbility getInstance()
    {
        return new Talk();
    }

    /*
     * Callback that is used to start a conversation after movement
     */

    private class TalkCallback implements Runnable
    {
        private PC parent;

        private TalkCallback(PC parent)
        {
            this.parent = parent;
        }

        @Override
        public void run()
        {
            if (parent.getLocation().getDistance(target) > 3) return;
            if (!parent.hasVisibility(target.getLocation())) return;

            target.startConversation(parent);
        }
    }
}
