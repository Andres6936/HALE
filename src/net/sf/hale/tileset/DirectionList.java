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

import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.StringParser;
import net.sf.hale.util.Logger;
import net.sf.hale.util.Point;

/**
 * A list of directions.  Starting at one tile in an area, the directions can
 * be followed in order to reach a specified final tile.  This class is immutable
 *
 * @author Jared Stephen
 */

public class DirectionList implements Iterable< Direction >
{
    private List< Direction > directions;

    @Override
    public Iterator< Direction > iterator( )
    {
        return directions.iterator( );
    }

    @Override
    public String toString( )
    {
        StringBuilder sb = new StringBuilder( );

        for ( Direction direction : directions )
        {
            sb.append( direction.toString( ) );
        }

        return sb.toString( );
    }

    /**
     * Returns the point that one ends up at if traversing this DirectionList
     * from the specified start point
     *
     * @param start the starting point
     * @return the end point from traversing this DirectionList
     */

    public Point getRelativePoint( Point start )
    {
        Point current = start;
        Point[] adjacent = AreaUtil.getAdjacentTiles( start );
        for ( Direction direction : directions )
        {
            switch ( direction )
            {
                case North:
                    current = adjacent[ 0 ];
                    break;
                case NorthEast:
                    current = adjacent[ 1 ];
                    break;
                case SouthEast:
                    current = adjacent[ 2 ];
                    break;
                case South:
                    current = adjacent[ 3 ];
                    break;
                case SouthWest:
                    current = adjacent[ 4 ];
                    break;
                case NorthWest:
                    current = adjacent[ 5 ];
                    break;
                case Center:
                    // no change in current
                    break;
            }

            adjacent = AreaUtil.getAdjacentTiles( current );
        }

        return current;
    }

    /**
     * Creates a new direction list from the specified String, which must be a
     * list of {@link Direction} enum string values separated by "-" characters.
     *
     * @param input the input string to parse
     * @return the DirectionList created by parsing the specified string
     */

    public static DirectionList parse( String input )
    {
        DirectionList list = new DirectionList( );
        ArrayList< Direction > directions = new ArrayList< Direction >( );

        StringParser tokenizer = new StringParser( input );
        tokenizer.whitespaceChars( '-', '-' );
        try
        {
            while ( tokenizer.hasNext( ) )
            {
                directions.add( Direction.valueOf( tokenizer.next( ) ) );
            }
        }
        catch ( Exception e )
        {
            Logger.appendToErrorLog( "Error parsing direction list from " + input, e );
        }

        directions.trimToSize( );
        list.directions = Collections.unmodifiableList( directions );
        return list;
    }
}
