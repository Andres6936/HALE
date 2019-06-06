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

public class BonusSuperTypeList
{
    private Map< Bonus.Type, BonusTypeList > bonuses;

    public BonusSuperTypeList( )
    {
        bonuses = new HashMap< Bonus.Type, BonusTypeList >( );
    }

    public BonusSuperTypeList( BonusSuperTypeList other )
    {
        this.bonuses = new HashMap< Bonus.Type, BonusTypeList >( );

        for ( Bonus.Type key : other.bonuses.keySet( ) )
        {
            BonusTypeList list = new BonusTypeList( other.bonuses.get( key ) );
            this.bonuses.put( key, list );
        }
    }

    public void remove( Bonus bonus )
    {
        Bonus.Type type = bonus.getType( );

        if ( bonuses.containsKey( type ) )
        {
            bonuses.get( type ).remove( bonus );

            if ( bonuses.get( type ).isEmpty( ) )
            {
                bonuses.remove( type );
            }
        }
    }

    public void add( Bonus bonus )
    {
        Bonus.Type type = bonus.getType( );

        if ( bonuses.containsKey( type ) )
        {
            bonuses.get( type ).add( bonus );
        }
        else
        {
            BonusTypeList list = new BonusTypeList( );
            list.add( bonus );
            bonuses.put( type, list );
        }
    }

    public int getCurrentTotal( Bonus.Type type )
    {
        if ( bonuses.containsKey( type ) ) { return bonuses.get( type ).getCurrentTotal( ); }
        else { return 0; }
    }
}
