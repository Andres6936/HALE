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

package net.sf.hale;

import java.util.HashMap;
import java.util.Map;

import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.Saveable;
import net.sf.hale.util.SimpleJSONObject;

/**
 * Class for storing data in a script via key - value pairs
 *
 * @author Jared
 */

public class ScriptState implements Saveable
{
    public Map< String, Object > state;

    @Override
    public Object save( )
    {
        JSONOrderedObject data = new JSONOrderedObject( );

        for ( String key : state.keySet( ) )
        {
            data.put( key, state.get( key ) );
        }

        return data;
    }

    /**
     * Creates a new ScriptState object by loading the specified JSON data
     *
     * @param data the parsed JSON data
     */

    public void load( SimpleJSONObject data )
    {
        for ( String key : data.keySet( ) )
        {
            if ( data.isString( key ) )
            {
                state.put( key, data.get( key, null ) );
            }
            else if ( data.isInteger( key ) )
            {
                state.put( key, data.get( key, 0 ) );
            }
            else if ( data.isFloat( key ) )
            {
                state.put( key, data.get( key, 0.0f ) );
            }
            else if ( data.isBoolean( key ) )
            {
                state.put( key, data.get( key, false ) );
            }
        }
    }

    /**
     * Creates a new scriptstate which is a copy of the specified scriptState
     *
     * @param other the scriptstate to copy
     */

    public ScriptState( ScriptState other )
    {
        this( );

        for ( String key : other.state.keySet( ) )
        {
            this.state.put( key, other.state.get( key ) );
        }
    }

    /**
     * Creates a new, empty ScriptState object for storing key-value pairs
     */

    public ScriptState( )
    {
        this.state = new HashMap< String, Object >( );
    }

    /**
     * Returns true if this script state contains one or more key value pairs, false otherwise
     *
     * @return whether this script state is empty
     */

    public boolean isEmpty( )
    {
        return state.isEmpty( );
    }

    /**
     * Returns the value for the specified key in this script state
     *
     * @param key the key of the key-value pair
     * @return the value for the specified key
     */

    public Object get( String key )
    {
        return state.get( key );
    }

    /**
     * Stores the specified key value pair in this script state.  The value must be
     * either a string, number, or boolean
     *
     * @param key   the key for the key-value pair
     * @param value the value for the pair
     */

    public void put( String key, Object value )
    {
        if ( value instanceof String )
        {
            state.put( key, value );
        }
        else if ( value instanceof Number )
        {
            state.put( key, value );
        }
        else if ( value instanceof Boolean )
        {
            state.put( key, value );
        }
        else
        {
            throw new IllegalArgumentException( "Only strings, numbers, or booleans may be stored in script state." );
        }
    }
}
