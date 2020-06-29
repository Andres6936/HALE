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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hale.util.SimpleJSONArray;
import hale.util.SimpleJSONArrayEntry;
import hale.util.SimpleJSONObject;

/**
 * A single type of terrain.  Contains a list of tiles which are randomly
 * selected, as well as borders with other terrain types.
 *
 * @author Jared Stephen
 */

public class TerrainType extends AbstractTerrainType
{
    private final Map<String, String> borders;

    private TerrainType(String id, boolean transparent, boolean passable,
                        TerrainTile previewTile, List<TerrainTile> tiles)
    {

        super(id, transparent, passable, previewTile, tiles);

        this.borders = new HashMap<String, String>();
    }

    /**
     * Returns the ID String for the BorderList between this terrainType
     * and the specified terrainType, or null if no borderList exists for
     * that type
     *
     * @param terrainType the terrainType to check
     * @return the ID for the BorderList between this terrainType and the specified
     * terrainType
     */

    public String getBorderIDWith(TerrainType terrainType)
    {
        if (terrainType == null) return null;

        return borders.get(terrainType.getID());
    }

    /**
     * Creates a new TerrainType from the data contained in the specified JSON object
     *
     * @param data                  the JSON data for the TerrainType
     * @param id                    the ID String for the TerrainType
     * @param terrainBorderPriority the list used to determine which terraintypes this terraintype
     *                              should share its default border with.  the list is iterated through until this terrain type is found,
     *                              and then stops.  this terrain type will be given the default border with all terrain types up to that
     *                              point.  this means that if this terrain type's id is not in the list, it will be given the default
     *                              border with all terrain types in the list.  note that this parameter only functions if the terrain type
     *                              specifies a default border in the JSON, and default borders can be overriden by a specified border in the
     *                              JSON
     * @return the newly created TerrainType
     */

    public static TerrainType parse(SimpleJSONObject data, String id, List<String> terrainBorderPriority)
    {
        List<TerrainTile> tiles = AbstractTerrainType.parseTiles(data);

        // default the previewTile to the first tile
        TerrainTile previewTile = AbstractTerrainType.parsePreviewTile(data);
        if (previewTile == null) previewTile = tiles.get(0);

        data.setWarnOnMissingKeys(false);

        boolean passable = data.get("passable", true);
        boolean transparent = data.get("transparent", true);

        TerrainType terrainType = new TerrainType(id, transparent, passable, previewTile,
                tiles);

        if (data.containsKey("defaultBorder")) {
            String defaultBorder = data.get("defaultBorder", null);

            for (String otherTerrainTypeID : terrainBorderPriority) {
                if (otherTerrainTypeID.equals(id)) break;

                terrainType.borders.put(otherTerrainTypeID, defaultBorder);
            }
        }

        SimpleJSONArray array = data.getArray("borders");
        for (SimpleJSONArrayEntry entry : array) {
            SimpleJSONObject borderObject = entry.getObject();
            String with = borderObject.get("with", null);
            String borderID = borderObject.get("id", null);
            terrainType.borders.put(with, borderID);
        }

        data.setWarnOnMissingKeys(true);

        return terrainType;
    }
}
