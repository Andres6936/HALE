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

import de.matthiasmann.twl.Color;
import hale.Game;
import hale.bonus.Bonus;
import hale.entity.Creature;
import hale.entity.Location;
import hale.entity.PC;
import hale.entity.Trap;

/**
 * A default ability for disarming a trap in the area.  If the parent is not currently
 * adjacent to the trap, the parent is first moved.  This default ability can only be used by parents with
 * Bonus.Type.TrapHandling.
 *
 * @author Jared Stephen
 */

public class DisarmTrap implements DefaultAbility
{
    private Move move;
    private Trap trap;

    @Override
    public String getActionName()
    {
        return "Disarm Trap";
    }

    @Override
    public boolean canActivate(PC parent, Location targetPosition)
    {
        if (!parent.timer.canPerformAction("DisarmTrapCost")) return false;

        if (!parent.stats.has(Bonus.Type.TrapHandling)) return false;

        trap = targetPosition.getTrap();
        if (trap == null || !trap.isSpotted()) return false;

        move = new Move();
        move.setAllowPartyMove(false);

        if (targetPosition.getDistance(parent) > 1) {
            // need to move towards the trap before disarming
            return move.canMove(parent, targetPosition, 1);
        }

        return true;
    }

    @Override
    public void activate(PC parent, Location targetPosition)
    {
        if (targetPosition.getDistance(parent) > 1) {
            // move towards the trap then disarm
            move.addCallback(new DisarmCallback(parent));
            move.moveTowards(parent, targetPosition, 1);
        } else {
            disarm(parent, trap);
        }

        Game.areaListener.computeMouseState();
    }

    /**
     * The specified Creature will attempt to disarm the trap.
     * <p>
     * The Creature's AP is decreased by "disarmTrapCost".
     *
     * @param parent the Creature trying to disarm the trap
     * @param trap   the trap to be recovered
     * @return true if the trap was disarmed, false if it was not
     * for any reason
     */

    public boolean disarm(Creature parent, Trap trap)
    {
        if (trap == null) return false;

        if (trap.getLocation().getDistance(parent) > 1) return false;

        if (!parent.timer.canPerformAction("DisarmTrapCost")) return false;

        if (!parent.stats.has(Bonus.Type.TrapHandling)) return false;

        parent.timer.performAction("DisarmTrapCost");

        boolean isDisarmed = trap.attemptDisarm(parent);

        if (!isDisarmed) {
            Game.mainViewer.addFadeAway("Disarm Trap Failed", trap.getLocation().getX(), trap.getLocation().getY(),
                    new Color(0xFFAbA9A9));
        } else {
            Game.mainViewer.addFadeAway("Trap Disarmed", trap.getLocation().getX(), trap.getLocation().getY(),
                    new Color(0xFFAbA9A9));
        }

        return isDisarmed;
    }

    @Override
    public DefaultAbility getInstance()
    {
        return new DisarmTrap();
    }

    private class DisarmCallback implements Runnable
    {
        private Creature parent;

        private DisarmCallback(Creature parent)
        {
            this.parent = parent;
        }

        @Override
        public void run()
        {
            disarm(parent, trap);
        }
    }
}
