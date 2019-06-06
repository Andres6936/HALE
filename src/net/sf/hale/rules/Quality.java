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

package net.sf.hale.rules;

import net.sf.hale.util.SimpleJSONObject;

/**
 * A specific quality level, with associated modifiers for items.  Each
 * field in this class represents a percentage modification to a specific
 * item attribute.
 *
 * @author Jared
 */

public class Quality implements Comparable< Quality >
{
    private String name;
    private int armorPenaltyBonus;
    private int armorClassBonus;
    private int movementPenaltyBonus;
    private int attackBonus;
    private int damageBonus;
    private int modifier;
    private int valueAdjustment;

    /**
     * Creates a new ItemQuality by parsing the JSON data
     *
     * @param data the JSON to parse
     */

    public Quality( SimpleJSONObject data )
    {
        this.name = data.get( "id", null );
        this.armorPenaltyBonus = data.get( "armorPenaltyBonus", 0 );
        this.armorClassBonus = data.get( "armorClassBonus", 0 );
        this.movementPenaltyBonus = data.get( "movementPenaltyBonus", 0 );
        this.attackBonus = data.get( "attackBonus", 0 );
        this.damageBonus = data.get( "damageBonus", 0 );
        this.modifier = data.get( "modifier", 0 );
        this.valueAdjustment = data.get( "valueAdjustment", 0 );
    }

    /**
     * The unique, descriptive name of this item quality
     *
     * @return the name
     */

    public String getName( ) { return name; }

    /**
     * The bonus to ArmorPenalty (how much Armor affects Dexterity bonuses).
     * A value of 0 is no modifier
     *
     * @return bonus to Armor Penalty
     */

    public int getArmorPenaltyBonus( ) { return armorPenaltyBonus; }

    /**
     * The bonus to ArmorClass.  A value of 0 is no modifier
     *
     * @return bonus to ArmorClass
     */

    public int getArmorClassBonus( ) { return armorClassBonus; }

    /**
     * The bonus to MovementPenalty.  A value of 0 is no modifier
     *
     * @return the bonus to MovementPenalty
     */

    public int getMovementPenaltyBonus( ) { return movementPenaltyBonus; }

    /**
     * The bonus to Attack.  A value of 0 is no modifier
     *
     * @return the bonus to Attack
     */

    public int getAttackBonus( ) { return attackBonus; }

    /**
     * The bonus to Damage.  A value of 0 is no modifier
     *
     * @return the bonus to Damage
     */

    public int getDamageBonus( ) { return damageBonus; }

    /**
     * The generic modifier which can be used by any other properties
     * of the item not explicitly mentioned in this class.  A value
     * of 0 means no modifier
     *
     * @return the generic modifier
     */

    public int getModifier( ) { return modifier; }

    /**
     * The modification to value.  A value of 100 means no modifier
     *
     * @return the value modification
     */

    public int getValueAdjustment( ) { return valueAdjustment; }

    @Override
    public int compareTo( Quality other )
    {
        return this.modifier - other.modifier;
    }

    @Override
    public boolean equals( Object other )
    {
        if ( ! ( other instanceof Quality ) ) return false;

        return name.equals( ( ( Quality ) other ).name );
    }
}
