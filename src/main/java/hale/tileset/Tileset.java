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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import main.java.hale.util.AreaUtil;
import main.java.hale.util.Point;
import main.java.hale.util.SimpleJSONArray;
import main.java.hale.util.SimpleJSONArrayEntry;
import main.java.hale.util.SimpleJSONObject;
import main.java.hale.util.SimpleJSONParser;

/**
 * A class represeting a set of tiles that are used as a palette for a given
 * area.  All of the tiles within a given area must be within one tileset.  There
 * can potentially be many tilesets, each providing a separate environment, such
 * as a cave, a dungeon, a desert, or a swamp.  This class is immutable after
 * creation
 *
 * @author Jared Stephen
 */

public class Tileset
{
    private final String id;
    private final String name;
    private final String directory;

    private final Map<String, Layer> layers;
    private final String entityLayerID;
    private final String interfaceLayerID;

    private final Map<String, TerrainType> terrainTypes;
    private final Map<String, FeatureType> featureTypes;

    private final Map<String, BorderList> borderLists;

    private final int defaultVisibilityRadius;
    private final String defaultTerrainType;

    private final ElevationList elevationList;

    /**
     * Creates a new Tileset with the specified ID, reading in the tileset
     * data file at the specified resource ID.
     *
     * @param id       the ID for this tileset
     * @param resource the resource ID of the resource containing tileset data
     */

    public Tileset(String id, String resource)
    {
        this.id = id;
        this.layers = new LinkedHashMap<String, Layer>();

        SimpleJSONParser parser = new SimpleJSONParser(resource);
        parser.setWarnOnMissingKeys(true);

        this.name = parser.get("name", id);
        this.directory = parser.get("directory", null);
        this.entityLayerID = parser.get("entityLayer", null);
        this.interfaceLayerID = parser.get("interfaceLayer", null);

        parser.setWarnOnMissingKeys(false);

        this.defaultVisibilityRadius = parser.get("defaultVisibilityRadius", 10);
        this.defaultTerrainType = parser.get("defaultTerrainType", null);

        // parse elevation rules if they exist
        SimpleJSONArray elevationObject = parser.getArray("elevation");
        if (elevationObject != null) {
            this.elevationList = ElevationList.parse(elevationObject);
        } else {
            this.elevationList = null;
        }

        parser.setWarnOnMissingKeys(true);

        // parse list of layers, note that the ordering in this list is important
        for (SimpleJSONArrayEntry entry : parser.getArray("layers")) {
            SimpleJSONObject layerObject = entry.getObject();

            List<String> layerSpriteSheets = new ArrayList<String>();
            String layerID = layerObject.get("id", null);
            for (SimpleJSONArrayEntry layerEntry : layerObject.getArray("spriteSheets")) {
                String sheetID = layerEntry.getString();
                layerSpriteSheets.add(sheetID);
            }

            this.layers.put(layerID, new Layer(layerID, directory, layerSpriteSheets));
        }

        //parse set of feature types
        this.featureTypes = new HashMap<String, FeatureType>();
        SimpleJSONObject featureTypeObject = parser.getObject("features");
        for (String featureID : featureTypeObject.keySet()) {
            SimpleJSONObject featureObject = featureTypeObject.getObject(featureID);

            FeatureType featureType = FeatureType.parse(featureObject, featureID);
            featureTypes.put(featureID, featureType);
        }

        List<String> terrainBorderPriority = new ArrayList<String>();
        if (parser.containsKey("terrainBorderPriority")) {
            SimpleJSONArray priorityArray = parser.getArray("terrainBorderPriority");

            for (SimpleJSONArrayEntry entry : priorityArray) {
                terrainBorderPriority.add(entry.getString());
            }
        }

        // parse set of terrain types
        this.terrainTypes = new HashMap<String, TerrainType>();
        SimpleJSONObject terrainTypeObject = parser.getObject("terrain");
        for (String terrainID : terrainTypeObject.keySet()) {
            SimpleJSONObject terrainObject = terrainTypeObject.getObject(terrainID);

            TerrainType terrainType = TerrainType.parse(terrainObject, terrainID, terrainBorderPriority);
            terrainTypes.put(terrainID, terrainType);
        }

        // parse set of border types
        this.borderLists = new HashMap<String, BorderList>();
        SimpleJSONObject borderListObject = parser.getObject("borders");
        for (String borderID : borderListObject.keySet()) {
            SimpleJSONArray borderArray = borderListObject.getArray(borderID);

            BorderList borderList = BorderList.parse(borderArray, borderID);
            borderLists.put(borderID, borderList);
        }

        parser.warnOnUnusedKeys();
    }

    /**
     * Parses all Spritesheets for all layers in this Tileset.  All sprites will
     * be loaded via the standard async texture loader
     */

    public void loadTiles()
    {
        for (String layerID : layers.keySet()) {
            layers.get(layerID).loadTiles();
        }
    }

    /**
     * Frees up all texture memory associated with this tileset
     */

    public void freeTiles()
    {
        for (String layerID : layers.keySet()) {
            layers.get(layerID).freeTiles();
        }
    }

    /**
     * Returns the set of all layer IDs contained in this tileset.  This set
     * is ordered, with layer IDs returned in the order in which the layers are drawn.
     * The returned set is unmodifiable
     *
     * @return the set of all layer IDs contained in this tileset
     */

    public Set<String> getLayerIDs()
    {
        return Collections.unmodifiableSet(layers.keySet());
    }

    /**
     * Returns the set of all terrain type IDs contained in this tileset.  The set is
     * unordered and unmodifiable
     *
     * @return the set of all terrain type IDs
     */

    public Set<String> getTerrainTypeIDs()
    {
        return Collections.unmodifiableSet(terrainTypes.keySet());
    }

    /**
     * Returns the set of all feature type IDs contained in this tileset.  This set is
     * unordered and unmodifiable
     *
     * @return the set of all feature type IDs
     */

    public Set<String> getFeatureTypeIDs()
    {
        return Collections.unmodifiableSet(featureTypes.keySet());
    }

    /**
     * Returns the elevation list for this tileset
     *
     * @return the elevation list
     */

    public ElevationList getElevationList()
    {
        return elevationList;
    }

    /**
     * Returns the set of border list matching the terrain at the specified point relative
     * to the adjacent terrain, or null if there is no matching list.  A single adjacent tile
     * must be the terrain type specified for the border list in order for the list to match
     *
     * @param terrain the grid of terrain
     * @param center  the point to find the border list for
     * @return the matching border list
     */

    public Set<BorderList> getMatchingBorderLists(TerrainType[][] terrain, Point center)
    {
        Set<BorderList> lists = new HashSet<BorderList>();

        TerrainType centerType = terrain[center.x][center.y];
        if (centerType == null) return lists;

        Point[] adjacent = AreaUtil.getAdjacentTiles(center);
        for (Point p : adjacent) {
            if (p.x < 0 || p.x >= terrain.length || p.y < 0 || p.y >= terrain[0].length) {
                continue;
            }

            if (terrain[p.x][p.y] == null) {
                continue;
            }

            String id = centerType.getBorderIDWith(terrain[p.x][p.y]);
            if (id != null) {
                lists.add(borderLists.get(id));
            }
        }

        return lists;
    }

    /**
     * Returns the FeatureType with the specified ID within this tileset, or null
     * if no such feature type exists
     *
     * @param featureTypeID the ID for the feature type
     * @return the FeatureType with the specified ID
     */

    public FeatureType getFeatureType(String featureTypeID)
    {
        return featureTypes.get(featureTypeID);
    }

    /**
     * Returns the TerrainType with the specified ID contained in this tileset,
     * or null if no such terrain type is found
     *
     * @param terrainTypeID the ID for the Terrain Type
     * @return the TerrainType with the specified ID
     */

    public TerrainType getTerrainType(String terrainTypeID)
    {
        return terrainTypes.get(terrainTypeID);
    }

    /**
     * Returns the layer with the specified ID within this tileset,
     * or null if there is no layer with that ID
     *
     * @param layerID the ID of the layer
     * @return the layer with the specified ID
     */

    public Layer getLayer(String layerID)
    {
        return layers.get(layerID);
    }

    /**
     * Returns the String ID of the interface layer.  Interface elements such as
     * the currently hovered tile and the selected entity are drawn immediately
     * after this layer
     *
     * @return the ID of the interface layer
     */

    public String getInterfaceLayerID()
    {
        return interfaceLayerID;
    }

    /**
     * Returns the String ID of the entity layer, the Layer that is drawn
     * at the same time as all Entities within a given Area
     *
     * @return the ID of the entity layer
     */

    public String getEntityLayerID()
    {
        return entityLayerID;
    }

    /**
     * Returns the visibility radius that areas created with this tileset will default to
     *
     * @return the default visibility radius
     */

    public int getDefaultVisibilityRadius()
    {
        return defaultVisibilityRadius;
    }

    /**
     * Returns the ID String of the default terrain type, that will fill newly created
     * areas.  This may return null, in which case there is no default terrain type
     *
     * @return the default terrain type
     */

    public String getDefaultTerrainType()
    {
        return defaultTerrainType;
    }

    /**
     * Returns the ID for this tileset
     *
     * @return the ID for this tileset
     */

    public String getID()
    {
        return id;
    }

    /**
     * Returns the descriptive name for this tileset
     *
     * @return the descriptive name for this tileset
     */

    public String getName()
    {
        return name;
    }
}
