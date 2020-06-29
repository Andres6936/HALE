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

/**
 * A single tile with a specified tile ID and layer, as well as a probability of
 * being generated.  This class is immutable
 *
 * @author Jared Stephen
 */

public class TerrainTile
{
    private String id;
    private String layerID;
    private int probability;

    /**
     * Creates a new TerrainTile
     *
     * @param id          the Tile ID
     * @param layerID     the ID of the layer that contains this Tile
     * @param probability the probability weight for picking this tile when generating
     *                    terrain.  The absolute probability will be this value divided by the total
     *                    probability of all terrain tiles.
     */

    protected TerrainTile(String id, String layerID, int probability)
    {
        this.id = id;
        this.layerID = layerID;
        this.probability = probability;
    }

    /**
     * Returns the probability weight for this TerrainTile
     *
     * @return the probability weight for this TerrainTile
     */

    public int getProbability()
    {
        return probability;
    }

    /**
     * Returns the Layer ID for this TerrainTile
     *
     * @return the Layer ID for this TerrainTile
     */

    public String getLayerID()
    {
        return layerID;
    }

    /**
     * Returns the Tile ID for this TerrainTile
     *
     * @return the Tile ID for this TerrainTile
     */

    public String getID()
    {
        return id;
    }
}
