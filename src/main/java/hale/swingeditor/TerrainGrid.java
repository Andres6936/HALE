package main.java.hale.swingeditor;

import java.util.ArrayList;
import java.util.List;

import main.java.hale.Game;
import main.java.hale.area.Area;
import main.java.hale.tileset.Border;
import main.java.hale.tileset.BorderList;
import main.java.hale.tileset.BorderTile;
import main.java.hale.tileset.ElevationList;
import main.java.hale.tileset.FeatureType;
import main.java.hale.tileset.TerrainTile;
import main.java.hale.tileset.TerrainType;
import main.java.hale.tileset.Tile;
import main.java.hale.tileset.TileLayerList;
import main.java.hale.tileset.Tileset;
import main.java.hale.util.AreaUtil;
import main.java.hale.util.Point;
import main.java.hale.util.PointImmutable;

/**
 * for editing the tiles that make up an area. terrain type is defined for each tile and then border
 * tiles can be added based on that
 *
 * @author jared
 */

public class TerrainGrid
{
    private final Tileset tileset;
    private final Area area;

    private final int width, height;
    private final TerrainType[][] terrain;
    private final TerrainTile[][] terrainTiles; // the actual tiles present on each terrain location

    private final FeatureType[][] features;
    private final TerrainTile[][] featureTiles;

    /**
     * Creates a new terrain grid for the given area.  initializes the terrain type for each
     * part of the grid
     *
     * @param area
     */

    public TerrainGrid(Area area)
    {
        this.tileset = Game.curCampaign.getTileset(area.getTileset());
        this.area = area;
        this.width = area.getWidth();
        this.height = area.getHeight();

        terrain = new TerrainType[width][height];
        terrainTiles = new TerrainTile[width][height];
        features = new FeatureType[width][height];
        featureTiles = new TerrainTile[width][height];

        List<Tile> tiles = new ArrayList<Tile>();

        // at each grid point, figure out what the terrain type is
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles.clear();

                for (String layerID : area.getTileGrid().getLayerIDs()) {
                    tiles.addAll(area.getTileGrid().getLayer(layerID).getTilesAt(x, y));
                }

                setTerrainType(tiles, x, y);

                setFeatureType(tiles, x, y);
            }
        }
    }

    /**
     * Sets the feature type at the specified coordinates based on the list of tiles.  if no
     * matching feature is found, nothing is set
     *
     * @param tiles
     * @param x
     * @param y
     */

    private void setFeatureType(List<Tile> tiles, int x, int y)
    {
        for (String featureTypeID : tileset.getFeatureTypeIDs()) {
            FeatureType featureType = tileset.getFeatureType(featureTypeID);

            for (Tile tile : tiles) {
                TerrainTile featureTile = featureType.getTerrainTile(tile.getTileID());

                if (featureTile != null) {
                    features[x][y] = featureType;
                    featureTiles[x][y] = featureTile;
                }
            }
        }
    }

    /**
     * Sets the terrain type and terrain tile at the specified coordinates based on the list of
     * tiles.  if no matching terrain is found, nothing is set
     *
     * @param tiles
     * @param x
     * @param y
     */

    private void setTerrainType(List<Tile> tiles, int x, int y)
    {
        for (String terrainTypeID : tileset.getTerrainTypeIDs()) {
            TerrainType terrainType = tileset.getTerrainType(terrainTypeID);

            for (Tile tile : tiles) {
                TerrainTile terrainTile = terrainType.getTerrainTile(tile.getTileID());

                if (terrainTile != null) {
                    terrain[x][y] = terrainType;
                    terrainTiles[x][y] = terrainTile;

                    return;
                }
            }
        }
    }

    /**
     * Modifies the elevation at the specified grid points by the specified delta amount.
     * For points outside the area bounds, no action is taken
     *
     * @param x
     * @param y
     * @param r
     * @param delta
     */

    public void modifyElevation(int x, int y, int r, byte delta)
    {
        for (PointImmutable p : area.getPoints(x, y, r)) {
            modifyElevation(p.x, p.y, delta);
        }

        setTerrain(x, y, r, null); // update tiles but don't modify terrain
    }

    private void modifyElevation(int x, int y, byte delta)
    {
        area.getElevationGrid().modifyElevation(x, y, delta);
    }

    private void addBorderTiles(int x, int y)
    {
        if (terrain[x][y] == null) return;

        Point p = new Point(x, y);

        for (BorderList borderList : tileset.getMatchingBorderLists(terrain, p)) {
            for (Border border : borderList.getMatchingBorders(terrain, p)) {
                for (BorderTile borderTile : border) {
                    Point borderPoint = borderTile.getPosition().getRelativePoint(p);

                    PointImmutable bP = new PointImmutable(borderPoint);
                    if (!bP.isWithinBounds(area)) continue;

                    area.getTileGrid().addTile(borderTile.getID(), borderTile.getLayerID(), bP.x, bP.y);
                }
            }
        }

        for (ElevationList.Elevation elevation : tileset.getElevationList().
                getMatchingElevationRules(area.getElevationGrid(), p)) {

            Border border = elevation.getBorder();

            for (BorderTile borderTile : border) {
                Point borderPoint = borderTile.getPosition().getRelativePoint(p);

                PointImmutable bP = new PointImmutable(borderPoint);
                if (!bP.isWithinBounds(area)) continue;

                area.getTileGrid().addTile(borderTile.getID(), borderTile.getLayerID(), bP.x, bP.y);
            }
        }
    }

    /**
     * Removes all tiles at the specified grid points that are from a feature.  for
     * any points outside the area bounds, no action is taken
     *
     * @param x the grid x coordinate
     * @param y the grid y coordinate
     * @param r the grid radius
     */

    public void removeFeatureTiles(int x, int y, int r)
    {
        for (PointImmutable p : area.getPoints(x, y, r)) {
            removeFeatureTiles(p.x, p.y);
        }
    }

    private void removeFeatureTiles(int x, int y)
    {
        FeatureType featureType = features[x][y];
        if (featureType == null) return;

        area.getTileGrid().removeMatchingTiles(featureType, x, y);

        features[x][y] = null;
        featureTiles[x][y] = null;
    }

    /**
     * Removes all tiles and clears the terrain at the specified grid points.  for
     * any grid points outside the area bounds, no action is taken
     *
     * @param x the grid x coordinate
     * @param y the grid y coordinate
     * @param r the grid radius
     */

    public void removeAllTiles(int x, int y, int r)
    {
        for (PointImmutable p : area.getPoints(x, y, r)) {
            removeAllTiles(p.x, p.y);
        }
    }

    private void removeAllTiles(int x, int y)
    {
        for (String layerID : area.getTileGrid().getLayerIDs()) {
            TileLayerList list = area.getTileGrid().getLayer(layerID);

            list.removeTiles(x, y);

            terrain[x][y] = null;
            terrainTiles[x][y] = null;
            features[x][y] = null;
            featureTiles[x][y] = null;
        }
    }

    private void removeButNotTerrain(int x, int y)
    {
        for (String layerID : area.getTileGrid().getLayerIDs()) {
            TileLayerList list = area.getTileGrid().getLayer(layerID);

            list.removeTiles(x, y);
        }
    }

    /**
     * Sets the feature type at the specified grid points.  sets an appropriate tile
     * based on feature.  for any points outside the area bounds, no action is taken
     *
     * @param x      the grid x coordinate
     * @param y      the grid y coordinate
     * @param radius the grid radius
     * @param type
     */

    public void setFeature(int x, int y, int radius, FeatureType type)
    {
        PointImmutable center = new PointImmutable(x, y);

        if (center.isWithinBounds(area)) {
            setFeature(center.x, center.y, type);
            setTransparencyAndPassability(center.x, center.y);
        }

        for (int r = 1; r <= radius; r++) {
            for (int i = 0; i < 6 * r; i++) {
                PointImmutable p = new PointImmutable(AreaUtil.convertPolarToGrid(x, y, r, i));

                if (!p.isWithinBounds(area)) continue;

                setFeature(p.x, p.y, type);
                setTransparencyAndPassability(p.x, p.y);
            }
        }

        area.getTileGrid().cacheSprites();
    }

    private void setFeature(int x, int y, FeatureType type)
    {
        if (type == null) return;

        if (features[x][y] != null) {
            area.getTileGrid().removeMatchingTiles(features[x][y], x, y);
        }

        TerrainTile tile = type.getRandomTerrainTile();
        features[x][y] = type;
        featureTiles[x][y] = tile;
        area.getTileGrid().addTile(tile.getID(), tile.getLayerID(), x, y);
    }

    /**
     * Sets the terrain type at the specified grid points.  sets an appropriate
     * tile based on the terrain.  for any grid points outside the area bounds,
     * no action is taken
     *
     * @param x      the grid x coordinate
     * @param y      the grid y coordinate
     * @param radius the grid radius
     * @param type
     */

    public void setTerrain(int x, int y, int radius, TerrainType type)
    {
        PointImmutable center = new PointImmutable(x, y);

        if (center.isWithinBounds(area)) {
            removeButNotTerrain(x, y);
        }

        for (int r = 1; r <= radius + 1; r++) {
            for (int i = 0; i < 6 * r; i++) {
                PointImmutable p = new PointImmutable(AreaUtil.convertPolarToGrid(x, y, r, i));

                if (!p.isWithinBounds(area)) continue;

                removeButNotTerrain(p.x, p.y);
            }
        }

        if (center.isWithinBounds(area)) {
            setTerrain(x, y, type);
            setFeature(x, y, features[x][y]);
        }

        for (int r = 1; r <= radius; r++) {
            for (int i = 0; i < 6 * r; i++) {
                PointImmutable p = new PointImmutable(AreaUtil.convertPolarToGrid(x, y, r, i));

                if (!p.isWithinBounds(area)) continue;

                setTerrain(p.x, p.y, type);
                setFeature(p.x, p.y, features[p.x][p.y]);
            }
        }

        // re-add nearby tiles
        for (int i = 0; i < 6 * (radius + 1); i++) {
            PointImmutable p = new PointImmutable(AreaUtil.convertPolarToGrid(x, y, radius + 1, i));

            if (!p.isWithinBounds(area)) continue;

            setTerrain(p.x, p.y, terrain[p.x][p.y]);
            setFeature(p.x, p.y, features[p.x][p.y]);
        }

        // re-add nearby border tiles
        for (int i = 0; i < 6 * (radius + 2); i++) {
            PointImmutable p = new PointImmutable(AreaUtil.convertPolarToGrid(x, y, radius + 2, i));

            if (!p.isWithinBounds(area)) continue;

            addBorderTiles(p.x, p.y);
        }

        area.getTileGrid().cacheSprites();

        // compute passability & transparency
        if (center.isWithinBounds(area)) {
            setTransparencyAndPassability(center.x, center.y);
        }

        for (int r = 1; r <= radius + 2; r++) {
            for (int i = 0; i < 6 * r; i++) {
                PointImmutable p = new PointImmutable(AreaUtil.convertPolarToGrid(x, y, r, i));

                if (!p.isWithinBounds(area)) continue;

                setTransparencyAndPassability(p.x, p.y);
            }
        }
    }

    private void setTransparencyAndPassability(int x, int y)
    {
        // set passability
        boolean passable = true;

        if (terrain[x][y] != null && !terrain[x][y].isPassable()) passable = false;

        if (features[x][y] != null && !features[x][y].isPassable()) passable = false;

        if (area.getElevationGrid().getElevation(x, y) != 0) passable = false;

        area.getPassability()[x][y] = passable;

        // set transparency
        boolean transparent = true;

        if (terrain[x][y] != null && !terrain[x][y].isTransparent()) transparent = false;

        if (features[x][y] != null && !features[x][y].isTransparent()) transparent = false;

        if (area.getElevationGrid().getElevation(x, y) > 0) transparent = false;

        area.getTransparency()[x][y] = transparent;
    }

    private void setTerrain(int x, int y, TerrainType type)
    {
        if (type != null) {
            TerrainTile tile = type.getRandomTerrainTile();
            terrain[x][y] = type;
            terrainTiles[x][y] = tile;
            area.getTileGrid().addTile(tile.getID(), tile.getLayerID(), x, y);

            addBorderTiles(x, y);
        } else {
            if (terrainTiles[x][y] != null) {
                area.getTileGrid().addTile(terrainTiles[x][y].getID(), terrainTiles[x][y].getLayerID(), x, y);
            }

            addBorderTiles(x, y);
        }
    }
}
