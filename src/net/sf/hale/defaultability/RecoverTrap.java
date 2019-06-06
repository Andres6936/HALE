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
import net.sf.hale.bonus.Bonus;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Location;
import net.sf.hale.entity.PC;
import net.sf.hale.entity.Trap;

/**
 * A default ability for recovering a trap from the area.  If the parent is not adjacent to
 * the trap, the parent is moved.  This default ability can only be used by parents with
 * Bonus.Type.TrapHandling.
 *
 * @author Jared Stephen
 */

public class RecoverTrap implements DefaultAbility
{
    private Move move;
    private Trap trap;

    @Override
    public String getActionName( )
    {
        return "Recover Trap";
    }

    @Override
    public boolean canActivate( PC parent, Location targetPosition )
    {
        if ( ! parent.timer.canPerformAction( "RecoverTrapCost" ) ) return false;

        if ( ! parent.stats.has( Bonus.Type.TrapHandling ) ) return false;

        trap = targetPosition.getTrap( );
        if ( trap == null || ! trap.isSpotted( ) ) return false;

        move = new Move( );
        move.setAllowPartyMove( false );

        if ( targetPosition.getDistance( parent ) > 1 )
        {
            // need to move towards the door before opening
            return move.canMove( parent, targetPosition, 1 );
        }

        return true;
    }

    @Override
    public void activate( PC parent, Location targetPosition )
    {
        if ( targetPosition.getDistance( parent ) > 1 )
        {
            // move towards the door then open
            move.addCallback( new RecoverCallback( parent ) );
            move.moveTowards( parent, targetPosition, 1 );
        }
        else
        {
            recover( parent, trap );
        }

        Game.areaListener.computeMouseState( );
    }

    /**
     * The specified Creature will attempt to recover the trap.
     * <p>
     * The Creature's AP is decreased by "recoverTrapCost".
     *
     * @param parent the Creature trying to recover the trap
     * @param trap   the trap to be recovered
     * @return true if the trap was recovered, false if it was not
     * for any reason
     */

    public boolean recover( Creature parent, Trap trap )
    {
        if ( parent.getLocation( ).getDistance( trap ) > 1 ) return false;

        if ( trap == null || ! parent.timer.canPerformAction( "RecoverTrapCost" ) ) return false;

        if ( ! parent.stats.has( Bonus.Type.TrapHandling ) ) return false;

        parent.timer.performAction( "RecoverTrapCost" );

        boolean isRecovered = trap.attemptRecover( parent );

        if ( ! isRecovered )
        {
            Game.mainViewer.addFadeAway( "Recover Trap Failed", trap.getLocation( ).getX( ), trap.getLocation( ).getY( ),
                                         new Color( 0xFFAbA9A9 ) );
        }
        else
        {
            Game.mainViewer.addFadeAway( "Trap Recovered", trap.getLocation( ).getX( ), trap.getLocation( ).getY( ),
                                         new Color( 0xFFAbA9A9 ) );
        }

        return isRecovered;
    }

    @Override
    public DefaultAbility getInstance( )
    {
        return new RecoverTrap( );
    }

    private class RecoverCallback implements Runnable
    {
        private Creature parent;

        private RecoverCallback( Creature parent )
        {
            this.parent = parent;
        }

        @Override
        public void run( )
        {
            recover( parent, trap );
        }
    }

}
