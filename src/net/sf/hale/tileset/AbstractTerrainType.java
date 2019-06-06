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

import net.sf.hale.Game;
import net.sf.hale.rules.Dice;
import net.sf.hale.util.SimpleJSONArrayEntry;
import net.sf.hale.util.SimpleJSONObject;

/**
 * The abstract base class for Terrain and Features
 *
 * @author Jared Stephen
 */

public abstract class AbstractTerrainType implements Iterable< TerrainTile >
{
    private final String id;
    private final boolean transparent;
    private final boolean passable;
    private final List< TerrainTile > tiles;
    private final TerrainTile previewTile;

    private final int totalProbability;

    /**
     * Creates a new AbstractTerrainType with the specified ID
     *
     * @param id the ID String
     */

    public AbstractTerrainType( String id, boolean transparent, boolean passable,
                                TerrainTile previewTile, List< TerrainTile > tiles )
    {
        this.id = id;
        this.transparent = transparent;
        this.passable = passable;
        this.previewTile = previewTile;
        this.tiles = Collections.unmodifiableList( tiles );

        int prob = 0;
        for ( TerrainTile tile : tiles )
        {
            prob += tile.getProbability( );
        }

        this.totalProbability = prob;
    }

    /**
     * Returns the ID for this AbstractTerrainType
     *
     * @return the ID for this AbstractTerrainType
     */

    public String getID( )
    {
        return id;
    }

    /**
     * Returns true if and only if this terrain type is transparent to creatures (whether
     * creatures can see through tiles with this terrain type)
     *
     * @return whether this terrain type is transparent
     */

    public boolean isTransparent( )
    {
        return transparent;
    }

    /**
     * Returns true if and only if this terrain type is passable by creatures
     *
     * @return whether this terrain type is passable
     */

    public boolean isPassable( )
    {
        return passable;
    }

    /**
     * Returns the tile that is shown as representing this TerrainType in the editor
     *
     * @return the previewTile for this TerrainType
     */

    public TerrainTile getPreviewTile( )
    {
        return previewTile;
    }

    /**
     * Returns true if this TerrainType contains a tile with the specified tile ID,
     * false otherwise
     *
     * @param tileID the id of the tile
     * @return true if and only if a tile with the specified ID is contained in this
     * terrainType
     */

    public boolean containsTileWithID( String tileID )
    {
        for ( TerrainTile tile : tiles )
        {
            if ( tile.getID( ).equals( tileID ) ) return true;
        }

        return false;
    }

    /**
     * Returns the terrain tile with a matching tile ID if one exists
     *
     * @param tileID
     * @return the terrain tile, or null if none exists
     */

    public TerrainTile getTerrainTile( String tileID )
    {
        for ( TerrainTile tile : tiles )
        {
            if ( tile.getID( ).equals( tileID ) ) return tile;
        }

        return null;
    }

    /**
     * Randomly returns one terrain tile from the list of terrain tiles in this TerrainType.
     * The tiles are weighted based on their individual probability factors
     *
     * @return a randomly chosen terrain tile, or null if this terrain type does not have a terrain tile
     */

    public TerrainTile getRandomTerrainTile( )
    {
        return getRandomTerrainTile( Game.dice );
    }

    /**
     * Randomly returns one terrain tile from the list of terrain tiles in this TerrainType.
     * The tiles are weighted based on their individual probability factors.  The random number
     * if pulled from the specified Dice
     *
     * @param dice
     * @return a randomly chosen terrain tile, or null if this terrain type does not have a terrain tile
     */

    public TerrainTile getRandomTerrainTile( Dice dice )
    {
        int randValue = dice.rand( 1, totalProbability );
        int curProbability = 0;
        for ( TerrainTile tile : tiles )
        {
            curProbability += tile.getProbability( );
            if ( randValue <= curProbability ) return tile;
        }

        return null;
    }

    @Override
    public Iterator< TerrainTile > iterator( )
    {
        return tiles.iterator( );
    }

    /**
     * Returns the list of tiles contained in the specified JSON data object
     *
     * @param data the JSON data to parse
     * @return the list of tiles
     */

    public static List< TerrainTile > parseTiles( SimpleJSONObject data )
    {
        ArrayList< TerrainTile > tiles = new ArrayList< TerrainTile >( );

        for ( SimpleJSONArrayEntry entry : data.getArray( "tiles" ) )
        {
            SimpleJSONObject tileObject = entry.getObject( );
            TerrainTile tile = new TerrainTile( tileObject.get( "id", null ),
                                                tileObject.get( "layer", null ), tileObject.get( "probability", 0 ) );

            tiles.add( tile );
        }
        tiles.trimToSize( );

        return tiles;
    }

    /**
     * Returns the previewTile contained in the specified JSON data, or null if
     * no previewTileObject is specified
     *
     * @param data the JSON data to parse
     * @return the preview tile
     */

    public static TerrainTile parsePreviewTile( SimpleJSONObject data )
    {
        if ( ! data.containsKey( "previewTile" ) ) return null;

        SimpleJSONObject previewTileObject = data.getObject( "previewTile" );

        return new TerrainTile( previewTileObject.get( "id", null ),
                                previewTileObject.get( "layer", null ), 0 );
    }
}
