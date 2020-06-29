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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hale.Game;
import hale.rules.Quality;
import hale.rules.Recipe;
import hale.util.Logger;
import hale.util.SimpleJSONArray;
import hale.util.SimpleJSONArrayEntry;
import hale.util.SimpleJSONObject;

/**
 * A class for generating loot (treasure) from a treature chest or hostile creature
 *
 * @author Jared
 */

public class LootList
{
    private final List<Entry> entries;

    /**
     * The type of loot generation that a given list uses
     *
     * @author Jared
     */

    private enum ProbabilityMode
    {
        /**
         * There is an individual probability for each item to be drawn
         */
        PerItem,

        /**
         * One item is drawn from the entire list with the specified probability
         */
        PerList;
    }

    ;

    /**
     * Generates a LootList from the specified JSON data
     *
     * @param data the JSON to parse
     */

    public LootList(SimpleJSONArray data)
    {
        entries = new ArrayList<Entry>();

        for (SimpleJSONArrayEntry entry : data) {
            SimpleJSONObject lootObject = entry.getObject();

            String itemListID = lootObject.get("id", null);

            SimpleJSONObject probObject = lootObject.getObject("probability");

            int probability = probObject.get("value", 0);

            ProbabilityMode mode = ProbabilityMode.valueOf(probObject.get("mode", null));

            entries.add(new Entry(itemListID, probability, mode));
        }
    }

    /**
     * Generates an ItemList containing items generated according to the rules
     * of this loot list
     *
     * @return an itemlist with the generated items
     */

    public ItemList generate()
    {
        ItemList loot = new ItemList();

        for (Entry lootEntry : entries) {
            SavedItemList entryItems = Game.ruleset.getItemList(lootEntry.itemListID);

            if (entryItems == null) {
                Logger.appendToErrorLog("Attempting to generate loot list but item list " +
                        lootEntry.itemListID + " does not exist.");
                continue;
            }

            // can't generate an item if there are none to choose from
            if (entryItems.size() == 0) continue;

            switch (lootEntry.mode) {
                case PerList:
                    if (Game.dice.rand(1, 100) > lootEntry.probability) {
                        break;
                    }

                    addRandomQuantityEntry(entryItems.getRandomEntry(), loot,
                            entryItems.getEnchantmentChance(), entryItems.getEnchantmentSkillModifier());
                    break;
                case PerItem:
                    for (ItemList.Entry perItemEntry : entryItems) {
                        if (Game.dice.rand(1, 100) > lootEntry.probability) {
                            continue;
                        }

                        addRandomQuantityEntry(perItemEntry, loot, entryItems.getEnchantmentChance(),
                                entryItems.getEnchantmentSkillModifier());
                    }
                    break;
            }
        }

        return loot;
    }

    private void addRandomQuantityEntry(ItemList.Entry entry, ItemList loot,
                                        int enchantmentChance, int enchantmentSkillModifier)
    {
        // randomize quantity unless it is infinite
        int quantity = (entry.getQuantity() == Integer.MAX_VALUE) ?
                Integer.MAX_VALUE : Game.dice.rand(1, entry.getQuantity());

        String itemID = entry.getID();
        Quality quality = entry.getQuality();

        // if the item is equippable, check for a random enchantment
        List<String> usableRecipes = getUsableRecipes(entry, enchantmentChance);
        if (usableRecipes.size() > 0) {
            int index = usableRecipes.size() == 1 ? 0 : Game.dice.rand(0, usableRecipes.size() - 1);
            Recipe recipe = Game.curCampaign.getRecipe(usableRecipes.get(index));
            Item newItem = recipe.createItem(entry.getID(), entry.getQuality(), enchantmentSkillModifier);

            itemID = newItem.getTemplate().getID();
            quality = newItem.getQuality();
        }

        loot.add(itemID, quality, quantity);
    }

    private List<String> getUsableRecipes(ItemList.Entry entry, int enchantmentChance)
    {
        ItemTemplate itemTemplate = EntityManager.getItemTemplate(entry.getID());

        // only equippable items can be enchanted
        if (!(itemTemplate instanceof EquippableItemTemplate)) return Collections.emptyList();

        EquippableItemTemplate equippableTemplate = (EquippableItemTemplate)itemTemplate;

        // only items without a current enchantment can be enchanted
        if (equippableTemplate.getEnchantments().size() > 0) return Collections.emptyList();

        if (Game.dice.rand(1, 100) <= enchantmentChance) {
            return Game.curCampaign.getEnchantmentsForItemType(equippableTemplate.getType());
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * A single entry in a loot list
     *
     * @author Jared
     */

    private class Entry
    {
        /**
         * The itemList that this LootEntry will draw items from
         */
        private final String itemListID;

        /**
         * The probability of drawing from the list, depending on the mode
         */
        private final int probability;

        /**
         * The probability mode that this entry operates under
         */
        private final ProbabilityMode mode;

        /**
         * Creates a new LootList entry based on the specified parameters
         *
         * @param itemListID  the ID of the item list to use
         * @param probability the probability of generating the item
         * @param mode        the mode
         */

        private Entry(String itemListID, int probability, ProbabilityMode mode)
        {
            this.itemListID = itemListID;
            this.probability = probability;
            this.mode = mode;
        }
    }
}
