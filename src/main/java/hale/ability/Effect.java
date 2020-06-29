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
import java.util.Collection;
import java.util.List;

import de.matthiasmann.twl.Color;
import main.java.hale.Game;
import main.java.hale.HasScriptState;
import main.java.hale.bonus.Bonus;
import main.java.hale.bonus.BonusList;
import main.java.hale.icon.Icon;
import main.java.hale.icon.IconFactory;
import main.java.hale.loading.JSONOrderedObject;
import main.java.hale.loading.LoadGameException;
import main.java.hale.loading.ReferenceHandler;
import main.java.hale.loading.Saveable;
import main.java.hale.particle.Animated;
import main.java.hale.resource.ResourceManager;
import main.java.hale.ScriptState;
import main.java.hale.util.SaveGameUtil;
import main.java.hale.util.SimpleJSONArray;
import main.java.hale.util.SimpleJSONArrayEntry;
import main.java.hale.util.SimpleJSONObject;

/**
 * An Effect is a container for applying bonuses and penalties to a Creature.
 * <p>
 * Each effect will generally contain one or more {@link main.java.hale.bonus.Bonus}es
 * stored in a BonusList.
 * <p>
 * Effects have a duration in rounds, which may be infinite.  Some Effects may
 * be canceled manually by the user or dispelled.
 *
 * @author Jared Stephen
 */

public class Effect extends Scriptable implements Saveable, HasScriptState
{
    private String title;
    private int duration;
    private boolean removeOnDeactivate;
    private boolean hasDescription;
    private List<Effect> childEffects;
    private EffectTarget target;
    private AbilitySlot slot;
    private BonusList bonuses;
    private List<Icon> icons;

    private List<Animated> animations;

    private ScriptState scriptState;

    @Override
    public JSONOrderedObject save()
    {
        JSONOrderedObject data = super.save();

        data.put("ref", SaveGameUtil.getRef(this));

        if (title != null) data.put("title", title);

        if (icons.size() > 0) {
            Object[] iconsData = new Object[icons.size()];
            for (int i = 0; i < iconsData.length; i++) {
                iconsData[i] = icons.get(i).save();
            }
            data.put("icons", iconsData);
        }

        data.put("duration", duration);
        data.put("removeOnDeactivate", removeOnDeactivate);
        data.put("hasDescription", hasDescription);

        if (childEffects.size() > 0) {
            // store children

            List<String> childData = new ArrayList<String>();
            for (Effect effect : childEffects) {
                // don't save children with invalid targets
                if (!effect.getTarget().isValidEffectTarget()) continue;

                childData.add(SaveGameUtil.getRef(effect));
            }

            data.put("children", childData.toArray());
        }

        if (slot != null) {
            data.put("slot", SaveGameUtil.getRef(slot));
        }

        if (bonuses.size() > 0) {
            data.put("bonuses", bonuses.save());
        }

        // store animations
        int i = 0;
        Object[] animationData = new JSONOrderedObject[animations.size()];
        for (Animated animated : animations) {
            animationData[i] = animated.save();
            i++;
        }

        if (animationData.length != 0) {
            data.put("animations", animationData);
        }

        if (!scriptState.isEmpty()) {
            data.put("scriptState", scriptState.save());
        }

        return data;
    }

    /**
     * Creates a new Effect from the specified JSON data.  If the JSON specifies an Aura, creates a new Aura
     *
     * @param data       the JSON data to parse
     * @param refHandler the reference handler containing all references in the currently loading save file
     * @param target     the target for this Effect (either an Entity or an Area)
     * @return a newly created Effect with the parsed data loaded
     * @throws LoadGameException
     */

    public static Effect load(SimpleJSONObject data, ReferenceHandler refHandler, EffectTarget target) throws LoadGameException
    {
        Effect effect;
        if (data.containsKey("isAura") && data.get("isAura", false) == true) {
            // all auras must have scripts
            effect = Aura.load(data, refHandler);
        } else
            if (data.containsKey("scriptLocation")) {
                // if this is an effect with a script
                effect = new Effect(data.get("scriptLocation", null));
            } else {
                // if this is an effect without a script
                effect = new Effect();
            }

        effect.setTarget(target);

        // add reference for this effect
        refHandler.add(data.get("ref", null), effect);

        if (data.containsKey("title")) {
            effect.title = data.get("title", null);
        }

        if (data.containsKey("icons")) {
            SimpleJSONArray iconsData = data.getArray("icons");
            for (SimpleJSONArrayEntry entry : iconsData) {
                effect.icons.add(IconFactory.createIcon(entry.getObject()));
            }
        }

        effect.duration = data.get("duration", 0);
        effect.removeOnDeactivate = data.get("removeOnDeactivate", false);
        effect.hasDescription = data.get("hasDescription", false);

        if (data.containsKey("children")) {
            SimpleJSONArray arrayData = data.getArray("children");
            for (SimpleJSONArrayEntry entry : arrayData) {
                String childRef = entry.getString();

                // add the reference to the child to be resolved later,
                // after all effects are loaded
                refHandler.addChildEffect(effect, childRef);
            }
        }

        if (data.containsKey("slot")) {
            refHandler.addSlotReference(effect, data.get("slot", null));
        }

        BonusList bonusList = new BonusList();
        effect.bonuses = bonusList;

        if (data.containsKey("bonuses")) {
            for (SimpleJSONArrayEntry entry : data.getArray("bonuses")) {
                Bonus bonus = (Bonus)SaveGameUtil.loadObject(entry.getObject());
                bonusList.add(bonus);
            }
        }

        // load animations if present
        if (data.containsKey("animations")) {
            for (SimpleJSONArrayEntry entry : data.getArray("animations")) {
                effect.animations.add((Animated)SaveGameUtil.loadObject(entry.getObject()));
            }
        }

        if (data.containsKey("scriptState")) {
            effect.scriptState.load(data.getObject("scriptState"));
        }

        // don't start animations on any effects yet

        return effect;
    }

    /**
     * Create a new Effect that applies no penalties or bonuses.  The creator
     * of this Effect is responsible for keeping track of its created Effects
     * each round and elapsing time.
     */

    public Effect()
    {
        super(null, null, false);
        this.bonuses = new BonusList();
        this.title = "Effect";
        this.hasDescription = true;
        this.icons = new ArrayList<Icon>();

        this.animations = new ArrayList<Animated>(1);
        this.childEffects = new ArrayList<Effect>(1);

        this.scriptState = new ScriptState();
    }

    /**
     * Create a new Effect that applies no penalties or bonuses.  The creator
     * of this Effect is responsible for keeping track of its created Effects
     * each round and elapsing time.
     * <p>
     * The specified script should contain an onApply method and any other
     * methods needed for this Effect in order to be useful.  Strictly speaking,
     * however, all script functions are optional.
     *
     * @param scriptID the Script Resource location of the script to use for this Effect
     */

    public Effect(String scriptID)
    {
        super(ResourceManager.getScriptResourceAsString(scriptID), scriptID, false);
        this.bonuses = new BonusList();
        this.hasDescription = true;
        this.icons = new ArrayList<Icon>();
        this.animations = new ArrayList<Animated>(1);
        this.childEffects = new ArrayList<Effect>(1);

        this.scriptState = new ScriptState();
    }

    /**
     * Creates a new Effect, copying the internal fields of the specified other Effect.
     *
     * @param other the Effect to copy
     */

    public Effect(Effect other, EffectTarget target)
    {
        super(other);

        this.target = target;
        this.bonuses = new BonusList(other.bonuses);
        this.duration = other.duration;
        this.title = other.title;
        this.icons = new ArrayList<Icon>(other.icons);
        this.scriptState = new ScriptState(other.scriptState);

        this.animations = new ArrayList<Animated>(1);
        this.childEffects = new ArrayList<Effect>(1);
    }

    /**
     * Sets whether this Effect will show a description string on various screens that
     * list effects.  Note that permanent effects never show a description.
     *
     * @param hasDescription whether to show a description for this effect
     */

    public void setHasDescription(boolean hasDescription)
    {
        this.hasDescription = hasDescription;
    }

    /**
     * Adds a simple icon with the specified sprite ID and a red tint
     * denoting a negative effect to this effect
     *
     * @param imageID
     */

    public void addNegativeIcon(String imageID)
    {
        this.icons.add(IconFactory.createIcon(imageID, Color.RED));
    }

    /**
     * Adds a simple icon with the specified sprite ID and a blue / green tint
     * denoting a positive effect to this effect
     *
     * @param imageID
     */

    public void addPositiveIcon(String imageID)
    {
        this.icons.add(IconFactory.createIcon(imageID, Color.AQUA));
    }

    /**
     * Adds the simple icon with the specified sprite ID to the list of
     * icons for this effect
     *
     * @param imageID
     */

    public void addIcon(String imageID)
    {
        this.icons.add(IconFactory.createIcon(imageID));
    }

    /**
     * Adds all the icons for this effect to the specified collection of icons
     *
     * @param icons
     */

    public void getIcons(Collection<Icon> icons)
    {
        icons.addAll(this.icons);
    }

    /**
     * Sets the title for this Effect to the specified String.  This title is
     * displayed when displaying the Effect in the user interface.
     *
     * @param title the title for this Effect
     */

    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * Returns the title for this Effect.  See {@link #setTitle(String)}.
     *
     * @return the title for this Effect
     */

    public String getTitle()
    {
        return title;
    }

    /**
     * Returns the BonusList associated with this Effect.  When an
     * Effect is applied, bonuses are also applied to the target Entity
     *
     * @return the BonusList associated with this Effect
     */

    public BonusList getBonuses()
    {
        return bonuses;
    }

    /**
     * Sets the number of rounds duration remaining for this Effect to the
     * specified value.
     *
     * @param duration the number of rounds that this Effect will continue
     *                 to be active.
     */

    public void setDuration(int duration)
    {
        this.duration = duration;
    }

    /**
     * Decrease the number of rounds remaining for this Effect by the specified value
     *
     * @param rounds the number of rounds to elapse
     */

    public void elapseRounds(int rounds)
    {
        if (this.duration != 0) {
            this.duration -= rounds;
        }

        // note that this is only being executed once even if we elapse many rounds
        this.executeFunction(ScriptFunctionType.onRoundElapsed, this);
    }

    /**
     * Returns the number of rounds of duration remaining for this Effect.
     *
     * @return the number of rounds of duration remaining for this Effect
     */

    public int getRoundsRemaining()
    {
        return duration;
    }

    /**
     * Sets whether this Effect should be removed when the parent AbilitySlot is
     * deactivated.  This is useful for modes that should remain in effect until
     * manually deactivated.
     * <p>
     * If removeOnDeactivate is set to true, then this Effect will not be removed
     * due to its duration ending.  It will only be removed when the parent AbilitySlot
     * is deactivated.
     * <p>
     * If removeOnDeactivate is set to false, then this Effect will be removed
     * due to duration as normal.
     *
     * @param removeOnDeactivate whether this Effect should be removed when its
     *                           parent AbilitySlot is deactivated
     */

    public void setRemoveOnDeactivate(boolean removeOnDeactivate)
    {
        this.removeOnDeactivate = removeOnDeactivate;
    }

    /**
     * Returns whether this Effect should be removed when its parent AbilitySlot
     * is deactivated.  See {@link #setRemoveOnDeactivate(boolean)}.
     *
     * @return whether this Effect should be removed onDeactivate
     */

    public boolean removeOnDeactivate()
    {
        return removeOnDeactivate;
    }

    /**
     * Returns the target EffectTarget for this Effect
     *
     * @return the target EffectTarget for this Effect
     */

    public EffectTarget getTarget()
    {
        return target;
    }

    /**
     * Sets the target for this Effect to the specified EffectTarget
     *
     * @param target the target for this Effect
     */

    public void setTarget(EffectTarget target)
    {
        this.target = target;

        // if this effect was created by an ability
        if (slot != null) {
            // modify the duration if needed
            slot.getAbility().setSpellDuration(this, slot.getParent());
        }
    }

    /**
     * Removes the specified Effect from the List of child effects for this Effect.  If the specified Effect is
     * not present in the list of children, no action is taken.
     *
     * @param effect the Effect to remove
     */

    public void removeChildEffect(Effect effect)
    {
        this.childEffects.remove(effect);
    }

    /**
     * Adds the specified Effect to the List of child effects of this Effect.  This method is not neccesary unless
     * this Effect's script needs to keep track of the child Effect
     *
     * @param effect the child Effect
     */

    public void addChildEffect(Effect effect)
    {
        this.childEffects.add(effect);
    }

    /**
     * Returns all child effects of this effect
     *
     * @return all child effects
     */

    public List<Effect> getChildEffects()
    {
        List<Effect> effects = new ArrayList<Effect>();

        for (Effect effect : childEffects) {
            effects.add(effect);
        }

        return effects;
    }

    /**
     * Returns the child effect from this Effect's list of children added via {@link #addChildEffect(Effect)} with
     * the specified target, or null if no matching Effect is found
     *
     * @param target the EffectTarget for the returned Effect
     * @return the first child effect found with the specified EffectTarget
     */

    public Effect getChildEffectWithTarget(EffectTarget target)
    {
        for (Effect effect : childEffects) {
            if (effect.getTarget() == target) return effect;
        }

        return null;
    }

    /**
     * Returns the List of all child effects from this Effect's list of children added via {@link #addChildEffect(Effect)}
     * with the specified target, or an empty list if no effects are found
     *
     * @param target the EffectTarget for the returned children
     * @return all children with the specified Target
     */

    public List<Effect> getChildEffectsWithTarget(EffectTarget target)
    {
        List<Effect> effects = new ArrayList<Effect>();

        for (Effect effect : childEffects) {
            if (effect.getTarget() == target) effects.add(effect);
        }

        return effects;
    }

    /**
     * Sets the AbilitySlot associated with this Effect.  This should
     * be the AbilitySlot responsible for creating the Effect.  The Ability
     * contained in this slot can potentially modify the duration of the effect
     * in the event the target has SpellResistance
     *
     * @param slot the AbilitySlot associated with this Effect
     */

    public void setSlot(AbilitySlot slot)
    {
        this.slot = slot;
    }

    /**
     * Returns the AbilitySlot associated with this Effect.  If this
     * Effect was not created by an AbilitySlot, returns null
     *
     * @return the AbilitySlot associated with this Effect
     */

    public AbilitySlot getSlot()
    {
        return slot;
    }

    /**
     * Adds the specified animation to the List of animations that will
     * be run when this Effect is applied.
     *
     * @param animation the Animated to add
     */

    public void addAnimation(Animated animation)
    {
        this.animations.add(animation);
    }

    /**
     * Starts all Animated objects associated with this Effect
     */

    public void startAnimations()
    {
        for (Animated animation : this.animations) {

            Game.particleManager.add(animation);
        }
    }

    /**
     * Stops all Animated objects associated with this Effect
     */

    public void endAnimations()
    {
        for (Animated animation : animations) {
            animation.setDuration(0.0f);
        }
    }

    /**
     * Moves the position of all active animations by the specified amount
     * in screen coordinates.
     *
     * @param x the x screen coordinate to move by
     * @param y the y screen coordinate to move by
     */

    public void offsetAnimationPositions(float x, float y)
    {
        for (Animated animation : animations) {
            animation.offsetPosition(x, y);
        }
    }

    /**
     * Appends the standard description for this Effect with name, duration, and
     * any bonuses to the specified StringBuilder
     *
     * @param sb the StringBuilder to append to
     */

    public void appendDescription(StringBuilder sb)
    {
        // don't append description for permanent effects
        if (this.duration == 0 && !this.removeOnDeactivate) return;

        // don't append description if the effect has been set as not showing a description
        if (!hasDescription) return;

        sb.append("<div style=\"margin-top: 1em;\">");
        sb.append("<span style=\"font-family: medium;\">");
        sb.append(this.title).append("</span>");

        if (this.duration != 0) {
            sb.append("<p>(<span style=\"font-family: blue;\">");
            sb.append(this.duration).append("</span> Rounds Remaining)</p>");
        }

        sb.append(this.bonuses.getDescription());

        sb.append("</div>");
    }

    public void put(String ref, Object data)
    {
        scriptState.put(ref, data);
    }

    public Object get(String ref)
    {
        return scriptState.get(ref);
    }
}
