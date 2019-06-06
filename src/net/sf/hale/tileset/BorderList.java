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

package net.sf.hale.tileset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sf.hale.util.Point;
import net.sf.hale.util.SimpleJSONArray;
import net.sf.hale.util.SimpleJSONArrayEntry;
import net.sf.hale.util.SimpleJSONObject;

/**
 * A list of borders for use in determining the transitions between terrain types
 *
 * @author Jared Stephen
 */

public class BorderList implements Iterable< Border >
{
    private String id;
    private List< Border > borders;

    private BorderList( String id )
    {
        this.id = id;
    }

    /**
     * Returns the ID String for this BorderList
     *
     * @return the ID String for this BorderList
     */

    public String getID( )
    {
        return id;
    }

    @Override
    public Iterator< Border > iterator( )
    {
        return borders.iterator( );
    }

    /**
     * Returns the List of Borders which have all conditions met at the specified point
     * assuming the specified terrain, or an empty list if there are no borders
     *
     * @param terrain the grid of terrain types
     * @param center  the point that is being checked for border conditions
     * @return the border conditions that are met
     */

    public List< Border > getMatchingBorders( TerrainType[][] terrain, Point center )
    {
        List< Border > borders = new ArrayList< Border >( );

        for ( Border border : this.borders )
        {
            if ( border.isMetAtPoint( terrain, center ) )
            { borders.add( border ); }
        }

        return borders;
    }

    /**
     * Creates a new BorderList by parsing the specified JSON data
     *
     * @param data the JSON data for the border list
     * @param id   the ID for the new border list
     * @return the new border list
     */

    public static BorderList parse( SimpleJSONArray data, String id )
    {
        BorderList borderList = new BorderList( id );

        ArrayList< Border > borders = new ArrayList< Border >( );
        for ( SimpleJSONArrayEntry entry : data )
        {
            SimpleJSONObject borderObject = entry.getObject( );

            borders.add( Border.parse( borderObject, id ) );
        }

        borders.trimToSize( );
        borderList.borders = Collections.unmodifiableList( borders );

        return borderList;
    }


}