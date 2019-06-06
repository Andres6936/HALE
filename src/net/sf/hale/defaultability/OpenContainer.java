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
import net.sf.hale.entity.Container;
import net.sf.hale.entity.Location;
import net.sf.hale.entity.PC;

/**
 * A default ability for opening a container.  Can also move towards a container
 * and then open it, as needed.
 *
 * @author Jared Stephen
 */

public class OpenContainer implements DefaultAbility
{
    // storage for movement properties if movement is needed
    private Move move;
    private Container container;

    @Override
    public String getActionName( )
    {
        return "Open Container";
    }

    @Override
    public boolean canActivate( PC parent, Location targetPosition )
    {
        if ( ! parent.timer.canPerformAction( "OpenContainerCost" ) ) return false;

        container = targetPosition.getContainer( );

        if ( container != null )
        {
            move = new Move( );
            move.setAllowPartyMove( false );

            if ( targetPosition.getDistance( parent ) > 1 )
            {
                // need to move towards the container before opening
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
            // move towards the container then open
            move.addCallback( new OpenContainerCallback( parent ) );
            move.moveTowards( parent, targetPosition, 1 );
        }
        else
        {
            openContainer( parent, targetPosition.getContainer( ) );
        }

        Game.areaListener.computeMouseState( );
    }

    @Override
    public DefaultAbility getInstance( )
    {
        return new OpenContainer( );
    }

    /**
     * The specified Creature will attempt to open the specified container
     * <p>
     * The creature's AP is decreased by the "openContainerCost"
     * <p>
     * There are many reasons why a creature might fail to open the object, including
     * not having enough AP, a lock on the object, or not being adjacent or on top of the container.
     *
     * @param parent    the PC that will attempt to open the container
     * @param container the container to be opened
     * @return true if the Container was successfully opened, false otherwise
     */

    public boolean openContainer( PC parent, Container container )
    {
        if ( parent.getLocation( ).getDistance( container ) > 1 ) return false;

        if ( container != null && parent.timer.canPerformAction( "OpenContainerCost" ) )
        {
            parent.timer.performAction( "OpenContainerCost" );

            container.attemptOpen( parent );

            // if the container was locked, it may not have actually opened
            // so check before showing the contents
            if ( container.isOpen( ) )
            {
                if ( container.getTemplate( ).isWorkbench( ) )
                {
                    Game.mainViewer.craftingWindow.setVisible( true );
                }
                else
                {
                    Game.mainViewer.containerWindow.setOpenerContainer( parent, container );
                    Game.mainViewer.containerWindow.setVisible( true );
                    Game.mainViewer.updateInterface( );
                }

                return true;
            }
            else
            {
                Game.mainViewer.addFadeAway( "Locked", container.getLocation( ).getX( ), container.getLocation( ).getY( ),
                                             new Color( 0xFFAbA9A9 ) );
            }

        }

        return false;
    }


    /*
     * A callback that is used to open the container after finishing
     * moving towards the container
     */

    private class OpenContainerCallback implements Runnable
    {
        private PC parent;

        private OpenContainerCallback( PC parent )
        {
            this.parent = parent;
        }

        @Override
        public void run( )
        {
            openContainer( parent, container );
        }
    }
}
