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

package net.sf.hale.interfacelock;

import net.sf.hale.Game;
import net.sf.hale.entity.Creature;

/**
 * An interface lock used to advance combat.  After each AI creature's combat turn, the
 * interface is locked for a short time and then the locker finishes, running the next turn
 *
 * @author Jared Stephen
 */

public class InterfaceCombatLock extends InterfaceLock
{
    /**
     * Create a new InterfaceLock with the specified parent lasting for the
     * specified duration
     *
     * @param locker   the parent Creature for this InterfaceLock
     * @param duration the length of time this lock will run in milliseconds.  After the duration
     *                 has elapsed, the next combat turn is run
     */

    public InterfaceCombatLock( Creature locker, long duration )
    {
        super( locker, duration );
    }

    @Override
    public boolean checkFinished( )
    {
        if ( super.checkFinished( ) )
        {
            Game.areaListener.getCombatRunner( ).nextCombatTurn( );
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public int getPriority( )
    {
        return 100;
    }

    @Override
    public String toString( ) { return "Combat lock: " + super.toString( ); }
}
