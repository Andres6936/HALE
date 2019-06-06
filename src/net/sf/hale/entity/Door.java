/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2012 Jared Stephen
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

import net.sf.hale.Game;
import net.sf.hale.area.Area;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.LoadGameException;
import net.sf.hale.loading.ReferenceHandler;
import net.sf.hale.util.SimpleJSONObject;

/**
 * A door is an entity that opens and closes, allowing or disallowing passage
 * by creatures
 *
 * @author Jared
 */

public class Door extends Openable
{

    private final DoorTemplate template;

    @Override
    public void load( SimpleJSONObject data, Area area, ReferenceHandler refHandler ) throws LoadGameException
    {
        super.load( data, area, refHandler );
    }

    @Override
    public JSONOrderedObject save( )
    {
        JSONOrderedObject out = super.save( );
        return out;
    }

    /**
     * Creates a new Door
     *
     * @param template
     */

    protected Door( DoorTemplate template )
    {
        super( template );

        this.template = template;
    }

    /**
     * Returns whether creatures can see through this door.  This is true for open doors,
     * and false for closed doors unless the template's {@link DoorTemplate#isTransparent()}
     * is true
     *
     * @return whether creatures can currently see through this door
     */

    public boolean isTransparent( )
    {
        return isOpen( ) || template.isTransparent( );
    }

    @Override
    public DoorTemplate getTemplate( )
    {
        return template;
    }

    @Override
    public boolean attemptOpen( Creature opener )
    {
        boolean isOpen = super.attemptOpen( opener );

        getLocation( ).getArea( ).getTransparency( )[ getLocation( ).getX( ) ][ getLocation( ).getY( ) ] = isOpen( );
        opener.getLocation( ).getArea( ).getUtil( ).updateVisibility( );

        if ( isOpen )
        {
            Game.areaListener.getCombatRunner( ).checkAIActivation( );
        }

        return isOpen;
    }

    @Override
    public void close( Creature closer )
    {
        super.close( closer );

        getLocation( ).getArea( ).getTransparency( )[ getLocation( ).getX( ) ][ getLocation( ).getY( ) ] = isOpen( );
        closer.getLocation( ).getArea( ).getUtil( ).updateVisibility( );

    }

    @Override
    public int compareTo( Entity other )
    {
        if ( other instanceof Creature ) return - 1;
        if ( other instanceof Container ) return 1;

        return super.compareTo( other );
    }
}
