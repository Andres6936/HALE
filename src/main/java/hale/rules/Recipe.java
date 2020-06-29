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

package main.java.hale.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import main.java.hale.Game;
import main.java.hale.entity.CreatedItem;
import main.java.hale.entity.CreatedItemModel;
import main.java.hale.entity.Creature;
import main.java.hale.entity.EntityManager;
import main.java.hale.entity.EquippableItem;
import main.java.hale.entity.EquippableItemTemplate;
import main.java.hale.entity.Inventory;
import main.java.hale.entity.Item;
import main.java.hale.icon.Icon;
import main.java.hale.icon.IconFactory;
import main.java.hale.util.SimpleJSONArray;
import main.java.hale.util.SimpleJSONArrayEntry;
import main.java.hale.util.SimpleJSONObject;
import main.java.hale.util.SimpleJSONParser;

/**
 * A recipe consists of a list of ingredients and a result.  When a player character with suitable
 * skill and all of the ingredients uses a workbench, they can craft the recipe.  The iterator for
 * this class iterates over the ingredients.
 *
 * @author Jared
 */

public class Recipe implements Iterable<Recipe.Ingredient>
{
    /**
     * A single type of ingredient for a recipe
     *
     * @author Jared
     */

    public class Ingredient
    {
        private final String itemID;
        private final int quantity;

        private Ingredient(String itemID, int quantity)
        {
            this.itemID = itemID;
            this.quantity = quantity;
        }

        /**
         * Returns the item ID of this ingredient
         *
         * @return the item ID
         */

        public String getItemID()
        {
            return itemID;
        }

        /**
         * Returns the required quantity of this ingredient
         *
         * @return the required quantity
         */

        public int getQuantity()
        {
            return quantity;
        }
    }

    private class LevelModifier
    {
        private final Quality quality;
        private final int skillRankRequirement;
        private final String enchantment;
        private final int valueModifier;

        private LevelModifier(SimpleJSONObject data)
        {
            if (data.containsKey("quality")) {
                this.quality = Game.ruleset.getItemQuality(data.get("quality", null));
            } else {
                this.quality = null;
            }


            this.skillRankRequirement = data.get("skillRankRequirement", 0);

            if (data.containsKey("enchantment")) {
                this.enchantment = data.get("enchantment", null);
            } else {
                this.enchantment = null;
            }

            if (data.containsKey("valueModifier")) {
                this.valueModifier = data.get("valueModifier", 100);
            } else {
                this.valueModifier = 100;
            }
        }

        /**
         * Creates an all empty, null modifier that specified no changes to the
         * base item
         */

        private LevelModifier()
        {
            this.quality = null;
            this.skillRankRequirement = 0;
            this.enchantment = null;
            this.valueModifier = 100;
        }
    }

    private final String id;
    private final String name;
    private final String description;

    private final List<Ingredient> ingredients;

    private final List<EquippableItemTemplate.Type> ingredientItemTypes;
    private final String result;
    private final int resultQuantity;

    private final String resultPrefix, resultPostfix;
    private final Icon resultOverlayIcon;

    private final Skill skill;
    private final int skillRankRequirement;

    // note that this list is unordered, we will simply search for the right
    // quality each time we craft
    private final List<LevelModifier> levelModifiers;

    public Recipe(String id, SimpleJSONParser parser)
    {
        this.id = id;
        this.name = parser.get("name", id);
        this.description = parser.get("description", null);
        this.skill = Game.ruleset.getSkill(parser.get("skill", null));
        this.skillRankRequirement = parser.get("skillRankRequirement", 0);

        this.ingredients = new ArrayList<Ingredient>();
        SimpleJSONObject ingredientsIn = parser.getObject("ingredients");
        for (String itemID : ingredientsIn.keySet()) {
            int quantity = ingredientsIn.get(itemID, 0);

            ingredients.add(new Ingredient(itemID, quantity));
        }

        SimpleJSONObject resultIn = parser.getObject("result");

        if (resultIn.containsKey("id")) {
            this.result = resultIn.get("id", null);
            this.resultQuantity = resultIn.get("quantity", 0);
            this.ingredientItemTypes = Collections.emptyList();

            this.resultPostfix = null;
            this.resultPrefix = null;
            this.resultOverlayIcon = null;
        } else {
            this.result = null;
            this.resultQuantity = 0;

            if (resultIn.containsKey("postfix")) {
                this.resultPostfix = resultIn.get("postfix", null);
            } else {
                this.resultPostfix = "";
            }

            if (resultIn.containsKey("prefix")) {
                this.resultPrefix = resultIn.get("prefix", null);
            } else {
                this.resultPrefix = "";
            }

            if (resultIn.containsKey("overlayIcon")) {
                this.resultOverlayIcon = IconFactory.createIcon(resultIn.getObject("overlayIcon"));
            } else {
                this.resultOverlayIcon = IconFactory.emptyIcon;
            }

            List<EquippableItemTemplate.Type> itemTypes = new ArrayList<EquippableItemTemplate.Type>();

            SimpleJSONArray itemTypesIn = resultIn.getArray("ingredientItemTypes");
            for (SimpleJSONArrayEntry entry : itemTypesIn) {
                String typeString = entry.getString();

                EquippableItemTemplate.Type type = EquippableItemTemplate.Type.valueOf(typeString);
                itemTypes.add(type);
            }

            this.ingredientItemTypes = Collections.unmodifiableList(itemTypes);
        }

        if (resultIn.containsKey("levels")) {
            levelModifiers = new ArrayList<LevelModifier>();

            SimpleJSONArray qualitiesIn = resultIn.getArray("levels");

            for (SimpleJSONArrayEntry entry : qualitiesIn) {
                SimpleJSONObject qualityIn = entry.getObject();

                levelModifiers.add(new LevelModifier(qualityIn));
            }

        } else {
            levelModifiers = Collections.emptyList();
        }

        parser.warnOnUnusedKeys();
    }

    /**
     * Returns the ID of this recipe
     *
     * @return the ID
     */

    public String getID()
    {
        return id;
    }

    /**
     * Returns the description of this recipe
     *
     * @return the description
     */

    public String getDescription()
    {
        return description;
    }

    /**
     * Returns the name of this recipe
     *
     * @return the name of this recipe
     */

    public String getName()
    {
        return name;
    }

    /**
     * Returns the ID of the resulting item, or null if this recipe modifies an existing item.
     * <p>
     * See {@link #getIngredientItemTypes()}
     *
     * @return the ID of the resulting item
     */

    public String getResultID()
    {
        return result;
    }

    /**
     * Returns the number of items of the {@link #getResultID()} that are created, or 0
     * if this recipe modifies an existing item.  See {@link #getIngredientItemTypes()}
     *
     * @return the number of items this recipe creates
     */

    public int getResultQuantity()
    {
        return resultQuantity;
    }

    /**
     * Returns the skill used in crafting this recipe
     *
     * @return the skill
     */

    public Skill getSkill()
    {
        return skill;
    }

    /**
     * Returns the number of ranks in the skill specified by {@link #getSkill()} needed
     * to craft this recipe.
     *
     * @return the number of skill ranks needed.
     */

    public int getSkillRankRequirement()
    {
        return skillRankRequirement;
    }

    /**
     * Returns the EquippableItem type or types of item that area needed as an ingredient for this
     * recipe in addition to the listed ingredients, or null if no item type is needed.  If this
     * is non-empty, then the item of the specified type will be modified and will be the result of
     * this recipe.
     *
     * @return the item types needed
     */

    public List<EquippableItemTemplate.Type> getIngredientItemTypes()
    {
        return ingredientItemTypes;
    }

    /**
     * Returns true if this recipe modifies an existing item (of a specified type,
     * see {@link #getIngredientItemTypes()} ), or false if this recipe creates a new item
     *
     * @return whether this recipe modifies an existing item or creates a new one
     */

    public boolean isResultIngredient()
    {
        return !ingredientItemTypes.isEmpty();
    }

    /**
     * Returns true if the item with the specified ID is an ingredient for this recipe,
     * false otherwise
     *
     * @param itemID
     * @return whether the item is an ingredient of this recipe
     */

    public boolean isIngredient(String itemID)
    {
        for (Ingredient ingredient : ingredients) {
            if (ingredient.itemID.equals(itemID)) return true;
        }

        return false;
    }

    /**
     * Returns true if the player character party has all the ingredients needed to
     * craft this recipe, false otherwise
     *
     * @return whether the player character party can craft this recipe
     */

    public boolean canCraft()
    {
        for (Ingredient ingredient : ingredients) {
            if (!Game.curCampaign.party.hasItem(ingredient.itemID, ingredient.quantity)) {
                return false;
            }
        }

        return true;
    }

    private void removeIngredients()
    {
        // remove the ingredients from the party

        for (Ingredient ingredient : ingredients) {
            Game.curCampaign.party.removeItem(ingredient.itemID, ingredient.quantity);
        }
    }

    private LevelModifier findLevelModifier(int skillModifier)
    {
        if (this.levelModifiers.size() == 0) {
            return new LevelModifier();
        }

        LevelModifier bestSoFar = levelModifiers.get(0);

        for (int i = 1; i < levelModifiers.size(); i++) {
            LevelModifier current = levelModifiers.get(i);

            if (current.skillRankRequirement < skillModifier &
                    current.skillRankRequirement > bestSoFar.skillRankRequirement) {
                bestSoFar = current;
            }
        }

        return bestSoFar;
    }

    private void addMessage(Item item, int quantity, Creature parent, boolean equipped)
    {
        StringBuilder sb = new StringBuilder();

        sb.append(item.getLongName());
        sb.append(" ");
        sb.append(skill.getPastTenseVerb());
        sb.append(" and ");

        if (equipped) {
            sb.append("equipped by ");
        } else {
            sb.append("in the inventory of ");
        }

        sb.append(parent.getTemplate().getName());
        sb.append(".");

        if (quantity != 1) {
            sb.insert(0, quantity + "x ");
        }

        Game.mainViewer.addMessage(sb.toString());
        Game.mainViewer.updateInterface();
    }

    /**
     * Creates a new Item which is a modification of the existing base item, based on this recipe.
     * Quality level, value, and other properties are set based on the specified skill modifier.  A
     * createditem is added to the campaign if one does not already exist
     *
     * @param baseItemID      the ID of the base item
     * @param baseItemQuality the quality of the base item, or null for no quality
     * @param skillModifier
     * @return a newly created item
     */

    public EquippableItem createItem(String baseItemID, Quality baseItemQuality, int skillModifier)
    {
        if (!isResultIngredient()) {
            throw new IllegalArgumentException("Cannot create an item by modification for recipe " + getID());
        }

        LevelModifier qualityModifier = findLevelModifier(skillModifier);

        // determine the quality if it is specified
        Quality quality;
        if (baseItemQuality == null || qualityModifier.quality == null) {
            quality = baseItemQuality;
        } else {
            quality = qualityModifier.quality;
        }

        String createdID = baseItemID + "-0x" + Integer.toHexString(qualityModifier.enchantment.hashCode());

        // if this created item has not been created before, add it
        if (!EntityManager.hasEntityTemplate(createdID)) {
            CreatedItemModel model = new CreatedItemModel(baseItemID, createdID);
            if (qualityModifier.enchantment != null) {
                model.addEnchantment(qualityModifier.enchantment);
            }
            model.setNamePostfix(this.resultPostfix);
            model.setNamePrefix(this.resultPrefix);
            model.setValueModifier(qualityModifier.valueModifier);
            model.setOverlayIcon(this.resultOverlayIcon);

            CreatedItem createdItem = model.getCreatedItem();

            Game.curCampaign.addCreatedItem(createdItem);
        }

        // return an instance of the newly registered item
        return (EquippableItem)EntityManager.getItem(createdID, quality);
    }

    /**
     * Craft this recipe, using the specified item as the base item.  This method may only
     * be called on recipes where {@link #isResultIngredient()} is true
     *
     * @param selectedItem the item to use as the result
     * @param owner        the owning creature
     * @param slot         the inventory slot the item is equipped in, or null if the item is not equipped
     */

    public void craft(Item selectedItem, Creature owner, Inventory.Slot slot)
    {
        if (!isResultIngredient()) {
            throw new IllegalArgumentException("Must not call craft with a specified item for recipe " + getID());
        }

        EquippableItem newItem = createItem(selectedItem.getTemplate().getID(), selectedItem.getQuality(),
                Game.curCampaign.getBestPartySkillModifier(skill.getID()));

        removeIngredients();

        if (slot != null) {
            owner.inventory.removeEquippedItem(slot);

            owner.inventory.addAndEquip(newItem, slot);
        } else {
            owner.inventory.getUnequippedItems().remove(selectedItem);
            owner.inventory.getUnequippedItems().add(newItem);
        }

        addMessage(selectedItem, 1, owner, slot != null);
    }

    /**
     * Crafts this recipe.  This method may only be called on recipes where {@link #isResultIngredient()} is
     * false
     */

    public void craft()
    {
        if (!canCraft()) return;

        if (isResultIngredient()) {
            throw new IllegalArgumentException("Must call craft with a specified item for recipe " + id);
        }

        Creature target = Game.curCampaign.party.getSelected();

        removeIngredients();

        LevelModifier qualityModifier = findLevelModifier(Game.curCampaign.getBestPartySkillModifier(skill.getID()));

        // set the quality on the item if it has been set for the recipe
        Item item;
        if (qualityModifier.quality == null) {
            item = EntityManager.getItem(result);
        } else {
            item = EntityManager.getItem(result, qualityModifier.quality);
        }

        // add the new item
        int quantity = Math.max(1, resultQuantity);

        target.inventory.getUnequippedItems().add(item, quantity);

        addMessage(item, quantity, target, false);
    }

    /**
     * The iterator over the recipe ingredients
     */

    @Override
    public Iterator<Ingredient> iterator()
    {
        return new IngredientIterator();
    }

    private class IngredientIterator implements Iterator<Ingredient>
    {
        private int index = 0;

        @Override
        public boolean hasNext()
        {
            return index < ingredients.size();
        }

        @Override
        public Ingredient next()
        {
            Ingredient next = ingredients.get(index);

            index++;

            return next;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException("Ingredients may not be removed from recipes");
        }
    }
}
