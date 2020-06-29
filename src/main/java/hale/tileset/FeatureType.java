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

package hale.tileset;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hale.util.SimpleJSONArrayEntry;
import hale.util.SimpleJSONObject;

/**
 * A feature with a set of standard tiles and terrain that it can be added to
 *
 * @author Jared Stephen
 */

public class FeatureType extends AbstractTerrainType
{
    private final Set<String> terrainTypeIDs;

    private FeatureType(String id, boolean transparent, boolean passable,
                        TerrainTile previewTile, List<TerrainTile> tiles)
    {
        super(id, transparent, passable, previewTile, tiles);

        terrainTypeIDs = new HashSet<String>();
    }

    /**
     * Returns true if and only if this FeatureType is valid and can be added to areas
     * with the specified TerrainType
     *
     * @param terrainType the TerrainType to check
     * @return true if and only if this FeatureType can be added to the TerrainType
     */

    public boolean canBeAddedTo(TerrainType terrainType)
    {
        if (terrainType == null) return false;

        return terrainTypeIDs.contains(terrainType.getID());
    }

    public static FeatureType parse(SimpleJSONObject data, String id)
    {
        List<TerrainTile> tiles = AbstractTerrainType.parseTiles(data);

        // default the previewTile to the first tile
        TerrainTile previewTile = AbstractTerrainType.parsePreviewTile(data);
        if (previewTile == null) previewTile = tiles.get(0);

        data.setWarnOnMissingKeys(false);

        boolean passable = data.get("passable", false);
        boolean transparent = data.get("transparent", true);

        data.setWarnOnMissingKeys(true);

        FeatureType featureType = new FeatureType(id, transparent, passable, previewTile, tiles);

        for (SimpleJSONArrayEntry entry : data.getArray("terrainTypes")) {
            featureType.terrainTypeIDs.add(entry.getString());
        }

        return featureType;
    }
}
