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

package net.sf.hale.defaultability;

import de.matthiasmann.twl.Color;
import net.sf.hale.Game;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Door;
import net.sf.hale.entity.Location;
import net.sf.hale.entity.PC;

/**
 * A DefaultAbility for opening a door.  Can also move to the door and
 * then open, if needed.
 *
 * @author Jared Stephen
 */

public class OpenDoor implements DefaultAbility
{
    private static final String doorOpenAction = "Open Door";
    private static final String doorCloseAction = "Close Door";

    // storage for movement properties as needed
    private Move move;
    private Door door;

    private String actionName;

    @Override
    public String getActionName( )
    {
        return actionName;
    }

    @Override
    public boolean canActivate( PC parent, Location targetPosition )
    {
        if ( ! parent.timer.canPerformAction( "OpenDoorCost" ) ) return false;

        door = targetPosition.getDoor( );

        if ( door != null )
        {
            if ( door.isOpen( ) ) { actionName = OpenDoor.doorCloseAction; }
            else { actionName = OpenDoor.doorOpenAction; }

            move = new Move( );

            if ( targetPosition.getDistance( parent ) > 1 )
            {
                // need to move towards the door before opening
                return move.canMove( parent, targetPosition, 1 );
            }

            return true;
        }

        return false;
    }

    @Override
    public void activate( PC parent, Location targetPosition )
    {
        if ( targetPosition.getDistance( parent ) > 1 )
        {
            // move towards the door then open
            move.addCallback( new OpenDoorCallback( parent ) );
            move.moveTowards( parent, targetPosition, 1 );
        }
        else
        {
            toggleDoor( parent, targetPosition.getDoor( ) );
        }

        Game.areaListener.computeMouseState( );
    }

    /**
     * The specified Creature will attempt to open the specified door
     * <p>
     * The creature's AP is decreased by the "openDoorCost"
     * <p>
     * There are many reasons why a creature might fail to open the object, including
     * not having enough AP, a lock on the object, or not being adjacent or on top of the container.
     *
     * @param parent the Creature that will attempt to open the container
     * @param door   the door to be opened
     * @return true if the Door was successfully opened, false otherwise
     */

    public boolean toggleDoor( Creature parent, Door door )
    {
        if ( parent.getLocation( ).getDistance( door ) > 1 ) return false;

        if ( door == null || ! parent.timer.canPerformAction( "OpenDoorCost" ) ) return false;

        parent.timer.performAction( "OpenDoorCost" );

        if ( door.isOpen( ) )
        {
            Creature creature = door.getLocation( ).getCreature( );
            if ( creature == null )
            {
                door.close( parent );
            }
            else
            {
                Game.mainViewer.addMessage( "red", creature.getTemplate( ).getName( ) + " blocks the door." );
            }

            return ! door.isOpen( );
        }
        else
        {
            door.attemptOpen( parent );

            if ( ! door.isOpen( ) )
            {
                Game.mainViewer.addFadeAway( "Locked", door.getLocation( ).getX( ), door.getLocation( ).getY( ),
                                             new Color( 0xFFAbA9A9 ) );
            }

            return door.isOpen( );
        }
    }

    @Override
    public DefaultAbility getInstance( )
    {
        return new OpenDoor( );
    }

    /*
     * A callback that is used to open the door after finishing
     * moving towards the door
     */

    private class OpenDoorCallback implements Runnable
    {
        private Creature parent;

        private OpenDoorCallback( Creature parent )
        {
            this.parent = parent;
        }

        @Override
        public void run( )
        {
            toggleDoor( parent, door );
        }
    }
}
