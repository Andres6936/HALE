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

import main.java.hale.area.Area;
import main.java.hale.loading.JSONOrderedObject;
import main.java.hale.loading.LoadGameException;
import main.java.hale.loading.ReferenceHandler;
import main.java.hale.util.SimpleJSONObject;

/**
 * An item that can be equipped as ammunition for a ranged weapon
 *
 * @author Jared
 */

public class Ammo extends EquippableItem
{

    private final AmmoTemplate template;

    @Override
    public void load(SimpleJSONObject data, Area area, ReferenceHandler refHandler) throws LoadGameException
    {
        super.load(data, area, refHandler);
    }

    @Override
    public JSONOrderedObject save()
    {
        return super.save();
    }

    /**
     * Creates a new Ammo item
     *
     * @param template
     */

    protected Ammo(AmmoTemplate template)
    {
        super(template);

        this.template = template;

    }

    @Override
    public AmmoTemplate getTemplate()
    {
        return template;
    }

    /**
     * Returns the bonus to attack caused by this item's quality, or 0 if this item
     * has no quality
     *
     * @return the quality based bonus to attack
     */

    public int getQualityAttackBonus()
    {
        if (this.getQuality() == null) {
            return 0;
        } else {
            return getQuality().getAttackBonus();
        }
    }

    /**
     * Returns the bonus to damage caused by this item's quality, or 0 if this item has no quality
     *
     * @return the quality based bonus to damage
     */

    public int getQualityDamageBonus()
    {
        if (getQuality() == null) {
            return 0;
        } else {
            return getQuality().getDamageBonus();
        }
    }

}
