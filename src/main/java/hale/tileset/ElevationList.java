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
import java.util.List;

import main.java.hale.util.Point;
import main.java.hale.util.SimpleJSONArray;
import main.java.hale.util.SimpleJSONArrayEntry;
import main.java.hale.util.SimpleJSONObject;

/**
 * A set of rules and tiles representing a change in elevation
 *
 * @author Jared
 */

public class ElevationList
{

    private List<Elevation> elevation;

    private ElevationList()
    {
    }

    /**
     * Creates a new ElevationList by parsing the specified JSON data
     *
     * @param data the JSON data to parse
     * @return the new ElevationList
     */

    public static ElevationList parse(SimpleJSONArray data)
    {
        ElevationList elevationList = new ElevationList();

        ArrayList<Elevation> elevation = new ArrayList<Elevation>();
        for (SimpleJSONArrayEntry entry : data) {
            SimpleJSONObject elevationObject = entry.getObject();

            elevation.add(new Elevation(elevationObject));
        }

        elevation.trimToSize();
        elevationList.elevation = Collections.unmodifiableList(elevation);

        return elevationList;
    }

    /**
     * Returns the list of all elevation rules.  This list is unmodifiable
     *
     * @return the list of all elevation rules
     */

    public List<Elevation> getElevationRules()
    {
        return elevation;
    }

    public List<Elevation> getMatchingElevationRules(AreaElevationGrid grid, Point center)
    {
        List<Elevation> elevation = new ArrayList<Elevation>();

        for (Elevation elev : this.elevation) {
            if (elev.isMetAtPoint(grid, center)) {
                elevation.add(elev);
            }
        }

        return elevation;
    }

    /**
     * A single elevation rule with associated impassability and opaque rules
     *
     * @author Jared
     */

    public static class Elevation
    {
        private final Border border;
        private final DirectionList impassable;

        /**
         * Returns the direction to the impassable tile for this rule,
         * or null if no tile is to be made impassable
         *
         * @return the direction to the impassable tile
         */

        public DirectionList getImpassable()
        {
            return impassable;
        }

        /**
         * Returns the border rules object associated with this Elevation object
         *
         * @return the border rules object associated with this Elevation object
         */

        public Border getBorder()
        {
            return border;
        }

        /**
         * Creates a new Elevation object by parsing the specified JSON data
         *
         * @param data   the JSON data to parse
         * @param parent the parent BorderList containing this border
         */

        private Elevation(SimpleJSONObject data)
        {
            border = Border.parse(data, "elevation");

            if (data.containsKey("impassable")) {
                impassable = DirectionList.parse(data.get("impassable", null));
            } else {
                impassable = null;
            }
        }

        private boolean isMetAtPoint(AreaElevationGrid grid, Point center)
        {
            byte centerElev = grid.getElevation(center.x, center.y);

            int width = grid.getWidth();
            int height = grid.getHeight();

            for (DirectionList list : border.getMustHave()) {
                Point p = list.getRelativePoint(center);
                if (p.x < 0 || p.x >= width || p.y < 0 || p.y >= height) {
                    // outside the grid, so the condition is not met
                    return false;
                }

                // if the elevation is greater than the center elevation, the condition is
                // met
                if (centerElev >= grid.getElevation(p.x, p.y)) return false;
            }

            for (DirectionList list : border.getMustNotHave()) {
                Point p = list.getRelativePoint(center);
                if (p.x < 0 || p.x >= width || p.y < 0 || p.y >= height) {
                    // outside the terrain, the condition is not met
                    return false;
                }

                // if the elevation is equal to the center elevation, the condition
                // is met

                if (centerElev != grid.getElevation(p.x, p.y)) return false;
            }

            return true;
        }
    }
}
