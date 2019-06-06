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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import net.sf.hale.area.Area;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Door;
import net.sf.hale.entity.Entity;
import net.sf.hale.entity.Trap;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.util.Point;

/**
 * A List of tiles at a given layer within a tileset in an area.
 *
 * @author Jared Stephen
 */

public class TileLayerList
{
    private TileList[][] tiles;

    // helpers for drawing entity tiles
    private boolean[][] explored;
    private boolean[][] visibility;
    private Area area;

    /**
     * saves this grid to JSON format for creating area files
     *
     * @return this grid JSON data
     */

    public JSONOrderedObject writeToJSON( )
    {
        JSONOrderedObject data = new JSONOrderedObject( );

        Map< String, List< int[] > > out = new LinkedHashMap< String, List< int[] > >( );

        for ( int x = 0; x < tiles.length; x++ )
        {
            for ( int y = 0; y < tiles[ x ].length; y++ )
            {
                // important to create the array here each time so it isn't overwritten later
                int[] coords = new int[ 2 ];
                coords[ 0 ] = x;
                coords[ 1 ] = y;

                TileList list = tiles[ x ][ y ];

                for ( Tile tile : list )
                {
                    String id = tile.getTileID( );

                    if ( ! out.containsKey( id ) )
                    {
                        out.put( id, new ArrayList< int[] >( ) );
                    }

                    out.get( id ).add( coords );
                }
            }
        }

        for ( String tileID : out.keySet( ) )
        {
            data.put( tileID, out.get( tileID ).toArray( ) );
        }

        return data;
    }

    /**
     * Creates a new TileLayerList of the specified size.  All added tiles
     * must be within the size of the list
     *
     * @param width  the width of the area
     * @param height the height of the area
     */

    public TileLayerList( int width, int height )
    {
        tiles = new TileList[ width ][ height ];

        // initialize an empty TileList at each point
        for ( int y = 0; y < height; y++ )
        {
            for ( int x = 0; x < width; x++ )
            {
                tiles[ x ][ y ] = new TileList( );
            }
        }
    }

    /**
     * Resizes this layer to the specified dimensions.  Tile data is saved
     *
     * @param newWidth  the new width
     * @param newHeight the new height
     */

    public void resize( int newWidth, int newHeight )
    {
        if ( newWidth == tiles.length && newHeight == tiles[ 0 ].length ) return;

        TileList[][] newTiles = new TileList[ newWidth ][ newHeight ];

        int copyWidth = Math.min( newTiles.length, tiles.length );
        int copyHeight = Math.min( newTiles[ 0 ].length, tiles[ 0 ].length );

        for ( int i = 0; i < copyWidth; i++ )
        {
            for ( int j = 0; j < copyHeight; j++ )
            {
                newTiles[ i ][ j ] = tiles[ i ][ j ];
            }
        }

        // initialize an empty TileList at each empty point
        for ( int x = 0; x < newWidth; x++ )
        {
            for ( int y = 0; y < newHeight; y++ )
            {
                if ( newTiles[ x ][ y ] == null )
                { newTiles[ x ][ y ] = new TileList( ); }
            }
        }

        this.tiles = newTiles;
    }

    /**
     * Returns the list of tiles at the specified coordinates in this layer.  The
     * returned list is unmodifiable
     *
     * @param x the x grid coordinate
     * @param y the y grid coordinate
     * @return the list of tiles at the specified coordinates
     */

    public List< Tile > getTilesAt( int x, int y )
    {
        return Collections.unmodifiableList( tiles[ x ][ y ] );
    }

    /**
     * Caches all sprites to be drawn within this layer for faster drawing
     */

    protected void cacheSprites( )
    {
        for ( int y = tiles[ 0 ].length - 1; y >= 0; y-- )
        {
            for ( int x = tiles.length - 1; x >= 0; x-- )
            {
                tiles[ x ][ y ].cacheSprites( );
            }
        }
    }

    /**
     * Adds the specified sprite to be drawn at the specified coordinates.  This method
     * enforces that no duplicate tiles are added
     *
     * @param tileID   the ID of the tile to add
     * @param spriteID the sprite to add
     * @param x        the x grid coordinate
     * @param y        the y grid coordinate
     * @return the tile that was just created
     */

    protected Tile addTile( String tileID, String spriteID, int x, int y )
    {
        Tile tile = new Tile( tileID, spriteID );

        return addTile( tile, x, y );
    }

    /**
     * Adds the specified tile to the list of tiles in this layer.  This method
     * enforces that no duplicate tiles are added
     *
     * @param tile the tile to add
     * @param x    the x grid coordinate
     * @param y    the y grid coordinate
     * @return the tile that was passed in, or the duplicate existing tile if no tile was created
     */

    protected Tile addTile( Tile tile, int x, int y )
    {
        for ( Tile existingTile : tiles[ x ][ y ] )
        {
            if ( existingTile.getTileID( ).equals( tile.getTileID( ) ) ) return existingTile;
        }

        tiles[ x ][ y ].add( tile );

        return tile;
    }

    /**
     * Removes any tiles found at the specified coordinates matching the given feature type
     *
     * @param featureType
     * @param x
     * @param y
     */

    protected void removeMatchingTiles( FeatureType featureType, int x, int y )
    {
        Iterator< Tile > iter = tiles[ x ][ y ].iterator( );

        while ( iter.hasNext( ) )
        {
            if ( featureType.getTerrainTile( iter.next( ).getTileID( ) ) != null )
            {
                iter.remove( );
            }
        }
    }

    /**
     * Removes all tiles in this layer at the specified coordinates except those with a
     * sprite ID contained in the set
     *
     * @param spriteIDs the set of sprite IDs
     * @param x         the x grid coordinate
     * @param y         the y grid coordinate
     */

    protected void removeTilesExceptMatching( Set< String > spriteIDs, int x, int y )
    {
        Iterator< Tile > iter = tiles[ x ][ y ].iterator( );
        while ( iter.hasNext( ) )
        {
            if ( ! spriteIDs.contains( iter.next( ).getSpriteID( ) ) )
            {
                iter.remove( );
            }
        }
    }

    /**
     * Removes all tiles in this layer at the specified coordinates that match
     * any of the sprite IDs contained in the set
     *
     * @param spriteIDs the set of sprite IDs to check against
     * @param x         the x grid coordinate
     * @param y         the y grid coordinate
     */

    protected void removeTilesMatching( Set< String > spriteIDs, int x, int y )
    {
        Iterator< Tile > iter = tiles[ x ][ y ].iterator( );
        while ( iter.hasNext( ) )
        {
            if ( spriteIDs.contains( iter.next( ).getSpriteID( ) ) )
            {
                iter.remove( );
            }
        }
    }

    /**
     * Removes all Tiles in this layer at the specified coordinates
     *
     * @param x the x grid coordinate
     * @param y the y grid coordinate
     */

    public void removeTiles( int x, int y )
    {
        tiles[ x ][ y ].clear( );
    }

    /**
     * Draws all tiles in this TileLayerList
     *
     * @param screenCoordinates the array of screen coordinates for the set
     *                          of grid points in this layer
     * @param topLeft           the top left grid point drawing bound, this point must be within the
     *                          bounds of the tile grid and the x coordinate must be even
     * @param bottomRight       the bottom right grid point drawing bound, this point must be within the
     *                          bounds of the tile grid
     */

    protected void draw( Point[][] screenCoordinates, Point topLeft, Point bottomRight )
    {
        for ( int y = topLeft.y; y <= bottomRight.y; y++ )
        {
            for ( int x = topLeft.x; x <= bottomRight.x; x += 2 )
            {
                draw( x, y, screenCoordinates[ x ][ y ].x, screenCoordinates[ x ][ y ].y );
            }

            for ( int x = topLeft.x + 1; x <= bottomRight.x; x += 2 )
            {
                draw( x, y, screenCoordinates[ x ][ y ].x, screenCoordinates[ x ][ y ].y );
            }
        }
    }

    private final void drawEntityTile( int x, int y, Point screen )
    {
        draw( x, y, screen.x, screen.y );

        // dont draw entities in unexplored tiles
        if ( ! explored[ x ][ y ] ) return;

        Collection< Entity > entities = area.getEntities( ).getEntitiesSet( x, y );
        if ( entities == null ) return;

        for ( Entity entity : entities )
        {
            // don't draw doors or hostiles that can't be seen
            if ( ! visibility[ x ][ y ] )
            {
                if ( entity instanceof Door ) continue;
                if ( ! entity.isPlayerFaction( ) && entity instanceof Creature ) continue;
            }

            // don't draw traps unless they have been spotted
            if ( entity instanceof Trap && ! ( ( Trap ) entity ).isSpotted( ) ) continue;

            entity.areaDraw( screen.x, screen.y );
        }

        GL11.glColor3f( 1.0f, 1.0f, 1.0f );
    }

    /**
     * Draws all tiles in this layer, also drawing entities within the layer
     *
     * @param screenCoordinates the array of screen coordinates for the set
     *                          of grid points in this layer
     * @param renderer          the renderer for the area being drawn
     * @param topLeft           the top left grid point drawing bound, this point must be within the
     *                          bounds of the tile grid and the x coordinate must be even
     * @param bottomRight       the bottom right grid point drawing bound, this point must be within the
     *                          bounds of the tile grid
     */

    protected void draw( Point[][] screenCoordinates, AreaTileGrid.AreaRenderer renderer, Point topLeft, Point bottomRight )
    {
        area = renderer.getArea( );
        visibility = area.getVisibility( );
        explored = area.getExplored( );

        for ( int y = topLeft.y; y <= bottomRight.y; y++ )
        {
            for ( int x = topLeft.x; x <= bottomRight.x; x += 2 )
            {
                drawEntityTile( x, y, screenCoordinates[ x ][ y ] );
            }

            for ( int x = topLeft.x + 1; x <= bottomRight.x; x += 2 )
            {
                drawEntityTile( x, y, screenCoordinates[ x ][ y ] );
            }
        }
    }

    private final void draw( int gridX, int gridY, int screenX, int screenY )
    {
        for ( Tile tile : tiles[ gridX ][ gridY ] )
        {
            tile.draw( screenX, screenY );
        }
    }

    private class TileList extends ArrayList< Tile >
    {
        private static final long serialVersionUID = 1L;

        private TileList( )
        {
            super( 1 );
        }

        private void cacheSprites( )
        {
            for ( Tile tile : this )
            {
                tile.cacheSprite( );
            }
        }
    }
}
