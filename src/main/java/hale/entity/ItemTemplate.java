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

package main.java.hale.entity;

import main.java.hale.Game;
import main.java.hale.ability.ScriptFunctionType;
import main.java.hale.rules.Quality;
import main.java.hale.util.SimpleJSONObject;

/**
 * The class containing the immutable parts of an Item
 *
 * @author Jared
 */

public class ItemTemplate extends EntityTemplate
{

    // the item's quality, or null if it has no quality
    private final Quality defaultQuality;

    // the value of the item in units such that 1 Copper Piece (CP) = 100
    private final int valueInCPOver100;

    private final int weightInGrams;

    // whether this item is marked as an ingredient for recipes in the player's inventory
    private final boolean isIngredient;

    // whether this is a quest item.  Quest items cannot be dropped or sold by the player
    // once obtained
    private final boolean isQuest;

    private final String useText;
    private final int useAP;

    /**
     * Creates a new ItemTemplate
     *
     * @param id   the entity ID of the template
     * @param data the JSON data to parse
     */

    public ItemTemplate(String id, SimpleJSONObject data)
    {
        super(id, data);

        if (data.containsKey("defaultQuality")) {
            String qualityID = data.get("defaultQuality", null);

            this.defaultQuality = Game.ruleset.getItemQuality(qualityID);
        } else {
            // item has no quality
            this.defaultQuality = null;
        }

        this.valueInCPOver100 = data.get("value", 0);

        this.weightInGrams = data.get("weight", 0);

        if (data.containsKey("isIngredient")) {
            this.isIngredient = data.get("isIngredient", false);
        } else {
            this.isIngredient = false;
        }

        if (data.containsKey("isQuest")) {
            this.isQuest = data.get("isQuest", false);
        } else {
            this.isQuest = false;
        }

        if (!data.containsKey("script")) {
            this.useText = null;
            this.useAP = 0;
        } else {
            if (data.containsKey("useText")) {
                this.useText = data.get("useText", null);
            } else {
                this.useText = null;
            }

            if (data.containsKey("useAP")) {
                this.useAP = data.get("useAP", 0);
            } else {
                this.useAP = 0;
            }
        }
    }

    /**
     * Creates an ItemTemplate which is a copy of the specified Template, except for the ID
     * and the createdItem properties
     *
     * @param id          the ID of the new template
     * @param other       the template to copy
     * @param createdItem
     */

    protected ItemTemplate(String id, ItemTemplate other, CreatedItem createdItem)
    {
        super(id, other, createdItem);

        this.defaultQuality = other.defaultQuality;
        this.valueInCPOver100 = createdItem.getModifiedValue(other.valueInCPOver100);
        this.weightInGrams = other.weightInGrams;
        this.isIngredient = other.isIngredient;
        this.isQuest = other.isQuest;
        this.useText = other.useText;
        this.useAP = other.useAP;
    }

    @Override
    public Item createInstance()
    {
        return new Item(this);
    }

    /**
     * Returns true if this item has a quality modifier, false otherwise
     *
     * @return whether this item has a quality modifier
     */

    public boolean hasQuality()
    {
        return defaultQuality != null;
    }

    /**
     * Returns the item quality for this item, or null if this item does
     * not have a quality modifier
     *
     * @return the item quality
     */

    public Quality getDefaultQuality()
    {
        return defaultQuality;
    }

    /**
     * Returns the value of this item in a unit such that 1 Copper Piece
     * equals 100
     *
     * @return the value of this item
     */

    public int getValueInCPOver100()
    {
        return valueInCPOver100;
    }

    /**
     * Returns the weight of this item in grams ( 1 kilogram = 1000 grams)
     *
     * @return the weight of this item
     */

    public int getWeightInGrams()
    {
        return weightInGrams;
    }

    /**
     * Returns true if this item is marked to appear in the interface as a recipe
     * ingredient, false otherwise.  Note that recipes can use items not marked
     * as ingredients in the interface as ingredients in some special cases
     *
     * @return whether this item is marked as an ingredient
     */

    public boolean isIngredient()
    {
        return isIngredient;
    }

    /**
     * Returns true if this item is a quest item (cannot be sold or dropped once
     * obtained), false otherwise
     *
     * @return whether this is a quest item
     */

    public boolean isQuest()
    {
        return isQuest;
    }

    /**
     * Returns true if and only if this Item is usable.  This means it
     * can be used via the inventory right click menu
     *
     * @return true if and only if this Item is usable
     */

    public boolean isUsable()
    {
        if (hasScript()) {
            return getScript().hasFunction(ScriptFunctionType.onUse);
        }

        return false;
    }

    /**
     * Returns the text that is associated with using this item, it
     * is normally displayed in the interface; or null if this item
     * is not usable
     *
     * @return the use text
     */

    public String getUseText()
    {
        return useText;
    }

    /**
     * Returns the number of action points required to use this item, or
     * 0 if this item is not usable.  Some usable items may use a dynamic
     * amount of AP.  In this case, this method will generally return 0
     *
     * @return the number of action points required to use this item
     */

    public int getUseAP()
    {
        return useAP;
    }

    /**
     * Returns true if the parent creature has the prereqs to equip this item, false
     * otherwise.  Note that only EquippableItems can be equipped, even though this
     * method will return true for regular Items.  It is testing only if the parent
     * has neccessary prereqs, not whether the item can actually be equipped.
     *
     * @param parent
     * @return whether the specified creature has the prereqs to equip this item
     */

    public boolean hasPrereqsToEquip(Creature parent)
    {
        return true;
    }
}
