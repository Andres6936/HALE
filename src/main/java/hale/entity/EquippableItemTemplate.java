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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import main.java.hale.icon.SimpleIcon;
import main.java.hale.icon.SubIcon;
import main.java.hale.util.SimpleJSONArrayEntry;
import main.java.hale.util.SimpleJSONObject;

/**
 * A template for any item that can be equipped into an inventory slot, such as weapons,
 * armor, cloaks, etc.
 *
 * @author Jared
 */

public class EquippableItemTemplate extends ItemTemplate
{

    // whether this item can be unequipped by the player normally
    private final boolean isUnequippable;

    // whether this item's subicon should remove he hair icon from the parent when equipped
    private final boolean coversHair;

    // whether this item's subicon should remove the beard icon
    // from the parent when equipped
    private final boolean coversBeard;

    // the sub icon used to represent this item on the owning creature
    private final SimpleIcon subIcon;

    // the type of subicon for this item, or null for the default
    private final SubIcon.Type subIconType;

    private final Type type;

    private final List<Enchantment> enchantments;

    /**
     * The type of item, which determines which inventory slots it can go into
     *
     * @author Jared
     */

    public enum Type
    {
        Weapon,
        Armor,
        Gloves,
        Helmet,
        Cloak,
        Boots,
        Belt,
        Amulet,
        Ring,
        Ammo,
        Shield;
    }

    public static final Map<Type, Inventory.Slot[]> validSlotsForType = new EnumMap<>(Type.class);

    /**
     * Initializes the map of inventory slots to equippable item types
     */

    public static void initializeTypesMap()
    {
        putInTypeMap(Type.Weapon, Inventory.Slot.MainHand, Inventory.Slot.OffHand);
        putInTypeMap(Type.Armor, Inventory.Slot.Armor);
        putInTypeMap(Type.Gloves, Inventory.Slot.Gloves);
        putInTypeMap(Type.Helmet, Inventory.Slot.Helmet);
        putInTypeMap(Type.Cloak, Inventory.Slot.Cloak);
        putInTypeMap(Type.Boots, Inventory.Slot.Boots);
        putInTypeMap(Type.Belt, Inventory.Slot.Belt);
        putInTypeMap(Type.Amulet, Inventory.Slot.Amulet);
        putInTypeMap(Type.Ring, Inventory.Slot.RightRing, Inventory.Slot.LeftRing);
        putInTypeMap(Type.Ammo, Inventory.Slot.Quiver);
        putInTypeMap(Type.Shield, Inventory.Slot.OffHand);
    }

    private static void putInTypeMap(Type type, Inventory.Slot... slots)
    {
        validSlotsForType.put(type, slots);
    }

    /**
     * Creates a new EquippableItemTemplate
     *
     * @param id   the entity ID
     * @param data the JSON data parser
     */

    public EquippableItemTemplate(String id, SimpleJSONObject data)
    {
        super(id, data);

        this.type = Type.valueOf(data.get("type", null));

        if (data.containsKey("coversHair")) {
            this.coversHair = data.get("coversHair", false);
        } else {
            this.coversHair = false;
        }

        if (data.containsKey("coversBeard")) {
            this.coversBeard = data.get("coversBeard", false);
        } else {
            this.coversBeard = false;
        }

        if (data.containsKey("isUnequippable")) {
            this.isUnequippable = data.get("isUnequippable", true);
        } else {
            this.isUnequippable = true;
        }

        if (data.containsKey("subIcon")) {
            SimpleJSONObject subIconObject = data.getObject("subIcon");

            if (subIconObject.containsKey("type")) {
                this.subIconType = SubIcon.Type.valueOf(subIconObject.get("type", null));
            } else {
                this.subIconType = null;
            }

            this.subIcon = new SimpleIcon(subIconObject);
        } else {
            this.subIcon = null;
            this.subIconType = null;
        }

        if (data.containsKey("enchantments")) {
            List<Enchantment> enchantments = new ArrayList<Enchantment>(1);

            for (SimpleJSONArrayEntry entry : data.getArray("enchantments")) {
                enchantments.add(new Enchantment(entry.getString(), false));
            }

            this.enchantments = Collections.unmodifiableList(enchantments);
        } else {
            this.enchantments = Collections.emptyList();
        }
    }

    /**
     * Creates a new EquippableItemTemplate, which is a copy of the specified template with the new ID and
     * createdItem properties.
     *
     * @param id
     * @param other
     * @param createdItem
     */

    protected EquippableItemTemplate(String id, EquippableItemTemplate other, CreatedItem createdItem)
    {
        super(id, other, createdItem);

        this.coversHair = other.coversHair;
        this.coversBeard = other.coversBeard;
        this.subIcon = other.subIcon;
        this.subIconType = other.subIconType;
        this.type = other.type;

        this.enchantments = createdItem.getEnchantments();

        this.isUnequippable = createdItem.getIsUnequippable(other);
    }

    /**
     * Creates a new template which is a copy of this specified template, except that the ID has
     * been changed and properties are copied from the created item
     *
     * @param other       the template to copy
     * @param createdItem the createdItem to make other modifications based on
     */

    public EquippableItemTemplate createModifiedCopy(String id, CreatedItem createdItem)
    {
        return new EquippableItemTemplate(id, this, createdItem);
    }

    @Override
    public EquippableItem createInstance()
    {
        return new EquippableItem(this);
    }

    /**
     * Returns the list of enchantments associated with this template.  Note that this
     * list is unmodifiable, and will be an empty list if there are no enchantments
     *
     * @return the list of enchantments
     */

    public List<Enchantment> getEnchantments()
    {
        return enchantments;
    }

    /**
     * Returns the subIcon type that this item is forced into, regardless of inventory slot.
     * If null, then this item does not force a particular sub Icon type, and the default
     * for the given inventory slot will be used
     *
     * @return null if this item does not force a sub icon type, or the forced type
     */

    public SubIcon.Type getSubIconTypeOverride()
    {
        return this.subIconType;
    }

    /**
     * Returns the type of this equippable item, which determines what inventory slots
     * it is able to be equipped into
     *
     * @return the type of this equippable item
     */

    public Type getType()
    {
        return type;
    }

    /**
     * Returns true if this item can be unequipped normally by the player, false otherwise
     *
     * @return whether this item can be normally unequipped through the interface
     */

    public boolean isUnequippable()
    {
        return isUnequippable;
    }

    /**
     * Returns true if this item's sub icon should cover (remove) the bear icon of the
     * parent creature while equipped, false otherwise
     *
     * @return whether this item's sub icon should remove the hair icon of the parent
     * while equipped
     */

    public boolean coversHair()
    {
        return coversHair;
    }

    /**
     * Returns true if this item's sub icon should cover (remove) the beard icon of the parent
     * creature while equipped, false otherwise.
     *
     * @return whether this item's sub icons should cover the beard icon of the parent
     */

    public boolean coversBeard()
    {
        return coversBeard;
    }

    /**
     * Gets the SubIcon used to represent this item on the owning Creature, or null
     * if this item does not have a subIcon
     *
     * @return the SubIcon
     */

    public SimpleIcon getSubIcon()
    {
        return subIcon;
    }
}
