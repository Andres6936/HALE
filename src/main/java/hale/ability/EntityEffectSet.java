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

package main.java.hale.ability;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import main.java.hale.Game;
import main.java.hale.bonus.Bonus;
import main.java.hale.bonus.BonusList;
import main.java.hale.entity.Entity;
import main.java.hale.loading.JSONOrderedObject;
import main.java.hale.loading.LoadGameException;
import main.java.hale.loading.ReferenceHandler;
import main.java.hale.loading.Saveable;
import main.java.hale.util.SaveGameUtil;
import main.java.hale.util.SimpleJSONArray;
import main.java.hale.util.SimpleJSONArrayEntry;
import main.java.hale.util.SimpleJSONObject;

/**
 * Class for storing the set of Effects currently applied to an Entity.
 *
 * @author Jared Stephen
 */

public class EntityEffectSet implements Iterable<Effect>, Saveable
{
    // all auras should also be present as an Effect in one of the other two arrays
    private final List<Aura> auras;

    private final List<Effect> effectsNoActiveScript;
    private final List<Effect> effectsWithActiveScript;

    @Override
    public JSONOrderedObject save()
    {
        JSONOrderedObject data = new JSONOrderedObject();

        if (effectsNoActiveScript.size() > 0) {
            List<Object> effectsNoData = new ArrayList<Object>();

            for (Effect effect : effectsNoActiveScript) {
                if (effect.getRoundsRemaining() == 0 && !effect.removeOnDeactivate())
                // the effect is permanent and will be recreated when creating the creature
                {
                    continue;
                }

                effectsNoData.add(effect.save());
            }

            if (effectsNoData.size() > 0) {
                data.put("noActiveScript", effectsNoData.toArray());
            }
        }

        if (effectsWithActiveScript.size() > 0) {
            List<Object> effectsWithData = new ArrayList<Object>();

            for (Effect effect : effectsWithActiveScript) {
                if (effect.getRoundsRemaining() == 0 && !effect.removeOnDeactivate())
                // the effect is permanent and will be recreated when creating the creature
                {
                    continue;
                }

                effectsWithData.add(effect.save());
            }

            if (effectsWithData.size() > 0) {
                data.put("withActiveScript", effectsWithData.toArray());
            }
        }

        if (auras.size() > 0) {
            Object[] aurasData = new Object[auras.size()];
            int i = 0;
            for (Aura aura : auras) {
                aurasData[i] = SaveGameUtil.getRef(aura);
                i++;
            }
            data.put("auras", aurasData);
        }

        return data;
    }

    public void load(SimpleJSONObject data, ReferenceHandler refHandler, EffectTarget target) throws LoadGameException
    {
        clear();

        if (data.containsKey("noActiveScript")) {
            SimpleJSONArray arrayData = data.getArray("noActiveScript");
            for (SimpleJSONArrayEntry entry : arrayData) {
                SimpleJSONObject entryData = entry.getObject();

                Effect effect = Effect.load(entryData, refHandler, target);

                String ref = entryData.get("ref", null);
                refHandler.add(ref, effect);
                effectsNoActiveScript.add(effect);
            }
        }

        if (data.containsKey("withActiveScript")) {
            SimpleJSONArray arrayData = data.getArray("withActiveScript");
            for (SimpleJSONArrayEntry entry : arrayData) {
                SimpleJSONObject entryData = entry.getObject();

                Effect effect = Effect.load(entryData, refHandler, target);

                String ref = entryData.get("ref", null);
                refHandler.add(ref, effect);
                effectsWithActiveScript.add(effect);
            }
        }

        if (data.containsKey("auras")) {
            SimpleJSONArray arrayData = data.getArray("auras");
            for (SimpleJSONArrayEntry entry : arrayData) {
                auras.add((Aura)refHandler.getEffect(entry.getString()));
            }
        }
    }

    /**
     * Creates a new EntityEffectSet with no Effects.
     */
    public EntityEffectSet()
    {
        auras = new ArrayList<Aura>();
        effectsNoActiveScript = new ArrayList<Effect>();
        effectsWithActiveScript = new ArrayList<Effect>();
    }

    /**
     * Creates an EntityEffectSet that is a copy of the specified
     * EntityEffectSet, containing copies of all Effects in that set.
     *
     * @param other  the EntityEffectSet to copy
     * @param target the target for all newly copied effects
     */

    public EntityEffectSet(EntityEffectSet other, EffectTarget target)
    {
        this();

        for (Effect effect : other.effectsNoActiveScript) {
            this.effectsNoActiveScript.add(new Effect(effect, target));
        }

        for (Effect effect : other.effectsWithActiveScript) {
            this.effectsWithActiveScript.add(new Effect(effect, target));
        }

        for (Aura aura : other.auras) {
            this.auras.add(new Aura(aura, target));
        }
    }

    /**
     * Removes all effects from this set
     */

    public synchronized void clear()
    {
        auras.clear();
        effectsNoActiveScript.clear();
        effectsWithActiveScript.clear();
    }

    /**
     * Returns the total number of effects stored in this set
     *
     * @return the number of effects in this set
     */

    public int size()
    {
        return effectsWithActiveScript.size() + effectsNoActiveScript.size();
    }

    /**
     * Moves all auras held by this EntityEffectSet to be centered on the
     * specified grid position.  Called whenever a creature moves
     */

    public synchronized void moveAuras()
    {
        for (Aura aura : auras) {
            Game.curCampaign.curArea.moveAura(aura, aura.getCurrentAffectedPoints());
        }
    }

    /**
     * Elapses the specified number of rounds for effects in this EntityEffectSet that are not otherwise
     * being tracked and have a positive duration, generally by an AbilitySlot.  Effects that
     * have not had their duration set via {@link Effect#setDuration(int)} will not decrease rounds
     * but may still run onRoundElapsed scripts
     *
     * @param parent the parent entity that is owning the effects in this set
     * @param rounds the number of rounds to elapse
     */

    public synchronized void elapseRounds(Entity parent, int rounds)
    {
        elapseRounds(effectsNoActiveScript, parent, rounds);
        elapseRounds(effectsWithActiveScript, parent, rounds);
    }

    private void elapseRounds(List<Effect> effects, Entity parent, int rounds)
    {
        for (int i = 0; i < effects.size(); i++) {
            Effect effect = effects.get(i);

            // only effects with a specified duration (meaning rounds remaining not equal to zero)
            // can be removed
            boolean removable = effect.getRoundsRemaining() != 0;

            if (effect.getSlot() != null) continue;

            effect.elapseRounds(rounds);
            if (removable && effect.getRoundsRemaining() < 1) {
                parent.removeEffect(effect);
                i--;
            }
        }
    }

    /**
     * Returns a list of all effects in this effect set that are dispellable - that is, effects
     * which were created by an ability slot which holds a spell that is affected by spell resistance
     *
     * @return the list of all dispellable effects
     */

    public synchronized List<Effect> getDispellableEffects()
    {
        List<Effect> dispellables = new ArrayList<Effect>();

        for (Effect effect : effectsNoActiveScript) {
            if (checkDispellable(effect)) {
                dispellables.add(effect);
            }
        }

        for (Effect effect : effectsWithActiveScript) {
            if (checkDispellable(effect)) {
                dispellables.add(effect);
            }
        }

        return dispellables;
    }

    private boolean checkDispellable(Effect effect)
    {
        if (effect.getSlot() == null) return false;

        Ability ability = effect.getSlot().getAbility();

        if (!ability.isActivateable()) return false;

        if (ability.getSpellLevel() <= 0) return false;

        if (!(ability instanceof Spell)) return false;

        Spell spell = (Spell)ability;

        return spell.spellResistanceApplies();
    }

    /**
     * Gets the first effect found in this effect set that was created by an abilityslot holding
     * the specified abilityID, or null if no effect is found matching that criterion
     *
     * @param abilityID the ID of the ability that created the effect
     * @return the effect created by the specified slot
     */

    public synchronized Effect getEffectCreatedBySlot(String abilityID)
    {
        for (Effect effect : effectsNoActiveScript) {
            if (effect.getSlot() == null) continue;

            if (abilityID.equals(effect.getSlot().getAbilityID())) {
                return effect;
            }
        }

        for (Effect effect : effectsWithActiveScript) {
            if (effect.getSlot() == null) continue;

            if (abilityID.equals(effect.getSlot().getAbilityID())) {
                return effect;
            }
        }

        return null;
    }

    /**
     * Adds the specified Effect to this EntityEffectSet
     *
     * @param effect        the Effect to add
     * @param positionValid if true, then auras can be added to the area, if false
     *                      then auras will not be added to the area
     */

    public synchronized void add(Effect effect, boolean positionValid)
    {
        if (effect instanceof Aura) {
            Aura aura = (Aura)effect;
            auras.add(aura);
            if (positionValid) {
                Game.curCampaign.curArea.applyEffect(aura, aura.getCurrentAffectedPoints());
            }
        }

        if (effect.getNumberOfScriptFunctionTypes() == 0) {
            effectsNoActiveScript.add(effect);
        } else {
            effectsWithActiveScript.add(effect);
        }

        executeOnAll(ScriptFunctionType.onEffectApplied, effect);
    }

    /**
     * Removes the specified Effect from this EntityEffectSet if it is present.
     * If it is not present, no action is taken.
     *
     * @param effect the Effect to remove.
     */

    public synchronized void remove(Effect effect)
    {
        if (effect instanceof Aura) {
            Aura aura = (Aura)effect;
            auras.remove(aura);
            Game.curCampaign.curArea.removeEffect(aura);
        }

        if (effect.getNumberOfScriptFunctionTypes() == 0) {
            effectsNoActiveScript.remove(effect);
        } else {
            effectsWithActiveScript.remove(effect);
        }
    }

    /**
     * For each aura held by this set, execute the function with the specified type for all child effects.
     * The function is executed with arguments of the child effect target and the aura, so the signature
     * looks like:
     * function(Game.scriptInterface, target, aura)
     *
     * @param type
     * @param arguments
     */

    public synchronized void executeOnAllAuraChildren(ScriptFunctionType type)
    {
        for (Aura aura : auras) {
            for (Effect childEffect : aura.getChildEffects()) {
                aura.executeFunction(type, childEffect.getTarget(), aura);
            }
        }
    }

    /**
     * Executes the script function with the specified ScriptFunctionType on all
     * Effects with a script and the specified ScriptFunctionType in this
     * EntityEffectSet.  The specified arguments are passed to all functions, with
     * the parent effect of each function passed as the final argument.  So, the
     * arguments list will end up looking like
     * function(Game.scriptInterface, arguments, effect)
     *
     * @param type      the ScriptFunctionType to execute
     * @param arguments the arguments to pass to the functions executed
     */

    public synchronized void executeOnAll(ScriptFunctionType type, Object... arguments)
    {
        // create the argument array with room for the effect at the end
        Object[] args = new Object[arguments.length + 1];
        for (int i = 0; i < arguments.length; i++) {
            args[i] = arguments[i];
        }

        Set<Effect> effectsAlreadyExecuted = new HashSet<Effect>();

        for (int i = 0; i < effectsWithActiveScript.size(); i++) {
            Effect effect = effectsWithActiveScript.get(i);

            // only execute the function once per effect
            if (effectsAlreadyExecuted.contains(effect)) continue;

            int sizeAtStart = effectsWithActiveScript.size();

            args[args.length - 1] = effect;
            effect.executeFunction(type, args);

            effectsAlreadyExecuted.add(effect);

            // if the effect has removed any other effects, we must reset and go through
            // the loop again to make sure all remaining effects have the scriptFunction run
            int sizeAtEnd = effectsWithActiveScript.size();
            if (sizeAtEnd != sizeAtStart) {
                i = 0;
            }
        }
    }

    /**
     * Offsets all animation positions of all animations of Effects contained
     * within this EntityEffectSet by the specified amount.
     *
     * @param screenDx the x coordinate screen amount
     * @param screenDy the y coordinate screen amount
     */

    public synchronized void offsetAnimationPositions(int screenDx, int screenDy)
    {
        for (Effect effect : effectsNoActiveScript) {
            effect.offsetAnimationPositions(screenDx, screenDy);
        }

        for (Effect effect : effectsWithActiveScript) {
            effect.offsetAnimationPositions(screenDx, screenDy);
        }
    }

    /**
     * Starts all animations associated with Effects contained in this set
     */

    public synchronized void startAnimations()
    {
        for (Effect effect : effectsNoActiveScript) {
            effect.startAnimations();
        }

        for (Effect effect : effectsWithActiveScript) {
            effect.startAnimations();
        }
    }

    /**
     * Ends all animations currently associated with Effects contained in this
     * EntityEffectSet
     */

    public synchronized void endAllAnimations()
    {
        for (Effect effect : effectsNoActiveScript) {
            effect.endAnimations();
        }

        for (Effect effect : effectsWithActiveScript) {
            effect.endAnimations();
        }
    }

    /**
     * Returns a List of all Bonuses of the specified Type contained in
     * all Effects in this EntityEffectSet.  In this context, only
     * Bonuses that have a positive value are returned.
     *
     * @param bonusType
     * @return the List of all Bonuses of the specified Type
     */

    public BonusList getBonusesOfType(String bonusType)
    {
        return getBonusesOfType(Bonus.Type.valueOf(bonusType), true);
    }

    /**
     * Returns a List of all Bonuses of the specified Type contained in
     * all Effects in this EntityEffectSet.  In this context, only
     * Bonuses that have a positive value are returned.
     *
     * @param type the Bonus Type to search for
     * @return the List of all Bonuses of the specified Type
     */

    public BonusList getBonusesOfType(Bonus.Type type)
    {
        return getBonusesOfType(type, true);
    }

    /**
     * Returns a List of all penalty Bonuses of the specified Type contained in
     * all Effects in this EntityEffectSet.  In this context, only Bonuses
     * with a negative value are returned.
     *
     * @param bonusType
     * @return the List of all Bonuses of the specified Type
     */

    public BonusList getPenaltiesOfType(String bonusType)
    {
        return getBonusesOfType(Bonus.Type.valueOf(bonusType), false);
    }

    /**
     * Returns a List of all penalty Bonuses of the specified Type contained in
     * all Effects in this EntityEffectSet.  In this context, only Bonuses
     * with a negative value are returned.
     *
     * @param type the Bonus Type to search for
     * @return the List of all Bonuses of the specified Type
     */

    public BonusList getPenaltiesOfType(Bonus.Type type)
    {
        return getBonusesOfType(type, false);
    }

    /**
     * Returns a List of all Effects contained in this EntityEffectSet
     * containing bonuses with the specified Bonus.Type
     *
     * @param type the Type of Bonus to search for
     * @return the List of Effects with bonuses of the specified Type
     */

    public synchronized List<Effect> getEffectsWithBonusesOfType(Bonus.Type type)
    {
        List<Effect> effects = new ArrayList<Effect>();

        for (Effect effect : effectsNoActiveScript) {
            for (Bonus bonus : effect.getBonuses()) {
                if (bonus.getType() == type) {
                    effects.add(effect);
                    break;
                }
            }
        }

        for (Effect effect : effectsWithActiveScript) {
            for (Bonus bonus : effect.getBonuses()) {
                if (bonus.getType() == type) {
                    effects.add(effect);
                    break;
                }
            }
        }

        return effects;
    }

    /**
     * Returns true if this entity has one or more penalties of any of the
     * specified types, false otherwise
     *
     * @param types
     * @return whether this entity has any penalties of any of the specified types
     */

    public synchronized boolean hasPenaltiesOfTypes(String[] types)
    {
        // first build up the hashset of bonus types
        Set<Bonus.Type> bonusTypes = new HashSet<Bonus.Type>();
        for (String type : types) {
            bonusTypes.add(Bonus.Type.valueOf(type));
        }

        // now check all bonuses for the specified types
        for (Effect effect : effectsNoActiveScript) {
            for (Bonus bonus : effect.getBonuses()) {
                if (bonusTypes.contains(bonus.getType()) && bonus.getValue() < 0) {
                    return true;
                }
            }
        }

        for (Effect effect : effectsWithActiveScript) {
            for (Bonus bonus : effect.getBonuses()) {
                if (bonusTypes.contains(bonus.getType()) && bonus.getValue() < 0) {
                    return true;
                }
            }
        }

        return false;
    }

    // if bonusOrPenalty true, find bonuses, else find penalties

    private synchronized BonusList getBonusesOfType(Bonus.Type type, boolean bonusOrPenalty)
    {
        BonusList bonusesToReturn = new BonusList();

        for (Effect effect : effectsNoActiveScript) {
            addBonusesOfTypeToList(effect, bonusesToReturn, type, bonusOrPenalty);
        }

        for (Effect effect : effectsWithActiveScript) {
            addBonusesOfTypeToList(effect, bonusesToReturn, type, bonusOrPenalty);
        }

        return bonusesToReturn;
    }

    private void addBonusesOfTypeToList(Effect effect, BonusList list, Bonus.Type type, boolean bonusOrPenalty)
    {
        for (Bonus bonus : effect.getBonuses()) {
            if (bonus.getType() != type) continue;

            if (bonusOrPenalty && bonus.getValue() >= 0) {
                list.add(bonus);
            } else
                if (!bonusOrPenalty && bonus.getValue() <= 0) list.add(bonus);
        }
    }

    /**
     * Returns an Iterator over all the Effects in this Set.  This Iterator
     * does not support the remove() operation.
     */

    @Override
    public java.util.Iterator<Effect> iterator()
    {
        return new Iterator();
    }

    private class Iterator implements java.util.Iterator<Effect>
    {
        private java.util.Iterator<Effect> iter;
        private boolean secondIterUsed;

        private Iterator()
        {
            iter = effectsNoActiveScript.iterator();
            secondIterUsed = false;
        }

        @Override
        public boolean hasNext()
        {
            if (!iter.hasNext()) {
                if (!secondIterUsed) {
                    iter = effectsWithActiveScript.iterator();
                    secondIterUsed = true;
                    return iter.hasNext();
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }

        @Override
        public Effect next()
        {
            return iter.next();
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
