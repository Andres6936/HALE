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

package net.sf.hale.bonus;

import java.util.HashMap;
import java.util.Map;

public class BonusTypeList
{
    private int currentTotal;
    private Map< Bonus.StackType, BonusStackTypeList > bonuses;

    public BonusTypeList( )
    {
        bonuses = new HashMap< Bonus.StackType, BonusStackTypeList >( );
        currentTotal = 0;
    }

    public BonusTypeList( BonusTypeList other )
    {
        this.bonuses = new HashMap< Bonus.StackType, BonusStackTypeList >( );
        for ( Bonus.StackType key : other.bonuses.keySet( ) )
        {
            BonusStackTypeList list = new BonusStackTypeList( other.bonuses.get( key ) );
            this.bonuses.put( key, list );
        }

        this.currentTotal = other.currentTotal;
    }

    public void remove( Bonus bonus )
    {
        Bonus.StackType stackType = bonus.getStackType( );

        if ( bonuses.containsKey( stackType ) )
        {
            bonuses.get( stackType ).remove( bonus );

            if ( bonuses.get( stackType ).isEmpty( ) )
            {
                bonuses.remove( stackType );
            }
        }

        // we need to recompute the total after removing a bonus
        if ( bonus.hasValue( ) )
        {
            currentTotal = 0;

            for ( Bonus.StackType type : bonuses.keySet( ) )
            {
                currentTotal += bonuses.get( type ).getCurrentTotal( );
            }
        }
    }

    public void add( Bonus bonus )
    {
        Bonus.StackType stackType = bonus.getStackType( );

        if ( bonuses.containsKey( stackType ) )
        {
            // subtract the old total from this stack type
            currentTotal -= bonuses.get( stackType ).getCurrentTotal( );

            bonuses.get( stackType ).add( bonus );

            // add the new total from this stack type
            if ( bonus.hasValue( ) ) currentTotal += bonuses.get( stackType ).getCurrentTotal( );

        }
        else
        {
            BonusStackTypeList bonusStackTypeList = new BonusStackTypeList( );
            bonusStackTypeList.add( bonus );
            bonuses.put( stackType, bonusStackTypeList );

            if ( bonus.hasValue( ) ) currentTotal += bonuses.get( stackType ).getCurrentTotal( );
        }
    }

    public int get( Bonus.StackType stackType )
    {
        if ( bonuses.containsKey( stackType ) ) { return bonuses.get( stackType ).getCurrentTotal( ); }
        else { return 0; }
    }

    public int getCurrentTotal( ) { return currentTotal; }

    public boolean isEmpty( )
    {
        return bonuses.keySet( ).size( ) == 0;
    }
}
