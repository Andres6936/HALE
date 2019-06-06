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

import net.sf.hale.Game;
import net.sf.hale.resource.Sprite;
import net.sf.hale.resource.SpriteManager;

/**
 * A single tile, drawn in layers as part of an area in the main game view
 *
 * @author Jared Stephen
 */

public class Tile
{
    private final String tileID;
    private final String spriteID;

    private Sprite sprite;
    private int offsetX, offsetY;

    /**
     * Creates a new Tile with the specified tile ID and sprite ID (used by the
     * spritemanager)
     *
     * @param tileID   the Tile ID of this Tile (the String used to identify it in area files)
     * @param spriteID the Sprite ID of this Tile (the String used to identify for the SpriteManager)
     */

    public Tile( String tileID, String spriteID )
    {
        this.tileID = tileID;
        this.spriteID = spriteID;
    }

    /**
     * Loads the sprite for this Tile from the SpriteManager for faster
     * drawing
     */

    public void cacheSprite( )
    {
        this.sprite = SpriteManager.getImage( spriteID );

        this.offsetX = ( Game.TILE_SIZE - sprite.getWidth( ) ) / 2;
        this.offsetY = ( Game.TILE_SIZE - sprite.getHeight( ) ) / 2;
    }

    /**
     * Draws this tile at the specified screen coordinates
     *
     * @param screenX the x coordinate
     * @param screenY the y coordinate
     */

    public final void draw( int screenX, int screenY )
    {
        sprite.draw( screenX + offsetX, screenY + offsetY );
    }

    /**
     * Returns the Tile ID for this Tile
     *
     * @return the Tile ID for this Tile
     */

    public String getTileID( )
    {
        return tileID;
    }

    /**
     * Returns the Sprite ID for this Tile
     *
     * @return the Sprite ID for this Tile
     */

    public String getSpriteID( )
    {
        return spriteID;
    }
}
