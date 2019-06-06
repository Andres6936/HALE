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

package net.sf.hale.entity;

import net.sf.hale.Game;
import net.sf.hale.rules.DamageType;
import net.sf.hale.util.SimpleJSONObject;

/**
 * The class containing the immutable parts of a Trap
 *
 * @author Jared
 */

public class TrapTemplate extends ItemTemplate
{

    private final DamageType damageType;
    private final int minDamage, maxDamage;

    private final int findDifficulty;
    private final int placeDifficulty;
    private final int disarmDifficulty;
    private final int recoverDifficulty;
    private final int reflexDifficulty;

    /**
     * Creates a new TrapTemplate
     *
     * @param id   the entity ID
     * @param data the JSON to parse
     */

    public TrapTemplate( String id, SimpleJSONObject data )
    {
        super( id, data );

        this.damageType = Game.ruleset.getDamageType( data.get( "damageType", null ) );

        this.minDamage = data.get( "minDamage", 0 );
        this.maxDamage = data.get( "maxDamage", 0 );

        this.findDifficulty = data.get( "findDifficulty", 0 );
        this.placeDifficulty = data.get( "placeDifficulty", 0 );
        this.disarmDifficulty = data.get( "disarmDifficulty", 0 );
        this.recoverDifficulty = data.get( "recoverDifficulty", 0 );
        this.reflexDifficulty = data.get( "reflexDifficulty", 0 );
    }

    @Override
    public Trap createInstance( )
    {
        return new Trap( this );
    }

    /**
     * Returns the damageType that this trap does.
     *
     * @return the damage type of this weapon
     */

    public DamageType getDamageType( ) { return damageType; }

    /**
     * Returns the base minimum amount of damage that this trap does.
     * Actual damage is based on this value, which is then modified by many
     * other factors including item quality
     *
     * @return the base minimum damage
     */

    public int getMinDamage( ) { return minDamage; }

    /**
     * Returns the base maximum amount of damage that this trap does.
     * Actual damage is based on this value, which is then modified by many
     * other factors including item quality
     *
     * @return the base maximum damage
     */

    public int getMaxDamage( ) { return maxDamage; }

    /**
     * Returns the value that a creature must make on a Search skill check in
     * order to spot this trap
     *
     * @return the difficulty in spotting the trap
     */

    public int getFindDifficulty( ) { return findDifficulty; }

    /**
     * Returns the value that a creature must make on a Traps skill check in
     * order to place and armo this trap
     *
     * @return the difficulty in setting the trap
     */

    public int getPlaceDifficulty( ) { return placeDifficulty; }

    /**
     * Returns the value that a creature must make on a Traps skill check in order
     * to permanently disarm this trap.  This is usually easier than recovering
     * the trap for later use.
     *
     * @return the difficulty in disarming the trap
     */

    public int getDisarmDifficulty( ) { return disarmDifficulty; }

    /**
     * Returns the value that a creature must make on a Traps skill check in order
     * to disarm and recover this trap, allowing it to be re-used by the creature
     *
     * @return the difficulty in disarming and recovering this trap
     */

    public int getRecoverDifficulty( ) { return recoverDifficulty; }

    /**
     * Returns the value that a creature must make on its Reflex Resistance check
     * in order to avoid the effects of this trap
     *
     * @return the reflex resistance check difficulty
     */

    public int getReflexDifficulty( ) { return reflexDifficulty; }
}
