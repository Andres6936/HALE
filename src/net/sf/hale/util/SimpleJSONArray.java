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

package net.sf.hale.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.minidev.json.JSONArray;

/**
 * A simple wrapper around a JSONArray allowing for easy access of all
 * array elements without any casting.
 *
 * @author Jared Stephen
 */

public class SimpleJSONArray implements Iterable< SimpleJSONArrayEntry >
{
    private List< SimpleJSONArrayEntry > data;

    /**
     * Creates a new, empty SimpleJSONArray with size 0
     */

    protected SimpleJSONArray( )
    {
        data = Collections.emptyList( );
    }

    /**
     * Creates a new SimpleJSONArray wrapping the specified array
     *
     * @param array   the array to wrap
     * @param arrayID ID for this array based on the key this array maps to
     */

    public SimpleJSONArray( JSONArray array, String arrayID )
    {
        data = new ArrayList< SimpleJSONArrayEntry >( array.size( ) );
        int index = 0;
        for ( Object object : array )
        {
            StringBuilder id = new StringBuilder( );
            id.append( arrayID );
            id.append( "[" );
            id.append( index );
            id.append( "]" );

            data.add( new SimpleJSONArrayEntry( object, id.toString( ) ) );
            index++;
        }
    }

    /**
     * Sets whether this Array and all sub objects will warn when a key is requested but not
     * found.
     *
     * @param warn whether to warn on missing keys
     */

    protected void setWarnOnMissingKeys( boolean warn )
    {
        for ( SimpleJSONArrayEntry object : data )
        {
            object.setWarnOnMissingKeys( warn );
        }
    }

    /**
     * Checks all keys in this object and all sub objects and logs a warning
     * for any keys that have not been read
     */

    protected void warnOnUnusedKeys( )
    {
        for ( SimpleJSONArrayEntry object : data )
        {
            object.warnOnUnusedKeys( );
        }
    }

    /**
     * Returns the number of elements contained within the wrapped array
     *
     * @return the number of elements in this array
     */

    public int size( )
    {
        return data.size( );
    }

    @Override
    public Iterator< SimpleJSONArrayEntry > iterator( )
    {
        return data.iterator( );
    }
}
