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

package main.java.hale.entity;

import main.java.hale.Game;
import main.java.hale.ability.Ability;
import main.java.hale.bonus.Bonus;
import main.java.hale.bonus.Stat;

/**
 * A class for keeping track of the number of Action Points (AP).  Note that the
 * action points displayed on screen are equal to the internally stored action
 * points divided by 100.  This gives improved precision to the internally stored
 * action points
 * available to a given creature
 *
 * @author Jared
 */

public class RoundTimer
{
    private boolean isInFreeMode = false;
    private boolean active = false;
    private boolean waitedOnce = false;

    private int AP;
    private int maxAP;

    private final Creature parent;

    /**
     * Creates a new RoundTimer with the specified creature as parent
     *
     * @param parent the parent creature
     */

    public RoundTimer(Creature parent)
    {
        this.parent = parent;
    }

    /**
     * Sets whether this round timer is in "free mode".  While in free mode, no
     * AP is deducted from this round timer and calls to {@link #canMove(Path)},
     * {@link #canAttack()}, {@link #canPerformAction(int)} etc will all
     * return true
     *
     * @param freeMode true to set free mode, false to unset it
     */

    public void setFreeMode(boolean freeMode)
    {
        this.isInFreeMode = freeMode;
    }

    /**
     * Returns true if the creature associated with this RoundTimer has taken
     * any action costing AP this round, false otherwise
     *
     * @return whether the creature has used AP
     */

    public boolean hasTakenAnAction()
    {
        return (AP < maxAP);
    }

    /**
     * Called each time a new round is started.  The Creature's maximum AP is computed
     * based on their stats, and the current AP is set to this maximum.  This
     * RoundTimer is set as active
     */

    public void reset()
    {
        active = true;

        maxAP = Math.max(0, Game.ruleset.getValue("BaseActionPoints") + parent.stats.get(Bonus.Type.ActionPoint) * 100);

        AP = maxAP;

        parent.updateListeners();
    }

    private int getCost(Path path, int index)
    {
        int bonus = path.getMovementBonus(index);

        // immobilization immunity protects against slowing effects
        if (bonus < 0 && parent.stats.has(Bonus.Type.ImmobilizationImmunity)) {
            bonus = 0;
        }

        return parent.stats.get(Stat.MovementCost) * (100 - bonus) / 100;
    }

    /**
     * Returns the number of steps in the specified Path that the parent creature
     * for this Timer is currently able to move.  While not in combat mode, there is
     * no limit so this returns Integer.MAX_VALUE
     *
     * @param path
     * @return the number of steps in the Path that the parent creature is able to move
     */

    public int getLengthCurrentlyMovable(Path path)
    {
        if (!Game.isInTurnMode()) {
            return Integer.MAX_VALUE;
        }

        int total = 0;

        for (int i = 0; i < path.length(); i++) {
            total += getCost(path, i);

            if (total > AP) return i;
        }

        return path.length();
    }

    /**
     * Returns the amount of AP that this timer had at the start of the current round for
     * the parent creature
     *
     * @return the max AP
     */

    public int getMaxAP()
    {
        return maxAP;
    }

    /**
     * Gets the cost in AP to move along the specific path for the creature
     * associated with this RoundTimer
     *
     * @param path the path to move along
     * @return the AP cost
     */

    public int getMovementCost(Path path)
    {
        int total = 0;

        for (int i = 0; i < path.length(); i++) {
            total += getCost(path, i);
        }

        return total;
    }

    /**
     * Returns true if the parent for this roundTimer can move along the specified path,
     * false otherwise.  The parent must not be immobilized and must have sufficient AP
     * to perform the move
     *
     * @param path the path to check
     * @return whether the parent can move along the path
     */

    public boolean canMove(Path path)
    {
        if (!Game.isInTurnMode()) return true;

        if (parent.stats.isImmobilized()) return false;

        if (isInFreeMode) return true;

        return getMovementCost(path) <= AP;
    }

    /**
     * Returns the current amount of AP remaining this round in this RoundTimer
     *
     * @return the amount of AP
     */

    public int getAP()
    {
        return this.AP;
    }

    /**
     * Returns true if the parent has sufficient AP to attack, false otherwise
     *
     * @return whether the parent has enough AP to attack
     */

    public boolean canAttack()
    {
        return canPerformAction(parent.stats.get(Stat.AttackCost));
    }

    /**
     * Deducts the amount of AP needed to perform a standard attack from the
     * AP counter in this RoundTimer
     *
     * @return true if there was enough AP to perform the attack, false otherwise
     */

    public boolean performAttack()
    {
        return performAction(parent.stats.get(Stat.AttackCost));
    }

    /**
     * Returns true if the parent has enough AP to perform the action with the
     * specified rules string, false otherwise
     *
     * @param actionText the rules string defined in the rules data file
     * @return whether the parent has enough AP to perform the action
     */

    public boolean canPerformAction(String actionText)
    {
        return canPerformAction(Game.ruleset.getValue(actionText));
    }

    /**
     * Returns true if the parent has sufficient AP to perform an action with the
     * specified cost, false otherwise
     *
     * @param cost the cost in Action Points (AP)
     * @return whether the parent has enough AP to perform the action
     */

    public boolean canPerformAction(int cost)
    {
        if (parent.stats.isHelpless()) return false;

        if (this.AP < cost && !isInFreeMode) return false;

        return true;
    }

    /**
     * Deducts the amount of AP to perform the action with the specified rules string
     *
     * @param actionText the rules string defined in the rules data file
     * @return whether the parent has enough AP to perform the action
     */

    public boolean performAction(String actionText)
    {
        return performAction(Game.ruleset.getValue(actionText));
    }

    /**
     * Deducts the specified quantity of AP from this RoundTimer
     *
     * @param AP the cost in Action Points (AP)
     * @return whether the parent has enough AP to perform the action
     */

    public boolean performAction(int AP)
    {
        if (!canPerformAction(AP)) return false;

        // only deduct AP if game is in turn based mode
        if (Game.isInTurnMode() && !isInFreeMode) {
            this.AP -= AP;
            parent.updateListeners();
        }

        return true;
    }

    /**
     * Returns true if the parent has enough action points to activate the
     * specified ability.  Note that this method is only relevant for abilities
     * that state a fixed action point cost in their data files.  Some abilities
     * have variable action point costs.
     *
     * @param abilityID the ruleset ID of the ability to check
     * @return whether the parent has enough AP to activate the ability
     */

    public boolean canActivateAbility(String abilityID)
    {
        Ability ability = Game.ruleset.getAbility(abilityID);

        return canPerformAction(ability.getAPCost());
    }

    private int getCostToEquipItem(EquippableItem item)
    {
        switch (item.getTemplate().getType()) {
            case Weapon:
            case Shield:
                return Game.ruleset.getValue("EquipItemCost") * (100 - parent.stats.get(Bonus.Type.ActionPointEquipHands)) / 100;
            case Armor:
                return Game.ruleset.getValue("EquipArmorCost");
            default:
                return Game.ruleset.getValue("EquipItemCost");
        }
    }

    public boolean canPerformEquipAction(EquippableItem item)
    {
        return canPerformAction(getCostToEquipItem(item));
    }

    public boolean performEquipAction(EquippableItem item)
    {
        return performAction(this.getCostToEquipItem(item));
    }

    /**
     * Returns the total number of feet the parent can move, assuming no
     * area movement effects.  1 tile = 5 feet
     *
     * @return the number of movement points remaining
     */

    public int getMovementLeft()
    {
        int cost = parent.stats.get(Stat.MovementCost);
        int finalCost = cost / 100;

        return 5 * AP / finalCost;
    }

    /**
     * Deducts AP to move the parent along a path with a single location in it
     *
     * @param location the location to move to
     * @return true if the parent has sufficient AP to perform the move, false otherwise
     */

    public boolean move(Location location)
    {
        return move(new Path(location));
    }

    /**
     * Deducts AP to move the parent along the specified path
     *
     * @param path the path to move along
     * @return true if the parent has sufficient AP to perform the move, false otherwise
     */

    public boolean move(Path path)
    {
        if (!canMove(path)) {
            return false;
        }

        // only deduct AP in turn based mode
        if (Game.isInTurnMode() && !isInFreeMode) {
            AP -= getMovementCost(path);
            parent.updateListeners();
        }

        return true;
    }

    /**
     * Ends the active turn of the parent and sets the remaining AP to 0.
     */

    public void endTurn()
    {
        active = false;
        AP = 0;
        waitedOnce = false;
        parent.updateListeners();
    }

    /**
     * Waiting the current turn of the parent and sets the remaining AP to 0.
     */

    public void waitTurn()
    {
        active = false;
        AP = 0;
        waitedOnce = true;
        parent.updateListeners();
    }

    /**
     * Returns true if the parent associated with this RoundTimer is currently in
     * their turn, false otherwise
     *
     * @return whether the parent is active (in their turn)
     */

    public boolean isActive()
    {
        return active;
    }

    /**
     * Returns true if the currend Creature (player) has waited once this round.<br>
     * Allow to skip the elapsed round step.
     *
     * @return whether the parent waited once (in their turn)
     */

    public boolean isWaitedOnce()
    {
        return waitedOnce;
    }
}