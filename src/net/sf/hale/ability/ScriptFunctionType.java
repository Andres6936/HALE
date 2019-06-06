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

package net.sf.hale.ability;

/**
 * An enum representing all the different types of Script functions.  Each
 * function type is called in a different situation.  For example "onApply" is called
 * when an Ability is added to a Creature or Item.  "onHit" is applied whenever
 * a Creature is hit with a weapon.
 * <p>
 * Each object's String representation is the JavaScript function that should be present
 * for a Script to call the specified type.
 *
 * @author Jared Stephen
 */

public enum ScriptFunctionType
{
    /**
     * Function called when it is a given Creature's turn for their AI script
     */
    runTurn,

    /**
     * Called in a Creature's AI script.  Returns true if the creature should take the AoO, false otherwise
     */
    takeAttackOfOpportunity,

    /**
     * Called in a Creature's AI script when the creature is killed
     */
    onCreatureDeath,

    /**
     * Function called when Ability or Effect is added to a Creature or Item
     */
    onApply,

    /**
     * Function called when Ability or Effect is removed from a Creature or Item
     */
    onRemove,

    /**
     * Function that is defined for some abilities that can be used by scripts to validate targets
     */
    isTargetValid,

    /**
     * Function called when Targeter finishes
     */
    onTargetSelect,

    /**
     * Function called when determining if Ability can be activated by player
     */
    canActivate,

    /**
     * Function called when Ability is activated by the player
     */
    onActivate,

    /**
     * Function called when reactivating auras that were canceled when going through an area transition
     */
    onReactivate,

    /**
     * Function called when an Ability is deactivated by the player
     */
    onDeactivate,

    /**
     * Function called when a Creature enters the affected region of an Effect
     */
    onTargetEnter,

    /**
     * Function called when a Creature exits the affected region of an Effect
     */
    onTargetExit,

    /**
     * Called when checking if an item can be used
     */
    canUse,

    /**
     * Called when an item is used
     */
    onUse,

    /**
     * Function called when a trap is sprung by a Creature
     */
    onSpringTrap,

    /**
     * Function called when a creature fails a trap reflex check
     */
    onTrapReflexFailed,

    /**
     * Called when a door is closed by a Creature
     */
    onClose,

    /**
     * Called when a door or container is opened by a Creature
     */
    onOpen,

    /**
     * Called when a door or container is unlocked, either with a key or through picking the lock
     */
    onUnlock,

    /**
     * Called when the area associated with a Trigger is loaded for the first time
     */
    onAreaLoadFirstTime,

    /**
     * Called each time the area associated with a Trigger is loaded
     */
    onAreaLoad,

    /**
     * Called each time the area associated with a Trigger is exited
     */
    onAreaExit,

    /**
     * Called the first time a player character enters a Point associated with a Trigger
     */
    onPlayerEnterFirstTime,

    /**
     * Called each time a player character enters a Point associated with a Trigger
     */
    onPlayerEnter,

    /**
     * Called each time a player exits a Trigger
     */
    onPlayerExit,

    /**
     * Called whenever an ability is activated on a creature.  This function is called
     * within the scripts of any effects applied to the parent
     */
    onAbilityActivated,

    /**
     * Called for all effects applied to the attacker whenever an attack is made before computing if it hit
     * Also called on the attacking item's script
     **/
    onAttack,

    /**
     * Called for all effects applied to the defender whenever an attack is made before computing if it hit
     */
    onDefense,

    /**
     * Called for effects applied to the attacker whenever an attack hits after damage has been applied
     * Also called on the attacking item's script
     **/
    onAttackHit,

    /**
     * Called for effects applied to the defender whenever an attack hits after damage has been applied
     */
    onDefenseHit,

    /**
     * Called for effects applied to a creature whenever that creature is damaged
     */
    onDamaged,

    /**
     * Called each time an effect elapses a round
     */
    onRoundElapsed,

    /**
     * Called on an item's script whenever it is added to an inventory
     */
    onAddItem,

    /**
     * Called on an item's script whenever it is equipped by a creature
     */
    onEquipItem,

    /**
     * Called on an item's script whenever it is unequipped by a creature
     */
    onUnequipItem,

    /**
     * Called on a creature's conversation script when starting a conversation
     */
    startConversation,

    /**
     * Called on all effects whenever a new effect is applied to a creature
     */
    onEffectApplied,

    /**
     * AI helper used to determine if it is valid / useful to use an ability on a given target
     */
    aiCheckTargetValid;
}
