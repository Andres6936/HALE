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

import net.sf.hale.util.SimpleJSONObject;

/**
 * A tile with a specified tile ID and relative position
 *
 * @author Jared Stephen
 */

public class BorderTile
{
    private final String id;
    private final String layerID;
    private final DirectionList position;

    /**
     * Creates a new BorderTile
     *
     * @param id       the tile ID
     * @param layerID  the layer ID for the layer containing this tile
     * @param position the position of this tile relative to the tile being
     *                 checked for the border condition
     */

    private BorderTile( String id, String layerID, DirectionList position )
    {
        this.id = id;
        this.layerID = layerID;
        this.position = position;
    }

    /**
     * returns the tileID for this Tile
     *
     * @return the TileID
     */

    public String getID( )
    {
        return id;
    }

    /**
     * Returns the Layer ID for this tile
     *
     * @return the Layer ID for this tile
     */

    public String getLayerID( )
    {
        return layerID;
    }

    /**
     * Returns the position of this Tile, relative to the tile for which the
     * border condition is being checked
     *
     * @return the position of this Tile
     */

    public DirectionList getPosition( )
    {
        return position;
    }

    /**
     * Creates a new BorderTile by parsing the data in the specified JSON object
     *
     * @param object the object to parse
     * @return a new BorderTile
     */

    protected static BorderTile parse( SimpleJSONObject object )
    {
        String tileID = object.get( "id", null );
        String layerID = object.get( "layer", null );
        DirectionList position = DirectionList.parse( object.get( "position", null ) );

        return new BorderTile( tileID, layerID, position );
    }
}