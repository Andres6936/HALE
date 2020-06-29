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

import hale.Game;
import hale.rules.ArmorType;
import hale.util.SimpleJSONObject;

/**
 * The immutable part of a piece or armor, gloves, boots, helmet, or shield
 *
 * @author Jared
 */

public class ArmorTemplate extends EquippableItemTemplate
{

    private final ArmorType armorType;

    private final int armorClass;

    // the armor penalty, which applied a penalty to dexterity bonuses and some skills
    private final int armorPenalty;
    private final int movementPenalty;

    // the penalty to attack from this armor, stacked as a "ShieldAttack" penalty
    private final int shieldAttackPenalty;

    /**
     * Creates a new ArmorTemplate
     *
     * @param id   the Entity ID
     * @param data the JSON data to be parsed
     */

    public ArmorTemplate(String id, SimpleJSONObject data)
    {
        super(id, data);

        this.armorType = Game.ruleset.getArmorType(data.get("armorType", null));

        this.armorClass = data.get("armorClass", 0);
        this.armorPenalty = data.get("armorPenalty", 0);
        this.movementPenalty = data.get("movementPenalty", 0);
        this.shieldAttackPenalty = data.get("shieldAttackPenalty", 0);
    }

    private ArmorTemplate(String id, ArmorTemplate other, CreatedItem createdItem)
    {
        super(id, other, createdItem);

        this.armorType = other.armorType;
        this.armorClass = other.armorClass;
        this.armorPenalty = other.armorPenalty;
        this.movementPenalty = other.movementPenalty;
        this.shieldAttackPenalty = other.shieldAttackPenalty;
    }

    @Override
    public ArmorTemplate createModifiedCopy(String id, CreatedItem createdItem)
    {
        return new ArmorTemplate(id, this, createdItem);
    }

    @Override
    public Armor createInstance()
    {
        return new Armor(this);
    }

    /**
     * Returns the ArmorType of this armor.  By default, the options are
     * Light, Medium, Heavy, Shield, and TowerShield
     *
     * @return the armorType
     */

    public ArmorType getArmorType()
    {
        return armorType;
    }

    /**
     * Returns the bonus that this piece of armor applied to armor class
     *
     * @return the armor class bonus
     */

    public int getArmorClass()
    {
        return armorClass;
    }

    /**
     * Returns the ArmorPenalty.  ArmorPenalty limits how much dexterity bonus can
     * be applied to armor class, and also penalizes some skills.  It is a percentage
     *
     * @return the ArmorPenalty
     */

    public int getArmorPenalty()
    {
        return armorPenalty;
    }

    /**
     * Returns the MovementPenalty.  This increases the number of ActionPoints (AP)
     * that a creature must use to move a tile.  It is a percentage
     *
     * @return the movement penalty for this armor
     */

    public int getMovementPenalty()
    {
        return movementPenalty;
    }

    /**
     * A penalty to attack that is used by some shields.  This attack penalty is stacked
     * separately from other attack bonuses and penalties
     *
     * @return the penalty to attack
     */

    public int getShieldAttackPenalty()
    {
        return shieldAttackPenalty;
    }

    @Override
    public boolean hasPrereqsToEquip(Creature parent)
    {
        return parent.stats.hasArmorProficiency(armorType.getName());
    }
}
