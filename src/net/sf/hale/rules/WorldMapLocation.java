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

package net.sf.hale.rules;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sf.hale.icon.Icon;
import net.sf.hale.icon.IconFactory;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.Saveable;
import net.sf.hale.util.SimpleJSONObject;

/**
 * A class representing a location on the world map that the party can travel to.
 * <p>
 * Locations will generally have one or more areas associated with them.
 *
 * @author Jared
 */

public class WorldMapLocation implements Saveable
{
    private final Map< String, Integer > travelTimesInHours;

    // the coordinates that this location is drawn on the world map
    private final int iconX, iconY;

    private final String name;
    private final Icon icon;
    private final String startingTransition;

    private boolean revealed;

    @Override
    public Object save( )
    {
        JSONOrderedObject data = new JSONOrderedObject( );

        data.put( "name", name );
        data.put( "revealed", revealed );

        return data;
    }

    /**
     * Creates a world map location by parsing the specified JSON
     *
     * @param data
     */

    public WorldMapLocation( SimpleJSONObject data )
    {
        this.name = data.get( "id", null );

        if ( data.containsKey( "startingTransition" ) )
        {
            this.startingTransition = data.get( "startingTransition", null );
        }
        else
        {
            this.startingTransition = null;
        }

        if ( data.containsKey( "icon" ) )
        {
            this.icon = IconFactory.createIcon( data.getObject( "icon" ) );
        }
        else
        {
            this.icon = IconFactory.emptyIcon;
        }

        if ( data.containsKey( "iconPosition" ) )
        {
            SimpleJSONObject iconPositionIn = data.getObject( "iconPosition" );
            this.iconX = iconPositionIn.get( "x", 0 );
            this.iconY = iconPositionIn.get( "y", 0 );
        }
        else
        {
            this.iconX = 0;
            this.iconY = 0;
        }

        if ( data.containsKey( "travelTimes" ) )
        {
            this.travelTimesInHours = new HashMap< String, Integer >( );
            SimpleJSONObject travelIn = data.getObject( "travelTimes" );
            for ( String destLocation : travelIn.keySet( ) )
            {
                this.travelTimesInHours.put( destLocation, travelIn.get( destLocation, 0 ) );
            }
        }
        else
        {
            this.travelTimesInHours = Collections.emptyMap( );
        }

        this.revealed = false;
    }

    /**
     * Sets whether this location is shown to the player when they are viewing the world map
     *
     * @param revealed whether the location will be shown to the player
     */

    public void setRevealed( boolean revealed ) { this.revealed = revealed; }

    /**
     * Returns true if the player knows of this location and can see it on the world map,
     * false otherwise
     *
     * @return whether the player can see this location
     */

    public boolean isRevealed( ) { return revealed; }

    /**
     * Returns the x coordinate that this location's icon should be drawn on the
     * world map
     *
     * @return the x coordinate of the icon
     */

    public int getIconPositionX( ) { return iconX; }

    /**
     * Returns the y coordinate that this location's icon should be drawn on the world map
     *
     * @return the y coordinate of the icon
     */

    public int getIconPositionY( ) { return iconY; }

    /**
     * Returns the name of this location
     *
     * @return the name
     */

    public String getName( ) { return name; }

    /**
     * Returns the icon that is drawn on the world map for this location.  Note that this
     * can be the empty icon
     *
     * @return the icon
     */

    public Icon getIcon( ) { return icon; }

    /**
     * Returns the starting area transition for this location.  This is the transition that
     * the player will be sent to when traveling to this location
     *
     * @return the starting area for this location
     */

    public String getStartingTransition( ) { return startingTransition; }

    /**
     * Returns the amount of time it takes the player's party (in hours) to travel from
     * this location to the specified location
     *
     * @param location
     * @return the travel time in hours
     */

    public int getTravelTime( WorldMapLocation location )
    {
        return travelTimesInHours.get( location.getName( ) );
    }
}
