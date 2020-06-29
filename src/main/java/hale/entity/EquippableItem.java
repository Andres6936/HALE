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

import hale.ability.Effect;
import hale.area.Area;
import hale.bonus.BonusList;
import hale.bonus.BonusManager;
import hale.loading.JSONOrderedObject;
import hale.loading.LoadGameException;
import hale.loading.ReferenceHandler;
import hale.util.SimpleJSONObject;

/**
 * An item that can be equipped in one of the inventory's slots
 *
 * @author Jared
 */

public class EquippableItem extends Item
{

    /**
     * The BonusManager, controlling the currently applied bonuses (from effects and
     * enchantments) to this Item
     */

    public final BonusManager bonuses;

    private final EquippableItemTemplate template;

    // the creature that currently has the item equipped
    private Creature owner;

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
     * Creates a new EquippableItem
     *
     * @param template
     */

    protected EquippableItem(EquippableItemTemplate template)
    {
        super(template);

        this.template = template;

        bonuses = new BonusManager();
        for (Enchantment enchantment : template.getEnchantments()) {
            bonuses.addAll(enchantment.getBonuses());
        }
    }

    @Override
    public EquippableItemTemplate getTemplate()
    {
        return template;
    }

    /**
     * Returns a list of all the bonuses that have been applied to this item, including
     * all effects and all enchantments
     *
     * @return a list of all bonuses applied to this item
     */

    public BonusList getBonusList()
    {
        BonusList bonuses = new BonusList();

        for (Effect effect : getEffects()) {
            bonuses.addAll(effect.getBonuses());
        }

        for (Enchantment enchantment : template.getEnchantments()) {
            bonuses.addAll(enchantment.getBonuses());
        }

        return bonuses;
    }

    /**
     * Sets the owner of this item.  The owner is the creature which currently has the item
     * equipped, or null if no creature currently has this item equipped
     *
     * @param owner
     */

    protected void setOwner(Creature owner)
    {
        this.owner = owner;
    }

    @Override
    public boolean elapseTime(int numRounds)
    {
        boolean returnValue = super.elapseTime(numRounds);

        updateListeners();

        return returnValue;
    }

    @Override
    protected void applyEffectBonuses(Effect effect)
    {
        bonuses.addAll(effect.getBonuses());

        if (owner != null) owner.stats.addAll(effect.getBonuses());
    }

    @Override
    protected void removeEffectBonuses(Effect effect)
    {
        bonuses.removeAll(effect.getBonuses());

        if (owner != null) owner.stats.removeAll(effect.getBonuses());
    }

    @Override
    public String getLongName()
    {
        return super.getLongName();
    }

}
