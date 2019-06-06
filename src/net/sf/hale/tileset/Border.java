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
import net.sf.hale.util.SimpleJSONArrayEntry;
import net.sf.hale.util.SimpleJSONObject;

/**
 * A border for use in the transition between terrain types.  Each border
 * specifies a set of rules that a given location in an area must satisfy for
 * the border condition to be met and the border tiles to be added
 *
 * @author Jared Stephen
 */

public class Border implements Iterable< BorderTile >
{
    private String borderListID;
    private List< DirectionList > mustHave;
    private List< DirectionList > mustNotHave;
    private List< BorderTile > tiles;

    /**
     * Creates a new Border with a parent list of the specified ID
     *
     * @param borderListID the ID of the parent list
     */

    protected Border( String borderListID, List< DirectionList > mustHave, List< DirectionList > mustNotHave, List< BorderTile > tiles )
    {
        this.borderListID = borderListID;
        this.mustHave = mustHave;
        this.mustNotHave = mustNotHave;
        this.tiles = tiles;
    }

    /**
     * Returns the list of rules that must be met.
     *
     * @return the list of rules that must be met.  The list is unmodifiable
     */

    public List< DirectionList > getMustHave( )
    {
        return mustHave;
    }

    /**
     * Returns the list of rules that must not be met.
     *
     * @return the list of rules that must not be met.  The list is unmodifiable
     */

    public List< DirectionList > getMustNotHave( )
    {
        return mustNotHave;
    }

    @Override
    public Iterator< BorderTile > iterator( )
    {
        return tiles.iterator( );
    }

    /**
     * Returns true if and only if all the conditions for this border are met at the
     * specified point within the specified grid of terrain
     *
     * @param terrain the set of terrain that is being checked against
     * @param center  the point within the set of terrain to check the border conditions
     * @return true if and only if this border's conditions are met
     */

    protected boolean isMetAtPoint( TerrainType[][] terrain, Point center )
    {
        TerrainType curTerrain = terrain[ center.x ][ center.y ];

        for ( DirectionList list : mustHave )
        {
            Point p = list.getRelativePoint( center );
            if ( p.x < 0 || p.x >= terrain.length || p.y < 0 || p.y >= terrain[ 0 ].length )
            {
                // outside the terrain, so the condition is not met
                return false;
            }

            // the border between the current terrain type and the terrain type at p must
            // use the same border list in order for the condition to be met
            String borderListID = curTerrain.getBorderIDWith( terrain[ p.x ][ p.y ] );
            if ( ! this.borderListID.equals( borderListID ) ) return false;
        }

        for ( DirectionList list : mustNotHave )
        {
            Point p = list.getRelativePoint( center );
            if ( p.x < 0 || p.x >= terrain.length || p.y < 0 || p.y >= terrain[ 0 ].length )
            {
                // outside the terrain, the condition is not met
                return false;
            }

            if ( terrain[ p.x ][ p.y ] != curTerrain ) return false;
        }

        return true;
    }

    /**
     * Creates a new Border object by parsing the specified JSON data
     *
     * @param data     the JSON data to parse
     * @param parentID the parent BorderList containing this border
     * @return the new Border object
     */

    public static Border parse( SimpleJSONObject data, String parentID )
    {
        ArrayList< BorderTile > tiles = new ArrayList< BorderTile >( );
        for ( SimpleJSONArrayEntry entry : data.getArray( "tiles" ) )
        {
            tiles.add( BorderTile.parse( entry.getObject( ) ) );
        }
        tiles.trimToSize( );
        data.setWarnOnMissingKeys( false );

        ArrayList< DirectionList > mustHave = new ArrayList< DirectionList >( );
        for ( SimpleJSONArrayEntry entry : data.getArray( "MustHave" ) )
        {
            mustHave.add( DirectionList.parse( entry.getString( ) ) );
        }
        mustHave.trimToSize( );

        ArrayList< DirectionList > mustNotHave = new ArrayList< DirectionList >( );
        for ( SimpleJSONArrayEntry entry : data.getArray( "MustNotHave" ) )
        {
            mustNotHave.add( DirectionList.parse( entry.getString( ) ) );
        }
        mustNotHave.trimToSize( );

        data.setWarnOnMissingKeys( true );

        Border border = new Border( parentID, Collections.unmodifiableList( mustHave ),
                                    Collections.unmodifiableList( mustNotHave ), Collections.unmodifiableList( tiles ) );

        return border;
    }
}
