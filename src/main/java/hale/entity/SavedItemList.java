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

package hale.entity;

import hale.util.SimpleJSONObject;

/**
 * An item list that has been saved on disk, with some associated additional
 * data
 *
 * @author Jared
 */

public class SavedItemList extends ItemList
{
    private final int enchantmentPercentage;
    private final int enchantmentSkillModifier;

    public SavedItemList(SimpleJSONObject data)
    {
        super(data.getArray("items"));

        if (data.containsKey("enchantmentChance")) {
            this.enchantmentPercentage = data.get("enchantmentChance", 0);
        } else {
            this.enchantmentPercentage = 0;
        }

        if (data.containsKey("enchantmentSkillModifier")) {
            this.enchantmentSkillModifier = data.get("enchantmentSkillModifier", 0);
        } else {
            this.enchantmentSkillModifier = 0;
        }
    }

    /**
     * Returns the percentage chance that a given item which has valid enchantments,
     * should be enchanted, when generating loot from this list
     *
     * @return the chance of an item being enchanted
     */

    public int getEnchantmentChance()
    {
        return enchantmentPercentage;
    }

    /**
     * Returns the skill modifier that should be used when enchanting an item as
     * part of generating loot from this item list
     *
     * @return the skill modifier for enchantments
     */

    public int getEnchantmentSkillModifier()
    {
        return enchantmentSkillModifier;
    }
}
