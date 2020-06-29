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

import java.util.ArrayList;
import java.util.List;

import main.java.hale.icon.Icon;
import main.java.hale.loading.Saveable;
import main.java.hale.util.SimpleJSONObject;

/**
 * A user created (enchanted) item that needs to be stored separately for saving / loading purposes
 *
 * @author Jared
 */

public class CreatedItem implements Saveable
{
    private final CreatedItemModel model;
    private final EquippableItemTemplate template;

    @Override
    public Object save()
    {
        return model.save();
    }

    /**
     * Creates a new CreatedItem from the data in the specified JSON object
     *
     * @param data the data to load
     * @return the new CreatedItem
     */

    public static CreatedItem load(SimpleJSONObject data)
    {
        CreatedItemModel model = CreatedItemModel.load(data);

        return new CreatedItem(model);
    }

    /**
     * Creates a new CreatedItem based on the specified CreatedItemModel
     *
     * @param model the model to base this createdItem on
     */

    public CreatedItem(CreatedItemModel model)
    {
        this.model = new CreatedItemModel(model);

        EquippableItemTemplate baseTemplate = (EquippableItemTemplate)EntityManager.getItemTemplate(model.getBaseItemID());
        this.template = baseTemplate.createModifiedCopy(model.getCreatedItemID(), this);
    }

    /**
     * Returns the template for the new created item
     *
     * @return the template
     */

    public EquippableItemTemplate getTemplate()
    {
        return template;
    }

    /**
     * Returns the ID of the base (resource) item
     *
     * @return the ID of the base item
     */

    public String getBaseItemID()
    {
        return model.getBaseItemID();
    }

    /**
     * Returns the ID of the created item
     *
     * @return the ID of the created item
     */

    public String getCreatedItemID()
    {
        return model.getCreatedItemID();
    }

    /**
     * Gets the list of enchantments that have been user added to this created item
     *
     * @return
     */

    public List<Enchantment> getEnchantments()
    {
        List<Enchantment> enchantments = new ArrayList<Enchantment>();
        for (String script : model.getEnchantments()) {
            enchantments.add(new Enchantment(script, true));
        }

        return enchantments;
    }

    /**
     * Gets a new name modified based on the prefix, baseName, and postfix
     *
     * @param baseName
     * @return a new, modified name
     */

    public String getModifiedName(String baseName)
    {
        return model.getModifiedName(baseName);
    }

    /**
     * Returns the isUnequippable status of this createdItem, based on the
     * status of the specified template
     *
     * @param other
     * @return the isUnequippable status
     */

    public boolean getIsUnequippable(EquippableItemTemplate other)
    {
        return model.getIsUnequippable(other);
    }

    /**
     * Modifies the specified value by the valueModifier of this CreatedItem,
     * and returns the result.  Note that the baseValue is multiplied by the value
     * modifier as a percentage, and in addition a minimum value based on the
     * modifier is also added
     *
     * @param baseValue
     * @return the modified value
     */

    public int getModifiedValue(int baseValue)
    {
        return model.getModifiedValue(baseValue);
    }

    /**
     * Returns a modified icon consisting of the base icon with this created item's
     * overlay icon on top
     *
     * @param baseIcon
     * @return a modified icon
     */

    public Icon getModifiedIcon(Icon baseIcon)
    {
        return model.getModifiedIcon(baseIcon);
    }
}
