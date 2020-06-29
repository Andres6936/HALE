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

package hale.area;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import hale.Game;
import hale.icon.Icon;
import hale.icon.IconFactory;
import hale.loading.JSONOrderedObject;
import hale.loading.Saveable;
import hale.resource.ResourceType;
import hale.util.PointImmutable;
import hale.util.SimpleJSONArray;
import hale.util.SimpleJSONArrayEntry;
import hale.util.SimpleJSONObject;
import hale.util.SimpleJSONParser;

/**
 * A transition from one area to another, or from an area to the world map.  Allows the player's
 * party to move between areas.
 *
 * @author Jared
 */

public class Transition implements Saveable
{
    private final String id;
    private final Icon icon;
    private final boolean isTwoWay;
    private final boolean isInitiallyActivated;
    private final String worldMapLocation;
    private final EndPoint from;
    private final EndPoint to;

    private boolean isActivated;

    @Override
    public Object save()
    {
        // if there is nothing to save, return null to indicate this transition
        // does not need to be saved
        if (isInitiallyActivated == isActivated) {
            return null;
        }

        JSONOrderedObject data = new JSONOrderedObject();

        data.put("name", id);
        data.put("activated", isActivated);

        return data;
    }

    public void load(SimpleJSONObject data)
    {
        this.isActivated = data.get("activated", false);
    }

    /**
     * Creates a new AreaTransition with the specified ID by parsing the JSON at the
     * appropriate resource location
     *
     * @param id
     */

    public Transition(String id)
    {
        this.id = id;

        SimpleJSONParser parser = new SimpleJSONParser("transitions/" + id + ResourceType.JSON.getExtension());

        isInitiallyActivated = parser.get("isInitiallyActivated", true);
        isTwoWay = parser.get("isTwoWay", true);
        worldMapLocation = parser.get("worldMapLocation", null);

        if (parser.containsKey("icon")) {
            icon = IconFactory.createIcon(parser.getObject("icon"));
        } else {
            icon = IconFactory.emptyIcon;
        }

        from = parseEndPoint(parser.getObject("from"));
        to = parseEndPoint(parser.getObject("to"));

        parser.warnOnUnusedKeys();

        isActivated = isInitiallyActivated;
    }

    /**
     * Gets the EndPoint (origin or destination) for creatures coming
     * from the world map.  If this transition has a world map end point,
     * this will be the other end point.  Otherwise, this will be null.
     *
     * @return the EndPoint for creatures coming from the world map.
     */

    public EndPoint getEndPointForWorldMap()
    {
        if (from.isWorldMap()) {
            return to;
        } else
            if (to.isWorldMap()) {
                return from;
            } else {
                return null;
            }
    }

    /**
     * Gets the EndPoint (origin or destination) for this transition
     * corresponding to the location that creatures in the current campaign
     * area (Game.curCampaign.curArea) will be sent to.  Or, returns null
     * if the current campaign area doesn't correspond to either end point
     * for this transition
     *
     * @return the EndPoint
     */

    public EndPoint getEndPointForCreaturesInCurrentArea()
    {
        if (Game.curCampaign.curArea.getID().equals(to.getAreaID())) {
            return from;
        } else
            if (Game.curCampaign.curArea.getID().equals(from.getAreaID())) {
                return to;
            } else {
                return null;
            }
    }

    /**
     * Returns the end point of this transition located in the specified area, or
     * null if neither end point is located in the specified area
     *
     * @param area
     * @return the end point located in the area
     */

    public EndPoint getEndPointInArea(Area area)
    {
        if (area.getID().equals(to.getAreaID())) {
            return to;
        } else
            if (area.getID().equals(from.getAreaID())) {
                return from;
            } else {
                return null;
            }
    }

    /**
     * Returns true if this transition's "from" end point is located in the specified area,
     * false otherwise
     *
     * @param area
     * @return whether this transition's "from" end point is located in the specified area
     */

    public boolean isFromArea(Area area)
    {
        return area.getID().equals(from.getAreaID());
    }

    /**
     * Returns true if this transition's "to" end point is located in the specified area,
     * false otherwise
     *
     * @param area
     * @return whether this transition's "to" end point is located in the specified area
     */

    public boolean isToArea(Area area)
    {
        return area.getID().equals(to.getAreaID());
    }

    /**
     * Returns the world map location associated with this transition
     *
     * @return the world map location
     */

    public String getWorldMapLocation()
    {
        return worldMapLocation;
    }

    /**
     * Returns whether this transition is activated.  Transitions that are not
     * activated are invisible to the player and cannot be used
     *
     * @return whether this transition is activated
     */

    public boolean isActivated()
    {
        return isActivated;
    }

    /**
     * Sets this transition as activated.  This transition will now be visible
     * to the player and usable.
     */

    public void activate()
    {
        this.isActivated = true;
    }

    /**
     * Returns true if this is a two way transition (can go both directions), false
     * if it can only be used in one direction
     *
     * @return whether this is a two way transition
     */

    public boolean isTwoWay()
    {
        return isTwoWay;
    }

    /**
     * Returns the ID of this Transition
     *
     * @return the ID
     */

    public String getID()
    {
        return id;
    }

    /**
     * Returns the icon for this transition.  This may be the empty icon.
     *
     * @return the icon for this transition
     */

    public Icon getIcon()
    {
        return icon;
    }

    /**
     * Gets either a world map end point or an area end point
     * depending on the data
     *
     * @param data
     * @return a newly created end point
     */

    private EndPoint parseEndPoint(SimpleJSONObject data)
    {
        boolean isWorldMap;
        if (data.containsKey("worldMap")) {
            isWorldMap = data.get("worldMap", false);
        } else {
            isWorldMap = false;
        }

        if (isWorldMap) {
            return new WorldMapEndPoint();
        } else {
            String areaID = data.get("area", null);
            int centerX = data.get("x", 0);
            int centerY = data.get("y", 0);

            String label;
            if (data.containsKey("label")) {
                label = data.get("label", null);
            } else {
                label = areaID;
            }

            List<PointImmutable> partyPositions = new ArrayList<PointImmutable>();

            SimpleJSONArray partyPositionsIn = data.getArray("partyPositions");
            for (SimpleJSONArrayEntry entry : partyPositionsIn) {
                SimpleJSONArray positionIn = entry.getArray();

                Iterator<SimpleJSONArrayEntry> iter = positionIn.iterator();

                int relativeX = iter.next().getInt(0);
                int relativeY = iter.next().getInt(0);

                // convert the relative coordinates to absolute area coordinates
                int partyX = relativeX + centerX;
                int partyY = relativeY + centerY;
                if (relativeX % 2 != 0 && centerX % 2 != 0) {
                    partyY++;
                }

                PointImmutable point = new PointImmutable(partyX, partyY);
                partyPositions.add(point);
            }
            ((ArrayList<PointImmutable>)partyPositions).trimToSize();

            return new AreaEndPoint(areaID, centerX, centerY, partyPositions, label);
        }
    }

    /**
     * A destination or origin for this transition
     *
     * @author Jared
     */

    public interface EndPoint
    {
        /**
         * Returns true if this is a world map end point, false otherwise.
         * <p>
         * Note that for world map end points, all other methods in this
         * interface are non-applicable
         *
         * @return whether this is a world map end point
         */

        public boolean isWorldMap();

        /**
         * Returns the text that should be shown when the user mouses over
         * this transition end point
         *
         * @return the label text
         */

        public String getLabel();

        /**
         * Returns the id of the area that this end Point is located in,
         * or null if this is a world map end point
         *
         * @return the area ID
         */

        public String getAreaID();

        /**
         * Returns the x coordinate of this endPoint within the area, or -1 if
         * this is a world map end point
         *
         * @return the x coordinate
         */

        public int getX();

        /**
         * Returns the y coordinate of this endPoint within the area, or -1 if
         * this is a world map end point
         *
         * @return the y coordinate
         */

        public int getY();

        /**
         * Gets an iterator over all party positions in this EndPoint.
         *
         * @return an iterator over all party positions in this EndPoint
         * @throws UnsupportedOperationException if this is a world map EndPoint
         */

        public Iterator<PointImmutable> getPartyPositionsIterator();
    }

    private class WorldMapEndPoint implements EndPoint
    {
        @Override
        public boolean isWorldMap()
        {
            return true;
        }

        @Override
        public String getLabel()
        {
            return "World Map";
        }

        @Override
        public String getAreaID()
        {
            return null;
        }

        @Override
        public int getX()
        {
            return -1;
        }

        @Override
        public int getY()
        {
            return -1;
        }

        @Override
        public Iterator<PointImmutable> getPartyPositionsIterator()
        {
            throw new UnsupportedOperationException("Cannot iterate over party positions in a world map end point");
        }
    }

    private class AreaEndPoint implements EndPoint
    {
        private final String label;
        private final String areaID;
        private final int x, y;
        private final List<PointImmutable> partyPositions;

        private AreaEndPoint(String areaID, int x, int y, List<PointImmutable> partyPositions, String label)
        {
            this.areaID = areaID;
            this.x = x;
            this.y = y;
            this.partyPositions = Collections.unmodifiableList(partyPositions);
            this.label = label;
        }

        @Override
        public boolean isWorldMap()
        {
            return false;
        }

        @Override
        public String getLabel()
        {
            return label;
        }

        @Override
        public String getAreaID()
        {
            return areaID;
        }

        @Override
        public int getX()
        {
            return x;
        }

        @Override
        public int getY()
        {
            return y;
        }

        @Override
        public Iterator<PointImmutable> getPartyPositionsIterator()
        {
            return partyPositions.iterator();
        }
    }
}
