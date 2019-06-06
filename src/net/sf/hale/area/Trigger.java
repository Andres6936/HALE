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

package net.sf.hale.area;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.ability.ScriptFunctionType;
import net.sf.hale.ability.Scriptable;
import net.sf.hale.entity.Entity;
import net.sf.hale.entity.Location;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.Saveable;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.util.PointImmutable;
import net.sf.hale.util.SimpleJSONArray;
import net.sf.hale.util.SimpleJSONArrayEntry;
import net.sf.hale.util.SimpleJSONObject;

/**
 * A trigger consists of a script and usually a set of points within an area.  Different
 * conditions being met will cause different functions within the script to be run
 *
 * @author Jared
 */

public class Trigger implements Saveable
{
    private final String id;
    private final Scriptable script;

    private final List< Entity > entitiesCurrentlyInside;
    private boolean enteredByPlayer;
    private boolean areaLoaded;

    private final List< PointImmutable > points;
    private final int pointsOffsetX, pointsOffsetY;
    private final boolean[][] pointsArray;

    @Override
    public Object save( )
    {
        // if we would only save the ID, return null to indicate this trigger doesn't need
        // to be saved
        if ( ! areaLoaded && ! enteredByPlayer )
        { return null; }

        JSONOrderedObject data = new JSONOrderedObject( );

        data.put( "id", id );

        if ( areaLoaded ) data.put( "areaLoaded", areaLoaded );

        if ( enteredByPlayer ) data.put( "enteredByPlayer", enteredByPlayer );

        return data;
    }

    /**
     * Sets the mutable state of this trigger based on the specified JSON data
     *
     * @param data
     */

    public void load( SimpleJSONObject data )
    {
        if ( data.containsKey( "areaLoaded" ) )
        { areaLoaded = data.get( "areaLoaded", false ); }
        else
        { areaLoaded = false; }

        if ( data.containsKey( "enteredByPlayer" ) )
        { enteredByPlayer = data.get( "enteredByPlayer", false ); }
        else
        { enteredByPlayer = false; }
    }

    /**
     * Creates a new Trigger with the specified ID.  The trigger's
     * properties are defined within the JSON data
     *
     * @param id
     * @param data
     */

    public Trigger( String id, SimpleJSONObject data )
    {
        this.id = id;

        String scriptFile = data.get( "script", null );
        String scriptContents = ResourceManager.getScriptResourceAsString( scriptFile );
        this.script = new Scriptable( scriptContents, scriptFile, false );

        int smallestX = Integer.MAX_VALUE, smallestY = Integer.MAX_VALUE;
        int largestX = 0, largestY = 0;

        points = new ArrayList< PointImmutable >( );

        if ( data.containsKey( "points" ) )
        {
            SimpleJSONArray pointsIn = data.getArray( "points" );
            for ( SimpleJSONArrayEntry entry : pointsIn )
            {
                SimpleJSONArray pointIn = entry.getArray( );
                Iterator< SimpleJSONArrayEntry > iter = pointIn.iterator( );

                int x = iter.next( ).getInt( 0 );
                int y = iter.next( ).getInt( 0 );

                // keep track of the bounds of all points
                if ( x < smallestX ) smallestX = x;
                if ( x > largestX ) largestX = x;
                if ( y < smallestY ) smallestY = y;
                if ( y > largestY ) largestY = y;

                points.add( new PointImmutable( x, y ) );
            }

            // create the offset & array combination to store the points efficiently
            this.pointsOffsetX = smallestX;
            this.pointsOffsetY = smallestY;
            this.pointsArray = new boolean[ largestX - smallestX + 1 ][ largestY - smallestY + 1 ];
            for ( PointImmutable point : points )
            {
                pointsArray[ point.x - pointsOffsetX ][ point.y - pointsOffsetY ] = true;
            }

        }
        else
        {
            this.pointsOffsetX = Integer.MAX_VALUE;
            this.pointsOffsetY = Integer.MAX_VALUE;
            this.pointsArray = null;
        }

        this.entitiesCurrentlyInside = new ArrayList< Entity >( );
    }

    /**
     * Returns a list of all points in this trigger.  this list is unmodifiable
     *
     * @return a list of all points
     */

    public List< PointImmutable > getPoints( )
    {
        return Collections.unmodifiableList( points );
    }

    /**
     * Returns the script object for this trigger
     *
     * @return the script object
     */

    public Scriptable getScript( )
    {
        return script;
    }

    /**
     * Returns the unique (within the parent area) ID of this trigger
     *
     * @return the unique ID
     */

    public String getID( )
    {
        return id;
    }

    /**
     * Calls the script functions {@link ScriptFunctionType#onAreaLoad}, and
     * {@link ScriptFunctionType#onAreaLoadFirstTime} if that function has not yet been called
     * for this trigger's script
     *
     * @param transition
     */

    public void checkOnAreaLoad( Transition transition )
    {
        if ( ! areaLoaded )
        {
            script.executeFunction( ScriptFunctionType.onAreaLoadFirstTime, Game.curCampaign.curArea, transition );
            areaLoaded = true;
        }

        script.executeFunction( ScriptFunctionType.onAreaLoad, Game.curCampaign.curArea, transition );
    }

    /**
     * Calls the script function {@link ScriptFunctionType#onAreaExit} for this trigger's script
     *
     * @param transition
     */

    public void checkOnAreaExit( Transition transition )
    {
        script.executeFunction( ScriptFunctionType.onAreaExit, Game.curCampaign.curArea, transition );
    }

    /**
     * Calls {@link ScriptFunctionType#onPlayerEnter}, {@link ScriptFunctionType#onPlayerEnterFirstTime},
     * and {@link ScriptFunctionType#onPlayerExit}, depending on whether the entity has entered this
     * trigger, entered it for the first time, or has exited it, respectively
     *
     * @param entity
     */

    public void checkPlayerMoved( Entity entity )
    {
        if ( ! enteredByPlayer )
        {
            checkPlayerEnterFirstTime( entity );
        }

        checkPlayerEnter( entity );
        checkPlayerExit( entity );
    }

    /**
     * Returns true if this trigger contains a point with coordinates matching the specified
     * location, false otherwise.  Note that this method does not check the area of the
     * specified location
     *
     * @param location
     * @return whether or not this trigger contains a point with the specified coordinates.
     */

    public boolean containsPoint( Location location )
    {
        int x = location.getX( );
        int y = location.getY( );

        if ( x < pointsOffsetX ) return false;
        if ( y < pointsOffsetY ) return false;

        if ( x >= pointsOffsetX + pointsArray.length ) return false;
        if ( y >= pointsOffsetY + pointsArray[ 0 ].length ) return false;

        return pointsArray[ x - pointsOffsetX ][ y - pointsOffsetY ];
    }

    private void checkPlayerEnterFirstTime( Entity entity )
    {
        if ( ! containsPoint( entity.getLocation( ) ) ) return;

        script.executeFunction( ScriptFunctionType.onPlayerEnterFirstTime, entity, this );
        enteredByPlayer = true;
    }

    private void checkPlayerEnter( Entity entity )
    {
        if ( entitiesCurrentlyInside.contains( entity ) ) return;
        if ( ! this.containsPoint( entity.getLocation( ) ) ) return;

        entitiesCurrentlyInside.add( entity );
        script.executeFunction( ScriptFunctionType.onPlayerEnter, entity, this );
    }

    private void checkPlayerExit( Entity entity )
    {
        if ( ! entitiesCurrentlyInside.contains( entity ) ) return;
        if ( this.containsPoint( entity.getLocation( ) ) ) return;

        entitiesCurrentlyInside.remove( entity );
        script.executeFunction( ScriptFunctionType.onPlayerExit, entity, this );
    }
}
