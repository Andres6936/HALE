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

package main.java.hale.tileset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import main.java.hale.resource.ResourceType;
import main.java.hale.resource.Sprite;
import main.java.hale.resource.SpriteManager;

/**
 * A Layer is a set of tiles that can all be drawn at the same time.  When drawing
 * an area, each layer is drawn one at a time, in order, with higher layers drawn on
 * top of lower layers.  Within the layer, tiles are drawn in order based on their
 * position
 *
 * @author Jared Stephen
 */

public class Layer
{
    private String id;
    private String directory;

    private List<String> spriteSheets;
    private List<String> textureSprites;
    private Set<String> tiles;

    /**
     * Creates a new Layer with the specified ID.  The spritesheets with the
     * specified resource IDs contain all the tiles for this Layer.
     *
     * @param id           the ID for this layer
     * @param spriteSheets the list of all SpriteSheet resource IDs containing
     *                     tiles for this Layer
     */

    protected Layer(String id, String directory, List<String> spriteSheets)
    {
        this.id = id;
        this.directory = directory;
        this.spriteSheets = spriteSheets;
        this.tiles = new LinkedHashSet<String>();
        this.textureSprites = new ArrayList<String>();
    }

    /**
     * Gets the Sprite ID within this Layer with the specified tile ID
     *
     * @param tileID the ID string for the sprite
     * @return the String ID for the Tile with the specified tile ID
     */

    public String getSpriteID(String tileID)
    {
        return directory + "/" + tileID + ResourceType.PNG.getExtension();
    }

    /**
     * Loads all spritesheets associated with this layer, adding all Tiles into
     * the list of tiles for this Layer
     */

    protected void loadTiles()
    {
        tiles.clear();

        for (String spriteSheet : spriteSheets) {
            List<String> tileIDs = SpriteManager.readSpriteSheet(directory + "/" + spriteSheet +
                    ResourceType.JSON.getExtension());

            Iterator<String> iter = tileIDs.iterator();
            // if empty spritesheet, continue
            if (!iter.hasNext()) continue;

            // add one sprite for each sprite sheet to enable deleting the texture later as needed
            String tileID = iter.next();
            String spriteID = directory + "/" + tileID + ResourceType.PNG.getExtension();
            textureSprites.add(spriteID);
            tiles.add(tileID);

            // add all other sprites to the list of tiles
            while (iter.hasNext()) {
                tileID = iter.next();
                tiles.add(tileID);
            }
        }
    }

    /**
     * Returns the set of all tiles for this Layer.  The returned set is
     * unmodifiable.  Each returned string represents the tile ID
     * of a tile
     *
     * @return the set of all tiles for this Layer
     */

    public Set<String> getTiles()
    {
        return Collections.unmodifiableSet(tiles);
    }

    /**
     * Frees all texture memory associated with tiles in this Layer
     */

    protected void freeTiles()
    {
        for (String spriteString : textureSprites) {
            Sprite sprite = SpriteManager.getImage(spriteString);
            SpriteManager.freeTexture(sprite);
        }

        textureSprites.clear();
        tiles.clear();
    }

    /**
     * Returns the ID for this Layer
     *
     * @return the ID for this Layer
     */

    public String getID()
    {
        return id;
    }
}
