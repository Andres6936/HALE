package main.java.hale.area;

import main.java.hale.Game;
import main.java.hale.rules.Dice;
import main.java.hale.tileset.Border;
import main.java.hale.tileset.BorderTile;
import main.java.hale.tileset.ElevationList;
import main.java.hale.tileset.Tileset;
import main.java.hale.util.Point;
import main.java.hale.util.PointImmutable;
import main.java.hale.util.SimpleJSONObject;

/**
 * breaks the area down into a random grid and builds a maze
 * using the walls of the grid
 *
 * @author Jared
 */

public class GridGenerator implements Generator
{
    private Dice random;
    private final Area area;
    private final Tileset tileset;

    private final int gridSize;
    private final int gridWidth, gridHeight;

    /**
     * Creates a new generator for the specified area
     *
     * @param area
     * @param random
     */

    public GridGenerator(Area area, SimpleJSONObject data)
    {
        this.area = area;
        this.tileset = Game.curCampaign.getTileset(area.getTileset());

        this.gridSize = data.get("gridSize", 0);

        gridWidth = area.getWidth() / gridSize;
        gridHeight = area.getHeight() / gridSize;
    }

    @Override
    public void setDice(Dice random)
    {
        this.random = random;
    }

    /**
     * Generates tiles with this generator
     */

    @Override
    public void generate()
    {
        int[][] cells = new int[gridWidth][gridHeight];
        generateMaze(cells, 0, 0);

        for (int cellX = 0; cellX < cells.length; cellX++) {
            for (int cellY = 0; cellY < cells[0].length; cellY++) {

                if ((cells[cellX][cellY] & Direction.North.bit) == 0) {
                    for (int i = 0; i < gridSize; i++) {
                        area.getElevationGrid().setElevation(cellX * gridSize + i, cellY * gridSize, (byte)1);
                    }
                }

                if ((cells[cellX][cellY] & Direction.West.bit) == 0) {
                    for (int i = 0; i < gridSize; i++) {
                        area.getElevationGrid().setElevation(cellX * gridSize, cellY * gridSize + i, (byte)1);
                    }

                    if (cellY * gridSize + gridSize < area.getHeight()) {
                        area.getElevationGrid().setElevation(cellX * gridSize, cellY * gridSize + gridSize, (byte)1);
                    }

                }
            }
        }

        // set south and east map edges to unpassable
        for (int cellX = 0; cellX < area.getWidth(); cellX++) {
            int cellY = area.getHeight() - 1;

            area.getElevationGrid().setElevation(cellX, cellY, (byte)1);
            area.getElevationGrid().setElevation(cellX, cellY - 1, (byte)1);
        }

        for (int cellY = 0; cellY < area.getHeight(); cellY++) {
            int cellX = area.getWidth() - 1;

            area.getElevationGrid().setElevation(cellX, cellY, (byte)1);
            area.getElevationGrid().setElevation(cellX - 1, cellY, (byte)1);
        }

        // set passability and transparency
        for (int x = 0; x < area.getWidth(); x++) {
            for (int y = 0; y < area.getHeight(); y++) {
                if (area.getElevationGrid().getElevation(x, y) != 0) {
                    area.getPassability()[x][y] = false;
                }

                if (area.getElevationGrid().getElevation(x, y) > 0) {
                    area.getTransparency()[x][y] = false;
                }
            }
        }

        // add elevation border tiles
        for (int x = 0; x < area.getWidth(); x++) {
            for (int y = 0; y < area.getHeight(); y++) {
                Point p = new Point(x, y);

                for (ElevationList.Elevation elevation : tileset.getElevationList().
                        getMatchingElevationRules(area.getElevationGrid(), p)) {

                    Border border = elevation.getBorder();

                    for (BorderTile borderTile : border) {
                        Point borderPoint = borderTile.getPosition().getRelativePoint(p);

                        PointImmutable bP = new PointImmutable(borderPoint);
                        if (!bP.isWithinBounds(area)) continue;

                        area.getTileGrid().addTile(borderTile.getID(), borderTile.getLayerID(), bP.x, bP.y);
                    }

                    if (elevation.getImpassable() != null) {
                        Point impass = elevation.getImpassable().getRelativePoint(p);

                        if (area.isValidPoint(impass)) {
                            area.getPassability()[impass.x][impass.y] = false;
                        }

                    }

                }
            }
        }

    }

    private void generateMaze(int[][] cells, int x, int y)
    {
        Direction[] directions = Direction.values();

        // shuffle array
        for (int i = directions.length - 1; i > 0; i--) {
            int index = random.rand(0, i);

            Direction d = directions[index];
            directions[index] = directions[i];
            directions[i] = d;
        }

        for (Direction direction : directions) {
            int newX = x + direction.dx;
            int newY = y + direction.dy;

            if (newX < 0 || newX >= gridWidth || newY < 0 || newY >= gridHeight) {
                continue;
            }

            if (cells[newX][newY] == 0) {
                cells[x][y] |= direction.bit;
                cells[newX][newY] |= direction.opposite.bit;
                generateMaze(cells, newX, newY);
            }
        }
    }

    private enum Direction
    {
        North(1, 0, -1), South(2, 0, 1), East(4, 1, 0), West(8, -1, 0);

        private final int bit;
        private final int dx;
        private final int dy;
        private Direction opposite;

        static {
            North.opposite = South;
            South.opposite = North;
            East.opposite = West;
            West.opposite = East;
        }

        private Direction(int bit, int dx, int dy)
        {
            this.bit = bit;
            this.dx = dx;
            this.dy = dy;
        }
    }

    ;
}
