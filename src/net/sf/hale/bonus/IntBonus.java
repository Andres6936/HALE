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

import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.util.SimpleJSONObject;

public class IntBonus extends Bonus
{
    private final int value;

    @Override
    public JSONOrderedObject save( )
    {
        JSONOrderedObject data = super.save( );

        data.put( "value", value );

        return data;
    }

    public static IntBonus load( SimpleJSONObject data )
    {
        int value = data.get( "value", 0 );
        Bonus.Type type = Type.valueOf( data.get( "type", null ) );
        Bonus.StackType stackType = StackType.valueOf( data.get( "stackType", null ) );

        return new IntBonus( type, stackType, value );
    }

    public IntBonus( Bonus.Type type, Bonus.StackType stackType, int value )
    {
        super( type, stackType );

        this.value = value;
    }

    @Override
    public boolean hasValue( ) { return true; }

    @Override
    public int getValue( ) { return value; }

    @Override
    public IntBonus cloneWithReduction( int reduction )
    {
        return new IntBonus( this.getType( ), this.getStackType( ), this.value - reduction );
    }

    @Override
    public void appendDescription( StringBuilder sb )
    {
        super.appendDescription( sb );

        if ( value > 0 )
        {
            sb.append( " <span style=\"font-family: green;\">+" );
        }
        else
        {
            sb.append( " <span style=\"font-family: red;\">" );
        }
        sb.append( getValue( ) ).append( "</span>" );
    }
}
