/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2012 Jared Stephen
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

package main.java.hale.loading;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.java.hale.ability.AbilitySlot;
import main.java.hale.ability.Effect;
import main.java.hale.area.Area;
import main.java.hale.entity.Entity;
import main.java.hale.util.Logger;

/**
 * A class for adding references that appear in saved game files and then later
 * retrieving the object associated with a given reference
 *
 * @author Jared
 */

public class ReferenceHandler
{
    private Map<String, Area> areaRefs;
    private Map<String, Entity> entityRefs;
    private Map<String, Effect> effectRefs;
    private Map<String, AbilitySlot> slotRefs;

    private Map<Effect, List<String>> effectsWithChildren;
    private Map<Effect, String> effectSlotReferences;
    private Map<AbilitySlot, List<String>> slotChildren;

    /**
     * Creates a new, empty reference handler
     */

    public ReferenceHandler()
    {
        areaRefs = new HashMap<String, Area>();
        entityRefs = new HashMap<String, Entity>();
        effectRefs = new HashMap<String, Effect>();
        slotRefs = new HashMap<String, AbilitySlot>();

        effectsWithChildren = new HashMap<Effect, List<String>>();

        effectSlotReferences = new HashMap<Effect, String>();

        slotChildren = new HashMap<AbilitySlot, List<String>>();
    }

    /**
     * Adds the effect with the specified reference as a child effect for the
     * specified parent.  This reference will be resolved once all effects are loaded
     *
     * @param parent   the parent effect
     * @param childRef the reference to the child effect
     */

    public void addChildEffect(Effect parent, String childRef)
    {
        List<String> effects = effectsWithChildren.get(parent);

        if (effects == null) {
            effects = new ArrayList<String>();
            effectsWithChildren.put(parent, effects);
        }

        effects.add(childRef);
    }

    /**
     * Adds the effect with the specified reference as a child for the specified ability slot
     * This reference will be resolved once all effects have been loaded
     *
     * @param parent   the parent ability slot
     * @param childRef the reference to the child effect
     */

    public void addEffectReference(AbilitySlot parent, String childRef)
    {
        List<String> effects = slotChildren.get(parent);

        if (effects == null) {
            effects = new ArrayList<String>();
            slotChildren.put(parent, effects);
        }

        effects.add(childRef);
    }

    /**
     * Adds the specified effect to have its ability slot resolved to the
     * specified reference after all ability slots have been loaded
     *
     * @param parent  the parent effect
     * @param slotRef the reference to the ability slot
     */

    public void addSlotReference(Effect parent, String slotRef)
    {
        effectSlotReferences.put(parent, slotRef);
    }

    /**
     * Resolves all outstanding registered references
     */

    public void resolveAllReferences()
    {
        // resolve effect child references
        for (Effect parent : effectsWithChildren.keySet()) {
            for (String childRef : effectsWithChildren.get(parent)) {
                Effect child = effectRefs.get(childRef);

                if (child == null) {
                    Logger.appendToErrorLog("Error resolving reference for child effect " + childRef);
                } else {
                    parent.addChildEffect(child);
                }
            }
        }

        // resolve effect slot references
        for (Effect parent : effectSlotReferences.keySet()) {
            String abilitySlotRef = effectSlotReferences.get(parent);

            AbilitySlot slot = this.slotRefs.get(abilitySlotRef);

            if (slot == null) {
                Logger.appendToErrorLog("Error resolving reference for ability slot " + abilitySlotRef);
            } else {
                parent.setSlot(slot);
            }
        }

        // resolve slot child effect references
        for (AbilitySlot parent : slotChildren.keySet()) {
            List<Effect> effects = new ArrayList<Effect>();

            for (String childRef : slotChildren.get(parent)) {
                Effect child = effectRefs.get(childRef);

                if (child == null) {
                    Logger.appendToErrorLog("Error resolving reference for slot effect " + childRef);
                } else {
                    effects.add(child);
                }
            }

            parent.loadActiveEffects(effects);
        }
    }

    /**
     * Adds a reference for the specified area to the set of references
     *
     * @param ref  the reference for the area to add
     * @param area the area being references
     */

    public void add(String ref, Area area)
    {
        areaRefs.put(ref, area);
    }

    /**
     * Returns the area with the specified reference or null if no area
     * is found with that reference
     *
     * @param ref the reference for the specified area
     * @return the area with the specified reference
     */

    public Area getArea(String ref)
    {
        return areaRefs.get(ref);
    }

    /**
     * Adds a reference for the specified entity to the set of references
     *
     * @param ref    the reference for the entity to add
     * @param entity the entity being references
     */

    public void add(String ref, Entity entity)
    {
        entityRefs.put(ref, entity);
    }

    /**
     * Returns the entity with the specified reference or null if no entity
     * is found with that reference
     *
     * @param ref the reference for the specified entity
     * @return the entity with the specified reference
     */

    public Entity getEntity(String ref)
    {
        return entityRefs.get(ref);
    }

    /**
     * Adds a reference for the specified effect to the set of references
     *
     * @param ref    the reference for the effect to add
     * @param effect the effect being referenced
     */

    public void add(String ref, Effect effect)
    {
        effectRefs.put(ref, effect);
    }

    /**
     * Returns the effect with the specified reference or null if no effect
     * is found with that reference
     *
     * @param ref the reference for the specified effect
     * @return the effect with the specified reference
     */

    public Effect getEffect(String ref)
    {
        return effectRefs.get(ref);
    }

    /**
     * Adds a reference for the specified abilitySlot to the set of references
     *
     * @param ref         the reference for the abilitySlot to add
     * @param abilitySlot the abilitySlot being referenced
     */

    public void add(String ref, AbilitySlot abilitySlot)
    {
        slotRefs.put(ref, abilitySlot);
    }

    /**
     * Returns the abilitySlot with the specified reference or null if no abilitySlot
     * is found with that reference
     *
     * @param ref the reference for the specified abilitySlot
     * @return the abilitySlot with the specified reference
     */

    public AbilitySlot getAbilitySlot(String ref)
    {
        return slotRefs.get(ref);
    }
}
