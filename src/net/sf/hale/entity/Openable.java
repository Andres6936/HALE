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
import net.sf.hale.ability.ScriptFunctionType;
import net.sf.hale.area.Area;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.LoadGameException;
import net.sf.hale.loading.ReferenceHandler;
import net.sf.hale.util.SimpleJSONObject;

/**
 * An openable is an entity that is capable of being open and closed, including
 * doors and containers
 *
 * @author Jared
 */

public abstract class Openable extends Entity
{

    private OpenableTemplate template;

    private boolean isOpen;
    private boolean isLocked;

    @Override
    public JSONOrderedObject save( )
    {
        JSONOrderedObject out = super.save( );

        out.put( "isOpen", isOpen );

        if ( isLocked )
        { out.put( "isLocked", isLocked ); }

        return out;
    }

    @Override
    public void load( SimpleJSONObject data, Area area, ReferenceHandler refHandler ) throws LoadGameException
    {
        super.load( data, area, refHandler );

        isOpen = data.get( "isOpen", false );

        if ( data.containsKey( "isLocked" ) )
        {
            isLocked = data.get( "isLocked", false );
        }
        else
        {
            isLocked = false;
        }
    }

    /**
     * Creates a new Openable
     *
     * @param template
     */

    protected Openable( OpenableTemplate template )
    {
        super( template );

        this.template = template;

        this.isOpen = false;
        this.isLocked = template.isDefaultLocked( );
    }

    @Override
    public OpenableTemplate getTemplate( )
    {
        return template;
    }

    /**
     * Returns true if this openable is open, false if it is closed
     *
     * @return whether this openable is open
     */

    public boolean isOpen( )
    {
        return isOpen;
    }

    /**
     * Returns true if this openable is locked, false if it is not locked
     *
     * @return whether this openable is locked
     */

    public boolean isLocked( )
    {
        return isLocked;
    }

    /**
     * Sets the locked state of this openable
     *
     * @param locked
     */

    public void setLocked( boolean locked )
    {
        this.isLocked = locked;
    }

    /**
     * Unlocks this openable
     */

    public void unlock( )
    {
        isLocked = false;
    }

    /**
     * Unlocks this openable and runs any associated scripts
     *
     * @param unlocker
     */

    public void unlock( Creature unlocker )
    {
        if ( isLocked && template.hasScript( ) )
        { template.getScript( ).executeFunction( ScriptFunctionType.onUnlock, this, unlocker ); }
        isLocked = false;
    }

    /**
     * The specified creature makes a locks attempt to pick the lock of this Openable.
     * If successful, this Openable is unlocked
     *
     * @param unlocker
     * @return whether this openable is now unlocked
     */

    public boolean attemptUnlock( Creature unlocker )
    {
        if ( ! isLocked ) return true;

        if ( template.isKeyRequiredToUnlock( ) )
        {
            Game.mainViewer.addMessage( "orange", "A specific key is required to unlock that object." );
        }
        else
        {
            int difficulty = template.getLockDifficulty( );
            int check = unlocker.skills.getCheck( "Locks", difficulty );

            if ( check >= difficulty )
            {
                unlock( unlocker );
            }
        }

        return ! isLocked;
    }

    /**
     * The specified creature will attempt to open this Openable.  If this Openable
     * is locked, the opener must have the key to use this method (otherwise, they will
     * need to pick the lock first, if possible)
     *
     * @param opener
     * @return true if the openable was successfully opened, false otherwise
     */

    public boolean attemptOpen( Creature opener )
    {
        if ( isLocked && template.hasKey( ) )
        {
            if ( opener.inventory.getTotalQuantity( template.getKeyID( ) ) > 0 )
            {
                Game.mainViewer.addMessage( "orange", opener.getTemplate( ).getName( ) + " uses a key." );

                unlock( opener );

                if ( template.isRemoveKeyOnUnlock( ) )
                {
                    opener.inventory.remove( template.getKeyID( ), 1 );
                }
            }
        }

        if ( ! isLocked )
        {
            // if openable was already unlocked or unlock was successful

            if ( ! isOpen && template.hasScript( ) )
            { template.getScript( ).executeFunction( ScriptFunctionType.onOpen, this, opener ); }

            isOpen = true;
        }
        else
        {
            Game.mainViewer.addMessage( "orange", "That object is locked." );
        }

        return isOpen;
    }

    /**
     * The specified creature closes this Openable.  Unlock {@link #attemptOpen(Creature)},
     * this method guarantees success
     *
     * @param closer
     */

    public void close( Creature closer )
    {
        if ( isOpen && template.hasScript( ) )
        { template.getScript( ).executeFunction( ScriptFunctionType.onClose, this, closer ); }

        isOpen = false;
    }

    @Override
    public void areaDraw( int x, int y )
    {
        if ( isOpen )
        { template.getOpenIcon( ).drawCentered( x, y, Game.TILE_SIZE, Game.TILE_SIZE ); }
        else
        { template.getClosedIcon( ).drawCentered( x, y, Game.TILE_SIZE, Game.TILE_SIZE ); }
    }

}
