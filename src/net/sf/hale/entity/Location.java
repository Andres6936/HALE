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

package net.sf.hale.entity;

import java.util.Collection;
import java.util.List;

import net.sf.hale.ability.Effect;
import net.sf.hale.area.Area;
import net.sf.hale.area.Transition;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.Saveable;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;
import net.sf.hale.util.PointImmutable;
import net.sf.hale.util.SimpleJSONObject;

/**
 * The unique location of a given entity.  This includes the area and coordinates,
 * or that the entity is located in an inventory.
 * <p>
 * This class is immutable
 *
 * @author Jared
 */

public class Location implements Saveable
{

    private final Area area;
    private final int x, y;

    /**
     * The unique location representing an entity being in the inventory of another entity,
     * i.e. not directly in an area
     */

    public static final Location Inventory = new Location( );

    @Override
    public JSONOrderedObject save( )
    {
        JSONOrderedObject out = new JSONOrderedObject( );

        out.put( "x", x );
        out.put( "y", y );

        return out;
    }

    /**
     * Loads a location from the specified data.  the location is assumed to be
     * within the specified parent area
     *
     * @param data
     * @param area
     * @return a new location
     */

    public static Location load( SimpleJSONObject data, Area area )
    {
        int x = data.get( "x", 0 );
        int y = data.get( "y", 0 );

        return new Location( area, x, y );
    }

    /**
     * Constructor for the "not in map" or "in inventory" location
     */

    private Location( )
    {
        this.area = null;
        this.x = 0;
        this.y = 0;
    }

    /**
     * Create a location representing a tile within an area
     *
     * @param area the area
     * @param x    the x coordinate
     * @param y    the y coordinate
     */

    public Location( Area area, int x, int y )
    {
        this.area = area;
        this.x = x;
        this.y = y;
    }

    /**
     * Create a location representing a tile within an area
     *
     * @param area  the area
     * @param point the x and y coordinates
     */

    public Location( Area area, Point point )
    {
        this.area = area;
        this.x = point.x;
        this.y = point.y;
    }

    /**
     * Create a location representing a tile within an area
     *
     * @param area  the area
     * @param point the x and y coordinates
     */

    public Location( Area area, PointImmutable point )
    {
        this.area = area;
        this.x = point.x;
        this.y = point.y;
    }

    /**
     * Returns the Area associated with this location or null
     * if this is the Inventory location
     *
     * @return the area associated with this location
     */

    public Area getArea( )
    {
        return area;
    }

    /**
     * Returns the x coordinate of this location
     *
     * @return the x coordinate of this location
     */

    public int getX( )
    {
        return x;
    }

    /**
     * Returns the y coordinate of this location
     *
     * @return the y coordinate of this location
     */

    public int getY( )
    {
        return y;
    }

    /**
     * Returns a point which represents this location in screen coordinates,
     * with the screen coordinates on the upper left corner of the grid point
     *
     * @return a point representing this location's screen coordinates
     */

    public Point getScreenPoint( )
    {
        return AreaUtil.convertGridToScreen( x, y );
    }

    /**
     * Returns a point which represents this location in screen coordinates,
     * with the screen coordinates centered on the grid point
     *
     * @return a point representing this location's screen coordinates
     */

    public Point getCenteredScreenPoint( )
    {
        return AreaUtil.convertGridToScreenAndCenter( x, y );
    }

    /**
     * Returns a mutable Point with the same x and y coordinates as this Location.
     * <p>
     * Note that the Point class does not retain any information about the Area
     *
     * @return a mutable Point with the coordinates of this Location
     */

    public Point toPoint( )
    {
        return new Point( x, y );
    }

    /**
     * Gets the distance, in pixels, from this location's area coordinates to the specified
     * location's area coordinates.  If the other location is not in the same area as this
     * location, returns Integer.MAX_VALUE
     *
     * @param other
     * @return the distance in pixels between the two locations
     */

    public double getScreenDistance( Location other )
    {
        if ( area != other.getArea( ) ) return Integer.MAX_VALUE;

        Point aScreen = AreaUtil.convertGridToScreen( x, y );
        Point bScreen = AreaUtil.convertGridToScreen( other.x, other.y );

        int distSquared = AreaUtil.euclideanDistance2( aScreen.x, aScreen.y, bScreen.x, bScreen.y );
        return Math.sqrt( distSquared );
    }

    /**
     * Gets the distance, in pixels, from this location's coordinates to the specified point,
     * assuming it refers to the same area as this location
     *
     * @param other
     * @return the distance in pixels between this location and the specified point
     */

    public double getScreenDistance( Point other )
    {
        Point aScreen = AreaUtil.convertGridToScreen( x, y );
        Point bScreen = AreaUtil.convertGridToScreen( other.x, other.y );

        int distSquared = AreaUtil.euclideanDistance2( aScreen.x, aScreen.y, bScreen.x, bScreen.y );
        return Math.sqrt( distSquared );
    }

    /**
     * Gets the distance, in pixels, from this location's coordinates to the specified coordinates,
     * assuming they refers to the same area as this location
     *
     * @param x
     * @param y
     * @return the distance in pixels between this location and the specified coordinates
     */

    public double getScreenDistance( int x, int y )
    {
        Point aScreen = AreaUtil.convertGridToScreen( x, y );
        Point bScreen = AreaUtil.convertGridToScreen( x, y );

        int distSquared = AreaUtil.euclideanDistance2( aScreen.x, aScreen.y, bScreen.x, bScreen.y );
        return Math.sqrt( distSquared );
    }

    /**
     * Gets the distance (in hex tiles) between this location and the specified point,
     * assuming that point is in the same area as this location
     *
     * @param point
     * @return the distance
     */

    public int getDistance( Point point )
    {
        return AreaUtil.distance( x, y, point.x, point.y );
    }

    /**
     * Gets the distance (in hex tiles) between this location and the specified point,
     * assuming that point is in the same area as this location
     *
     * @param x the x coodinate
     * @param y the y coordinate
     * @return the distance
     */

    public int getDistance( int x, int y )
    {
        return AreaUtil.distance( this.x, this.y, x, y );
    }

    /**
     * Gets the distance (in hex tiles) between this location and the
     * other location
     *
     * @param location
     * @return the distance between the two locations, or Integer.MAX_VALUE if the two
     * locations are in a different area
     */

    public int getDistance( Location location )
    {
        if ( area != location.getArea( ) ) return Integer.MAX_VALUE;

        return AreaUtil.distance( x, y, location.x, location.y );
    }

    /**
     * Gets the distance (in hex tiles) between this location and the location of
     * the specified entity
     *
     * @param entity
     * @return the distance between this location and the entity's location, or
     * Integer.MAX_VALUE if the two locations are in a different area
     */

    public int getDistance( Entity entity )
    {
        return getDistance( entity.getLocation( ) );
    }

    /**
     * Gets the set of entities that are located at this location
     *
     * @return the set of entities at this location
     */

    public Collection< Entity > getEntities( )
    {
        if ( area == null )
        { throw new UnsupportedOperationException( "getEntities() may only be called on locations within an Area" ); }

        return area.getEntities( ).getEntitiesSet( x, y );
    }

    /**
     * Gets the creature located at this location within the area, or null if there
     * is no creature at this location.  This method may only be used with Locations
     * that have an Area
     *
     * @return the creature located at this location
     */

    public Creature getCreature( )
    {
        if ( area == null )
        { throw new UnsupportedOperationException( "getCreature() may only be called on locations within an Area" ); }

        return area.getCreatureAtGridPoint( x, y );
    }

    /**
     * Gets the trap at this location, or null if there is no trap at this
     * location.  This method may only be used with locations that have an Area
     *
     * @return the trap location at this location
     */

    public Trap getTrap( )
    {
        if ( area == null )
        { throw new UnsupportedOperationException( "getTrap() may only be called on locations within an Area" ); }

        return area.getTrapAtGridPoint( x, y );
    }

    /**
     * Gets the Container at this location, or null if there is no Container at this
     * location.  This method may only be used with locations that have an Area
     *
     * @return the Container location at this location
     */

    public Container getContainer( )
    {
        if ( area == null )
        { throw new UnsupportedOperationException( "getContainer() may only be called on locations within an Area" ); }

        return area.getContainerAtGridPoint( x, y );
    }

    /**
     * Gets the Openable (Door or Container) at this location, or null if there is no Openable at this
     * location.  This method may only be used with locations that have an Area
     *
     * @return the Openable location at this location
     */

    public Openable getOpenable( )
    {
        if ( area == null )
        { throw new UnsupportedOperationException( "getOpenable() may only be called on locations within an Area" ); }

        return area.getOpenableAtGridPoint( x, y );
    }

    /**
     * Gets the Door at this location, or null if there is no Door at this
     * location.  This method may only be used with locations that have an Area
     *
     * @return the Door location at this location
     */

    public Door getDoor( )
    {
        if ( area == null )
        { throw new UnsupportedOperationException( "getDoor() may only be called on locations within an Area" ); }

        return area.getDoorAtGridPoint( x, y );
    }

    /**
     * Gets the AreaTransition at this location, or null if there is no AreaTransition at this
     * location.  This method may only be used with locations that have an Area
     *
     * @return the AreaTransition location at this location
     */

    public Transition getAreaTransition( )
    {
        if ( area == null )
        { throw new UnsupportedOperationException( "getDoor() may only be called on locations within an Area" ); }

        return area.getTransitionAtGridPoint( x, y );
    }

    /**
     * Returns true if and only if this Location has an area and the coordinates of this Location
     * are within the area's bounds (0 to width - 1 for x, 0 to height - 1 for y)
     *
     * @return whether this location is within valid area bounds
     */

    public boolean isInAreaBounds( )
    {
        if ( area == null )
        { return false; }

        return x >= 0 && x < area.getWidth( ) && y >= 0 && y < area.getHeight( );
    }

    /**
     * Gets the elevation for this location, i.e. the elevation of the
     * area at the point specified by this location.  This method can only
     * be called on locations with a specified area
     *
     * @return the elevation for this location
     */

    public byte getElevation( )
    {
        if ( area == null )
        { throw new UnsupportedOperationException( "getElevation() can only be called on locations within an Area" ); }

        return area.getElevationGrid( ).getElevation( x, y );
    }

    /**
     * Gets a list of effects applied to the area at this location
     *
     * @return a list of effects
     */

    public List< Effect > getEffects( )
    {
        if ( area == null )
        { throw new UnsupportedOperationException( "getEffects() may only be called on locations within an Area" ); }

        return area.getEffectsAt( x, y );
    }

    /**
     * Returns true if and only if this Location is in an area and that area is passable at this location.  The refers
     * only to the Area passability, not to any doors or creatures
     *
     * @return whether this location is passable
     */

    public boolean isPassable( )
    {
        if ( area == null )
        { throw new UnsupportedOperationException( "isAreaPassable() can only be called on locations within an Area" ); }

        return area.isPassable( x, y );
    }

    /**
     * Returns true if and only if this Location is in an area and that area is explored at this Location
     *
     * @return whether this location is explored
     */

    public boolean isExplored( )
    {
        if ( area == null )
        { throw new UnsupportedOperationException( "isAreaExplored() can only be called on locations within an Area" ); }

        return area.getExplored( )[ x ][ y ];
    }

    /**
     * Returns true if and only if the parameter is a Location with the same
     * area and coordinates (x and y) as this Location
     *
     * @param other
     */

    @Override
    public boolean equals( Object other )
    {
        if ( ! ( other instanceof Location ) ) return false;

        Location otherLocation = ( Location ) other;

        return otherLocation.area == this.area && otherLocation.x == x && otherLocation.y == y;
    }

    /**
     * Returns a new Location with the same area as this location but
     * the specified x and y coordinates
     *
     * @param x
     * @param y
     * @return a new location with the specified coordinates
     */

    public Location getInSameArea( int x, int y )
    {
        return new Location( area, x, y );
    }

    @Override
    public String toString( )
    {
        if ( area != null )
        {
            return "(" + x + ", " + y + " in " + area.getName( ) + ")";
        }
        else
        {
            return "(" + x + ", " + y + ")";
        }
    }
}
