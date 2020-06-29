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
import java.util.List;

import main.java.hale.Game;
import main.java.hale.icon.SimpleIcon;
import main.java.hale.rules.BaseWeapon;
import main.java.hale.util.SimpleJSONArrayEntry;
import main.java.hale.util.SimpleJSONObject;

/**
 * The immutable part of the definition for ammo for a ranged weapon
 *
 * @author Jared
 */

public class AmmoTemplate extends EquippableItemTemplate
{

    // the list of weapons that this is ammo for
    private final List<BaseWeapon> baseWeapons;

    private final SimpleIcon projectileIcon;

    /**
     * Creates a new AmmoTemplate
     *
     * @param id   the Entity ID
     * @param data the JSON data to be parsed
     */

    public AmmoTemplate(String id, SimpleJSONObject data)
    {
        super(id, data);

        // the projectile icon must be a simple icon for animation purposes
        projectileIcon = new SimpleIcon(data.getObject("projectileIcon"));

        baseWeapons = new ArrayList<BaseWeapon>(2);

        for (SimpleJSONArrayEntry entry : data.getArray("weapons")) {
            String baseWeaponID = entry.getString();

            baseWeapons.add(Game.ruleset.getBaseWeapon(baseWeaponID));
        }
    }

    private AmmoTemplate(String id, AmmoTemplate other, CreatedItem createdItem)
    {
        super(id, other, createdItem);

        this.projectileIcon = other.projectileIcon;
        this.baseWeapons = new ArrayList<BaseWeapon>(other.baseWeapons);
    }

    @Override
    public AmmoTemplate createModifiedCopy(String id, CreatedItem createdItem)
    {
        return new AmmoTemplate(id, this, createdItem);
    }

    @Override
    public Ammo createInstance()
    {
        return new Ammo(this);
    }

    /**
     * Returns the icon that is drawn in the area to represent this projectile
     *
     * @return the projectile icon
     */

    public SimpleIcon getProjectileIcon()
    {
        return projectileIcon;
    }

    /**
     * Returns true if this ammo is usable by the specified baseWeapon, false otherwise
     *
     * @param baseWeapon
     * @return whether this ammo can be used by the specified baseWeapon
     */

    public boolean isUsableByBaseWeapon(BaseWeapon baseWeapon)
    {
        return baseWeapons.contains(baseWeapon);
    }

    /**
     * Returns the list of base weapons that can use this ammo.
     *
     * @return the list of base weapons that can use this ammo.  Note that this
     * list is unmodifiable
     */

    public List<BaseWeapon> getUsableBaseWeapons()
    {
        return Collections.unmodifiableList(baseWeapons);
    }
}
