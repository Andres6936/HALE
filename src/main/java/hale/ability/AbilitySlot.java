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

package hale.ability;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import hale.Game;
import hale.entity.Creature;
import hale.entity.NPC;
import hale.loading.JSONOrderedObject;
import hale.loading.ReferenceHandler;
import hale.loading.Saveable;
import hale.util.SaveGameUtil;
import hale.util.SimpleJSONArrayEntry;
import hale.util.SimpleJSONObject;

/**
 * Each Creature will have a number of AbilitySlots which will vary
 * depending on the Creature's race, role, and level.
 * <p>
 * Each AbilitySlot will accept one specific type of {@link Ability}.
 * (See {@link Ability#getType()}).  The AbilitySlot allows the Creature
 * to ready an Ability that they can then use.  AbilitySlots can
 * only accept activateable Abilities.  (See {@link Ability#isActivateable()}).
 * <p>
 * Each slot has a cooldown period before it can be used again.  Only one Ability
 * may be readied in a given AbilitySlot at a time.  As Creatures advance in level,
 * they will gain more AbilitySlots, expanding the number and diversity of their
 * readied Abilities.
 * <p>
 * AbilitySlots can be fixed, meaning the Ability associated with the slot cannot
 * be modified after creation.  This allows a given slot to be locked to a given
 * Ability.
 *
 * @author Jared Stephen
 */

public class AbilitySlot implements Saveable
{
    private final String type;
    private boolean fixed;

    private String abilityID;
    private int cooldownRoundsLeft;
    private int activeRoundsLeft;
    private boolean active;

    private Creature parent;

    private List<Effect> activeEffects;

    @Override
    public Object save()
    {
        JSONOrderedObject data = new JSONOrderedObject();

        data.put("ref", SaveGameUtil.getRef(this));
        data.put("type", type);

        if (fixed) data.put("fixed", fixed);

        if (abilityID != null) {
            data.put("abilityID", abilityID);

            if (active) {
                data.put("active", active);
            }

            if (activeRoundsLeft != 0) {
                data.put("activeRoundsLeft", activeRoundsLeft);
            }

            if (cooldownRoundsLeft != 0) {
                data.put("cooldownRoundsLeft", cooldownRoundsLeft);
            }
        }

        if (activeEffects.size() > 0) {
            List<Object> effectsData = new ArrayList<Object>();

            for (Effect effect : activeEffects) {
                // don't save effect references for effects on dead NPCs, as these
                // will not be resolved on loading
                if (effect.getTarget() instanceof NPC) {
                    NPC target = (NPC)effect.getTarget();

                    if (target.isDead()) {
                        continue;
                    }
                }

                effectsData.add(SaveGameUtil.getRef(effect));
            }

            data.put("effects", effectsData.toArray());
        }

        return data;
    }

    public static AbilitySlot load(SimpleJSONObject data, ReferenceHandler refHandler, Creature parent)
    {
        AbilitySlot slot = new AbilitySlot(data.get("type", null), parent);

        refHandler.add(data.get("ref", null), slot);

        if (data.containsKey("fixed")) {
            slot.fixed = data.get("fixed", false);
        }

        if (data.containsKey("abilityID")) {
            slot.abilityID = data.get("abilityID", null);

            if (data.containsKey("activeRoundsLeft")) {
                slot.activeRoundsLeft = data.get("activeRoundsLeft", 0);
            }

            if (data.containsKey("active")) {
                slot.active = data.get("active", false);
            }

            if (data.containsKey("cooldownRoundsLeft")) {
                slot.cooldownRoundsLeft = data.get("cooldownRoundsLeft", 0);
            }
        }

        if (data.containsKey("effects")) {
            for (SimpleJSONArrayEntry entry : data.getArray("effects")) {
                String effectRef = entry.getString();

                refHandler.addEffectReference(slot, effectRef);
            }
        }

        return slot;
    }

    /**
     * This method should only be used by the reference handler during loading.
     * Sets the active effects for this ability slot to the specified array
     *
     * @param effects the array of effects
     */

    public void loadActiveEffects(List<Effect> effects)
    {
        activeEffects = effects;
    }

    /**
     * Create a new AbilitySlot accepting Abilities of the specified type.
     * This AbilitySlot will not be fixed.
     *
     * @param type   the value of {@link Ability#getType()} that any Ability
     *               readied in this slot must return
     * @param parent the Creature or AbilityActivator that owns this AbilitySlot
     */

    public AbilitySlot(String type, Creature parent)
    {
        this.type = type;
        this.fixed = false;

        this.abilityID = null;
        this.cooldownRoundsLeft = 0;
        this.activeRoundsLeft = 0;
        this.active = false;

        this.parent = parent;
        activeEffects = new ArrayList<Effect>();
    }

    /**
     * Create a new AbilitySlot fixed to the supplied Ability.  The Ability
     * readied by this slot will not be modifiable after creation.
     *
     * @param ability the Ability to be readied in this fixed slot
     * @param parent  the Creature that owns this AbilitySlot
     */

    public AbilitySlot(Ability ability, Creature parent)
    {
        this.type = ability.getType();
        this.fixed = true;

        this.abilityID = ability.getID();
        this.cooldownRoundsLeft = 0;
        this.activeRoundsLeft = 0;
        this.active = false;

        this.parent = parent;
        activeEffects = new ArrayList<Effect>();
    }

    /**
     * Create a new AbilitySlot that is an exact copy of the specified
     * AbilitySlot, except that this AbilitySlot's parent will be the
     * specified Creature.  This includes the type requirement, whether
     * the Slot is fixed, and all other properties both temporary and permanent.
     *
     * @param other  the AbilitySlot to copy
     * @param parent the owner Creature for this AbilitySlot
     */

    public AbilitySlot(AbilitySlot other, Creature parent)
    {
        this.type = other.type;
        this.fixed = other.fixed;

        this.abilityID = other.abilityID;
        this.cooldownRoundsLeft = other.cooldownRoundsLeft;
        this.activeRoundsLeft = other.activeRoundsLeft;
        this.active = other.active;

        this.parent = parent;

        activeEffects = new ArrayList<Effect>();
        // don't copy active effects from other ability slot
    }

    /**
     * Return the type that any Ability readied in this AbilitySlot must match
     *
     * @return the type of any Ability readied in this AbilitySlot
     */

    public String getType()
    {
        return type;
    }

    /**
     * Returns true if this AbilitySlot is fixed and the readied Ability cannot be
     * changed, false otherwise
     *
     * @return true if this AbilitySlot is fixed, false otherwise
     */

    public boolean isFixed()
    {
        return fixed;
    }

    /**
     * Returns the Ability currently readied in this AbilitySlot
     *
     * @return the Ability currently readied in this AbilitySlot
     */

    public Ability getAbility()
    {
        return Game.ruleset.getAbility(abilityID);
    }

    /**
     * Returns the ID of the Ability currently readied in this AbilitySlot, or
     * null if no Ability is readied.
     *
     * @return the ID of the Ability currently readied in this AbilitySlot
     */

    public String getAbilityID()
    {
        return abilityID;
    }

    /**
     * Returns the length of the cooldown, in rounds, until this AbilitySlot
     * can be activated again.
     *
     * @return the length of the cooldown for this AbilitySlot
     */

    public int getCooldownRoundsLeft()
    {
        return cooldownRoundsLeft;
    }

    /**
     * Returns whether this AbilitySlot is active.  This is only possible if
     * the readied Ability is a mode (See {@link Ability#isMode()}).
     *
     * @return true if this AbilitySlot is active, false otherwise
     */

    public boolean isActive()
    {
        return active;
    }

    /**
     * Returns the number of rounds that this AbilitySlot will remain active.
     *
     * @return the number of remaining rounds that this AbilitySlot will remain
     * active.
     */

    public int getActiveRoundsLeft()
    {
        return activeRoundsLeft;
    }

    /**
     * Returns the owner of this AbilitySlot
     *
     * @return the owner of this AbilitySlot
     */

    public Creature getParent()
    {
        return parent;
    }

    /**
     * Returns true if this AbilitySlot currently does not ready any Ability.
     * Return false otherwise.
     *
     * @return whether this AbilitySlot is empty
     */

    public boolean isEmpty()
    {
        return abilityID == null;
    }

    /**
     * Sets the number of rounds that this AbilitySlot will remain active.
     * This is only relevant for modes.
     *
     * @param rounds the number of rounds for this AbilitySlot to remain active
     */

    public void setActiveRoundsLeft(int rounds)
    {
        this.activeRoundsLeft = rounds;
    }

    /**
     * Attempts to set the Ability readied in this AbilitySlot to the specified Ability.
     * If this AbilitySlot is fixed or is active and cannot be canceled, nothing is done.
     * Note that cooldown rounds left are not affected by this.  Passing null as the
     * parameter to this method will unready the slot.
     *
     * @param ability the Ability to be readied in this AbilitySlot
     * @return true if the Ability was readied, or false if the Ability cannot be readied
     */

    protected boolean setAbility(Ability ability)
    {
        if (ability == null) {
            return setAbility((String)null);
        } else {
            return setAbility(ability.getID());
        }
    }

    /**
     * Attempts to set this Ability readied in this AbilitySlot to the Ability with the
     * specified ID.  If this AbilitySlot is fixed or is active and cannot be canceled,
     * nothing is done and the method returns false.  Otherwise, the ability is readied
     * and the method returns true.  Passing null as the parameter to this method will
     * unready the slot.  Note that the cooldown rounds are unaffected by this method.
     *
     * @param abilityID the ID of the Ability to ready
     * @return true if the Ability was readied, false otherwise
     */

    protected boolean setAbility(String abilityID)
    {
        if (!isSettable()) return false;

        if (this.abilityID != null) {
            if (this.active) {
                if (this.getAbility().isCancelable()) {
                    deactivate();
                } else {
                    return false;
                }
            }
        }

        this.active = false;
        this.activeRoundsLeft = 0;

        this.abilityID = abilityID;

        return true;
    }

    /**
     * Determines whether or not the Ability readied by this AbilitySlot can
     * currently be set to another value.  Fixed ability slots are never
     * settable.  If an AbilitySlot is active and cannot be canceled, it is
     * also not settable.
     *
     * @return false if the AbilitySlot is fixed or active and not cancelable,
     * true otherwise
     */

    public boolean isSettable()
    {
        if (fixed) return false;

        if (this.abilityID != null) {
            if (this.active && !this.getAbility().isCancelable()) return false;
        }

        return true;
    }

    /**
     * Returns true if and only if this AbilitySlot currently has associated Effects.
     *
     * @return true if and only if this AbilitySlot currently has associated Effects.
     */

    public boolean hasActiveEffects()
    {
        return !activeEffects.isEmpty();
    }

    /**
     * Sets the cooldown time remaining for this ability slot to zero, allowing it
     * to be activated again immediately if it is not currently active
     */

    public void resetCooldown()
    {
        this.cooldownRoundsLeft = 0;
    }

    /**
     * Returns true if and only if the Ability readied in this slot can be activated.  Meaning
     * that this slot is readying an Ability, it is not already active or in cooldown, and the
     * parent of this slot has the necessary Action Points (AP).  If the readied Ability has
     * a "canActivate" script function, then that function must return true.  If the readied
     * Ability does not have a "canActivate" function, then only the above considerations are
     * taken into account.
     * <p>
     * The return value of this function specifies whether the player should be able to
     * activate this AbilitySlot at this time.
     *
     * @return whether this AbilitySlot can be activated
     */

    public boolean canActivate()
    {
        if (abilityID == null) return false;
        if (active) return false;
        if (cooldownRoundsLeft > 0) return false;
        if (!parent.timer.canPerformAction(this.getAbility().getAPCost())) return false;
        if (!Game.isInTurnMode() && !getAbility().canActivateOutsideCombat()) return false;

        if (getAbility().hasFunction(ScriptFunctionType.canActivate)) {
            Object returnValue = getAbility().executeFunction(ScriptFunctionType.canActivate, parent, this);
            return Boolean.TRUE.equals(returnValue);
        } else {
            return true;
        }
    }

    /**
     * Returns true if and only if the Ability readied in this slot can be manually deactivated at the
     * current time.  This requires that the Ability is an active mode that is cancelable.
     *
     * @return true if and only if the Ability readied in this slot can be manually deactivated
     */

    public boolean canDeactivate()
    {
        if (abilityID == null) return false;
        if (!active || !this.getAbility().isCancelable()) return false;

        return true;
    }

    /**
     * Activates this AbilitySlot.  If the AbilitySlot is currently empty or otherwise
     * cannot be activated, nothing happens.  Depending on the Ability, a cooldown may be
     * initiated and / or the AbilitySlot may enter an active mode.  Any AP cost is
     * also deducted from the parent at this point.
     */

    public void activate()
    {
        if (cooldownRoundsLeft > 0) return;

        if (getAbility().isMode()) {
            this.active = true;

            if (this.activeRoundsLeft == 0) {
                this.activeRoundsLeft = Integer.MAX_VALUE / 2;
            }
        }

        this.cooldownRoundsLeft = getAbility().getCooldown(this.parent) + this.activeRoundsLeft;

        getAbility().activate(this.getParent());
    }

    /**
     * Deactivates this AbilitySlot.  If the AbilitySlot is currently empty or not active,
     * nothing happens.  The cooldown rounds remaining are set to the ability's cooldown.
     * The AbilitySlot is set to not active.  Any Effects with removeOnDeactivate set to true
     * will be removed from their targets.
     */

    public void deactivate()
    {
        if (abilityID == null || !active) return;

        this.active = false;
        this.cooldownRoundsLeft = getAbility().getCooldown(this.parent);
        this.activeRoundsLeft = 0;

        Iterator<Effect> effectIter = activeEffects.iterator();
        while (effectIter.hasNext()) {
            Effect effect = effectIter.next();

            // remove effect if it has removeOnDeactivate
            if (effect.removeOnDeactivate()) {
                effect.getTarget().removeEffect(effect);
                effectIter.remove();
            }
        }
    }

    /**
     * Decreases the number of rounds left in the active state and cooldown
     * (if applicable) of this AbilitySlot by the specified amount.  If the cooldown
     * reaches 0, this AbilitySlot becomes ready again.  If the active timer reaches 0,
     * then the AbilitySlot is deactivated.
     * <p>
     * Also is responsible for elapsing rounds for child Effects
     *
     * @param rounds the number of rounds to elapse
     */

    public void elapseRounds(int rounds)
    {
        if (cooldownRoundsLeft > 0) cooldownRoundsLeft -= rounds;

        if (activeRoundsLeft > 0) {
            activeRoundsLeft -= rounds;

            if (activeRoundsLeft <= 0) {
                deactivate();
            }
        }

        if (cooldownRoundsLeft < 0) cooldownRoundsLeft = 0;
        if (activeRoundsLeft < 0) activeRoundsLeft = 0;

        // elapse rounds for child effects
        // only go up to the size of the current size of the list
        // this will prevent elapsing a round for any effects that
        // were created as a result of the current effects running their onRoundElapsed
        synchronized (activeEffects) {
            int size = activeEffects.size();

            for (int i = 0; i < size; i++) {
                Effect effect = activeEffects.get(i);

                effect.elapseRounds(rounds);

                boolean removeEffect = effect.getRoundsRemaining() < 1 && !effect.removeOnDeactivate();
                if (effect.getTarget() != null) {
                    removeEffect = removeEffect || !effect.getTarget().isValidEffectTarget();

                    if (removeEffect) {
                        effect.getTarget().removeEffect(effect);
                        activeEffects.remove(i);

                        // we have one fewer effect to run through
                        i--;
                        size--;
                    }
                } else {
                    // just remove the effect if it has no target
                    activeEffects.remove(i);
                    i--;
                    size--;
                }


            }
        }
    }

    /**
     * Cancels and immediately ends all effects being tracked from this ability slot.
     */

    public void cancelAllEffects()
    {
        synchronized (activeEffects) {
            int size = activeEffects.size();

            for (int i = 0; i < size; i++) {
                Effect effect = activeEffects.get(i);

                effect.getTarget().removeEffect(effect);

                if (size != activeEffects.size()) {
                    // the effect was removed as a result of a script firing
                    i--;
                    size--;
                }
            }

            activeEffects.clear();
        }

        if (getAbility().isCancelable()) {
            deactivate();
        }
    }

    /**
     * Cancels and immediately ends all Aura effects active for this ability slot
     *
     * @return true if this ability slot was deactivated as a result of the aura cancelation,
     * false otherwise
     */

    public boolean cancelAllAuras()
    {
        boolean auraFound = false;

        synchronized (activeEffects) {
            int size = activeEffects.size();

            for (int i = 0; i < size; i++) {
                Effect effect = activeEffects.get(i);

                if (!(effect instanceof Aura)) continue;

                effect.getTarget().removeEffect(effect);
                activeEffects.remove(i);

                i--;
                size--;
                auraFound = true;
            }
        }

        if (auraFound && abilityID != null) {
            if (getAbility().isCancelable()) {
                deactivate();
                return true;
            }
        }

        return false;
    }

    /**
     * Creates a new Effect with the creator set to the parent of this AbilitySlot,
     * registers the Effect with this AbilitySlot so that it is removed when appropriate,
     * and returns the new Effect.
     *
     * @return a new Effect with the parent of this AbilitySlot as creator
     */

    public Effect createEffect()
    {
        Effect effect = new Effect();
        effect.setSlot(this);

        synchronized (activeEffects) {
            this.activeEffects.add(effect);
        }

        return effect;
    }

    /**
     * Creates a new Effect with the creator set to the parent of this AbilitySlot,
     * registers the Effect with this AbilitySlot so that it is removed when appropriate,
     * and returns the new Effect.  The script for the Effect is set to the Script
     * located at the Resource indicated by scriptID.
     *
     * @param scriptID the Script Resource ID of the script for this Effect
     * @return a new Effect
     */

    public Effect createEffect(String scriptID)
    {
        Effect effect = new Effect(scriptID);
        effect.setSlot(this);

        synchronized (activeEffects) {
            this.activeEffects.add(effect);
        }

        return effect;
    }

    /**
     * Creates a new Aura with the creator set to the parent of this AbilitySlot,
     * registers the Aura with this AbilitySlot so that it is removed when appropriate,
     * and returns the new Aura.  The script for the Aura is set to the Script
     * located at the Resource indicated by scriptID.
     *
     * @param scriptID the Script Resource ID of the script for this Aura
     * @return a new Aura
     */

    public Aura createAura(String scriptID)
    {
        Aura aura = new Aura(scriptID);
        aura.setSlot(this);

        synchronized (activeEffects) {
            this.activeEffects.add(aura);
        }

        return aura;
    }

    /**
     * Returns the String label that should be shown on top of any viewer for this
     * AbilitySlot.  This String represents the cooldown.
     *
     * @return the String label to be shown on a viewer for this AbilitySlot
     */

    public String getLabelText()
    {
        int cooldown = cooldownRoundsLeft - activeRoundsLeft;
        int active = activeRoundsLeft;

        StringBuilder labelText = new StringBuilder();
        if (active > Integer.MAX_VALUE / 4) {
            labelText.append("\u221E");
        } else
            if (active > 0) {
                labelText.append(active);
            }

        if (cooldown > 0) {
            if (labelText.length() > 0) labelText.append(" + ");

            labelText.append(cooldown);
        }

        return labelText.toString();
    }

    @Override
    public String toString()
    {
        if (abilityID == null) {
            return "None";
        } else {
            return abilityID;
        }
    }
}
