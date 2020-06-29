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

package hale.entity;

import java.util.ArrayList;
import java.util.List;

import hale.icon.ComposedIcon;
import hale.icon.Icon;
import hale.icon.IconFactory;
import hale.loading.JSONOrderedObject;
import hale.loading.Saveable;
import hale.util.SimpleJSONArrayEntry;
import hale.util.SimpleJSONObject;

/**
 * A mutable model for a created item.  This model is modified by a class, and then a new,
 * immutable, createdItem is created by calling {@link #getCreatedItem()}
 *
 * @author Jared
 */

public class CreatedItemModel implements Saveable
{
    private final String baseItemID;
    private final String createdItemID;

    private String namePrefix, namePostfix;

    private boolean forceNotUnequippable;

    private int valueModifier;
    private Icon overlayIcon;

    private final List<String> enchantments;

    @Override
    public JSONOrderedObject save()
    {
        JSONOrderedObject data = new JSONOrderedObject();

        data.put("baseItemID", baseItemID);
        data.put("createdItemID", createdItemID);

        if (!enchantments.isEmpty()) {
            data.put("scripts", enchantments.toArray());
        }

        if (namePrefix != null) {
            data.put("namePrefix", namePrefix);
        }

        if (namePostfix != null) {
            data.put("namePostfix", namePostfix);
        }

        if (forceNotUnequippable) {
            data.put("forceNotUnequippable", true);
        }

        if (valueModifier != 100) {
            data.put("valueModifier", valueModifier);
        }

        if (overlayIcon != IconFactory.emptyIcon) {
            data.put("overlayIcon", overlayIcon.save());
        }

        return data;
    }

    /**
     * Creates a new CreatedItemModel by parsing the specified JSON
     *
     * @param data
     * @return a new CreatedItemModel
     */

    public static CreatedItemModel load(SimpleJSONObject data)
    {
        CreatedItemModel model = new CreatedItemModel(data.get("baseItemID", null), data.get("createdItemID", null));

        if (data.containsKey("scripts")) {
            for (SimpleJSONArrayEntry entry : data.getArray("scripts")) {
                model.enchantments.add(entry.getString());
            }
        }

        if (data.containsKey("namePrefix")) {
            model.namePrefix = data.get("namePrefix", null);
        }

        if (data.containsKey("namePostfix")) {
            model.namePostfix = data.get("namePostfix", null);
        }

        if (data.containsKey("forceNotUnequippable")) {
            model.forceNotUnequippable = data.get("forceNotUnequippable", false);
        }

        if (data.containsKey("valueModifier")) {
            model.valueModifier = data.get("valueModifier", 100);
        } else {
            model.valueModifier = 100;
        }

        if (data.containsKey("overlayIcon")) {
            model.overlayIcon = IconFactory.createIcon(data.getObject("overlayIcon"));
        } else {
            model.overlayIcon = IconFactory.emptyIcon;
        }

        return model;
    }

    /**
     * Creates a new model, modifying the specified base item to create an item with the specified
     * ID
     *
     * @param baseItemID
     * @param createdItemID
     */

    public CreatedItemModel(String baseItemID, String createdItemID)
    {
        this.baseItemID = baseItemID;
        this.createdItemID = createdItemID;
        this.enchantments = new ArrayList<String>();
        this.overlayIcon = IconFactory.emptyIcon;
    }

    /**
     * Creates a copy of the specified CreatedItemModel
     *
     * @param other
     */

    protected CreatedItemModel(CreatedItemModel other)
    {
        this.baseItemID = other.baseItemID;
        this.createdItemID = other.createdItemID;
        this.enchantments = new ArrayList<String>(other.enchantments);
        this.namePostfix = other.namePostfix;
        this.namePrefix = other.namePrefix;
        this.forceNotUnequippable = other.forceNotUnequippable;
        this.valueModifier = other.valueModifier;
        this.overlayIcon = other.overlayIcon;
    }

    /**
     * Sets the icon that is used to overlay the base item's icon
     *
     * @param icon
     */

    public void setOverlayIcon(Icon icon)
    {
        this.overlayIcon = icon;
    }

    /**
     * Sets the amount, as a percentage, that this created item modifies the value
     * of the base item.  Note that a value of 100 is no modification
     *
     * @param valueModifier
     */

    public void setValueModifier(int valueModifier)
    {
        this.valueModifier = valueModifier;
    }

    /**
     * Sets whether this model forces the created item to be not unequippable
     *
     * @param unequip
     */

    public void setForceNotUnequippable(boolean unequip)
    {
        this.forceNotUnequippable = unequip;
    }

    /**
     * Sets the prefix that will be applied to the new item's name
     *
     * @param prefix
     */

    public void setNamePrefix(String prefix)
    {
        this.namePrefix = prefix;
    }

    /**
     * Sets the postfix that will be applied to the new item's name
     *
     * @param postfix
     */

    public void setNamePostfix(String postfix)
    {
        this.namePostfix = postfix;
    }

    /**
     * Returns the ID of the already existing base item
     *
     * @return the base item ID
     */

    public String getBaseItemID()
    {
        return baseItemID;
    }

    /**
     * Returns the ID of the new created item
     *
     * @return the created item ID
     */

    public String getCreatedItemID()
    {
        return createdItemID;
    }

    /**
     * Adds the specified enchantment to the list of enchantments to be applied to the CreatedItem
     *
     * @param enchantment
     */

    public void addEnchantment(String enchantment)
    {
        this.enchantments.add(enchantment);
    }

    /**
     * Returns the list of all enchantments to be applied to this item
     *
     * @return the list of all enchantments
     */

    public List<String> getEnchantments()
    {
        return this.enchantments;
    }

    /**
     * Gets a new name modified based on the prefix, baseName, and postfix
     *
     * @param baseName
     * @return a new, modified name
     */

    public String getModifiedName(String baseName)
    {
        String newName;
        if (namePrefix != null) {
            newName = namePrefix + baseName;
        } else {
            newName = baseName;
        }

        if (namePostfix != null) {
            newName = newName + namePostfix;
        }

        return newName;
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
        if (forceNotUnequippable) {
            return false;
        } else {
            return other.isUnequippable();
        }
    }

    /**
     * Modifies the specified value by the valueModifier of this CreatedItem,
     * and returns the result
     *
     * @param baseValue
     * @return the modified value
     */

    public int getModifiedValue(int baseValue)
    {
        return valueModifier * 100 + (baseValue * valueModifier / 100);
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
        if (overlayIcon == IconFactory.emptyIcon) {
            return baseIcon;
        } else {
            return new ComposedIcon(baseIcon, overlayIcon);
        }
    }

    /**
     * Creates a new CreatedItem based on the current state of this Model and returns it.
     * Note that the CreatedItem is immutable
     *
     * @return a newly created CreatedItem
     */

    public CreatedItem getCreatedItem()
    {
        return new CreatedItem(this);
    }
}
