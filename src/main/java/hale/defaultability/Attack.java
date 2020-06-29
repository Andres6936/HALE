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
import hale.entity.Creature;
import hale.entity.Location;
import hale.entity.PC;
import hale.entity.Weapon;

/**
 * A default ability for using a standard attack against an opponent.  If the
 * opponent is not within reach, the parent is first moved towards the hostile
 *
 * @author Jared Stephen
 */

public class Attack implements DefaultAbility
{
    private Move move;
    private Creature target;

    @Override
    public String getActionName()
    {
        return "Attack";
    }

    @Override
    public boolean canActivate(PC parent, Location targetPosition)
    {
        if (!parent.timer.canAttack()) return false;

        if (parent.getLocation().equals(targetPosition)) return false;

        target = targetPosition.getCreature();

        if (target != null && parent.getFaction().isHostile(target)) {
            move = new Move();

            if (parent.canAttack(targetPosition)) {
                return true;
            } else {
                Weapon weapon = parent.getMainHandWeapon();
                if (weapon.isMelee()) {
                    return move.canMove(parent, targetPosition, weapon.getTemplate().getMaxRange());
                }
            }
        }

        return false;
    }

    @Override
    public void activate(PC parent, Location targetPosition)
    {
        if (parent.canAttack(targetPosition)) {
            Game.areaListener.getCombatRunner().creatureStandardAttack(parent, target);
        } else {
            Weapon weapon = parent.getMainHandWeapon();

            if (weapon.isMelee()) {
                move.addCallback(new AttackCallback(parent));
                move.moveTowards(parent, targetPosition, weapon.getTemplate().getMaxRange());
            }
        }

        Game.areaListener.computeMouseState();
    }

    @Override
    public DefaultAbility getInstance()
    {
        return new Attack();
    }

    /*
     * Callback used for Attacking after movement
     */

    private class AttackCallback implements Runnable
    {
        private Creature parent;

        private AttackCallback(Creature parent)
        {
            this.parent = parent;
        }

        @Override
        public void run()
        {
            if (parent.canAttack(target.getLocation())) {
                Game.areaListener.getCombatRunner().creatureStandardAttack(parent, target);
            }
        }
    }
}
