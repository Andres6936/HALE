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

import de.matthiasmann.twl.Color;
import net.sf.hale.Game;
import net.sf.hale.ability.ScriptFunctionType;
import net.sf.hale.area.Area;
import net.sf.hale.bonus.Bonus;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.LoadGameException;
import net.sf.hale.loading.ReferenceHandler;
import net.sf.hale.util.SimpleJSONObject;

/**
 * A Trap is a type of item that is triggered when a creature passes through
 * its tile.  It will do damage or cause a negative effect on its victim.
 *
 * @author Jared
 */

public class Trap extends Item
{

    // whether the location of this trap is known to hostile creatures
    private boolean isSpotted;

    private final TrapTemplate template;

    @Override
    public void load( SimpleJSONObject data, Area area, ReferenceHandler refHandler ) throws LoadGameException
    {
        super.load( data, area, refHandler );

        isSpotted = data.get( "isSpotted", false );
    }

    @Override
    public JSONOrderedObject save( )
    {
        JSONOrderedObject out = super.save( );

        out.put( "isSpotted", isSpotted );

        return out;
    }

    protected Trap( TrapTemplate template )
    {
        super( template );

        this.template = template;

        this.isSpotted = false;
    }

    @Override
    public TrapTemplate getTemplate( )
    {
        return template;
    }

    /**
     * Returns true if this trap is spotted and will be seen by all creatures.  Returns false
     * if the trap is not spotted and will only be visible to friendly creatures.  Hostiles
     * must succeed in a Search check against find difficulty to spot a trap.
     *
     * @return whether the trap is visible to hostile creatures
     */

    public boolean isSpotted( )
    {
        return isSpotted;
    }

    /**
     * Returns true if the parent is currently capable of attempting to place a trap on
     * their current location, false otherwise
     *
     * @param parent
     * @return whether the parent can attempt to place a trap
     */

    public boolean canAttemptPlace( PC parent )
    {
        if ( ! parent.stats.has( Bonus.Type.TrapHandling ) ) return false;

        // if there is already a trap at the location
        if ( parent.getLocation( ).getTrap( ) != null )
        {
            Game.mainViewer.addFadeAway( "Trap already present here", parent.getLocation( ).getX( ),
                                         parent.getLocation( ).getY( ), Color.RED );
            return false;
        }

        return true;
    }

    /**
     * The specified parent attemps to place this trap at their current location
     *
     * @param parent
     * @return true if this trap is succesfully placed, false otherwise
     */

    public boolean attemptPlace( PC parent )
    {
        if ( ! canAttemptPlace( parent ) ) return false;

        int difficulty = modifyValueByQuality( template.getPlaceDifficulty( ) );
        int check = parent.skills.getCheck( "Traps", difficulty );

        // other creatures get a chance to spot a hiding creature when it places a trap
        Game.scriptInterface.performSearchChecksForCreature( parent, Game.ruleset.getValue( "HidePlaceTrapPenalty" ) );

        if ( check >= difficulty )
        {
            // the player character can see their own traps
            isSpotted = true;

            setLocation( parent.getLocation( ) );
            setFaction( parent.getFaction( ) );
            Game.curCampaign.curArea.placeTrap( this );
            parent.inventory.getUnequippedItems( ).remove( this );

            Game.mainViewer.addFadeAway( "Trap placed", parent.getLocation( ).getX( ),
                                         parent.getLocation( ).getY( ), Color.RED );

            return true;
        }
        else
        {
            Game.mainViewer.addFadeAway( "Failed to place trap", parent.getLocation( ).getX( ),
                                         parent.getLocation( ).getY( ), Color.RED );

            if ( check < difficulty - Game.ruleset.getValue( "TrapCriticalFailureThreshold" ) )
            {
                //critical failure
                fireTrap( parent );
            }

            return false;
        }
    }

    /**
     * The specified Creature will attempt to disarm this trap.  The
     * Creature must succeed at a Traps check against this Trap's
     * disarmDifficulty.  If successfully disarmed via this method,
     * the trap is destroyed.
     *
     * @param parent the Creature trying to disarm this Trap.
     * @return true if and only if this Trap was disarmed successfully.
     */

    public boolean attemptDisarm( Creature parent )
    {
        int difficulty = modifyValueByQuality( template.getDisarmDifficulty( ) );
        int check = parent.skills.getCheck( "Traps", difficulty );

        // other creatures get a chance to spot a hiding creature when it disarms a trap
        Game.scriptInterface.performSearchChecksForCreature( parent, Game.ruleset.getValue( "HideDisarmTrapPenalty" ) );

        if ( check >= difficulty )
        {
            Game.curCampaign.curArea.removeEntity( this );
            return true;
        }
        else if ( check < difficulty - Game.ruleset.getValue( "TrapCriticalFailureThreshold" ) )
        {
            //critical failure
            fireTrap( parent );
        }

        return false;
    }

    /**
     * The specified Creature will attempt to recover this trap.  The
     * Creature must succeed at a Traps check against this Trap's
     * recoverDifficulty.  If recovered, the trap is added to the
     * creature's inventory for their use
     *
     * @param parent the Creature trying to recover this Trap
     * @return true if and only if the Trap was recovered successfully
     */

    public boolean attemptRecover( Creature parent )
    {
        int difficulty = modifyValueByQuality( template.getRecoverDifficulty( ) );
        int check = parent.skills.getCheck( "Traps", difficulty );

        // other creatures get a chance to spot a hiding creature when it recovers a trap
        Game.scriptInterface.performSearchChecksForCreature( parent, Game.ruleset.getValue( "HideRecoverTrapPenalty" ) );

        if ( check >= difficulty )
        {
            Game.curCampaign.curArea.removeEntity( this );
            parent.inventory.getUnequippedItems( ).add( this );
            return true;
        }
        else if ( check < difficulty - Game.ruleset.getValue( "TrapCriticalFailureThreshold" ) )
        {
            //critical failure
            fireTrap( parent );
        }

        return false;
    }

    /**
     * The specified Creature will attempt to spot this Trap.  The creature must
     * succeed at a Search check against this Trap's findDifficulty.
     *
     * @param parent the Creature trying to spot the trap
     * @return true if the trap was spotted via this search attempt, false otherwise
     */

    public boolean attemptSearch( Creature parent )
    {
        if ( isSpotted ) return false;

        int distancePenalty = 10 * getLocation( ).getDistance( parent );

        if ( parent.skills.performCheck( "Search", modifyValueByQuality( template.getFindDifficulty( ) ) + distancePenalty ) )
        {
            Game.mainViewer.addMessage( "orange", parent.getTemplate( ).getName( ) + " spots a trap!" );
            Game.mainViewer.addFadeAway( "Search: Success", getLocation( ).getX( ), getLocation( ).getY( ), new Color( 0xFFAbA9A9 ) );

            isSpotted = true;
        }

        return isSpotted;
    }

    /**
     * Checks to see if the target is valid for this trap.  If so, springs the trap on them.
     *
     * @param target
     * @return whether the trap was fired or not
     */

    public boolean checkSpringTrap( Creature target )
    {
        // only fire on hostile creatures
        if ( ! getFaction( ).isHostile( target ) )
        { return false; }

        Game.mainViewer.addMessage( "red", target.getTemplate( ).getName( ) + " springs a trap." );

        fireTrap( target );

        return true;
    }

    /**
     * Causes the trap to "Attack" the target.  The target is allowed a reflex save.
     *
     * @param target
     */

    private void fireTrap( Creature target )
    {
        Game.mainViewer.addFadeAway( "Trap sprung", target.getLocation( ).getX( ),
                                     target.getLocation( ).getY( ), Color.RED );

        if ( template.hasScript( ) )
        {
            template.getScript( ).executeFunction( ScriptFunctionType.onSpringTrap, this, target );
        }

        // other creatures get a chance to spot a hiding creature when it springs a trap
        Game.scriptInterface.performSearchChecksForCreature( target, Game.ruleset.getValue( "HideSpringTrapPenalty" ) );

        if ( ! target.stats.getReflexResistanceCheck( modifyValueByQuality( template.getReflexDifficulty( ) ) ) )
        {
            if ( template.hasScript( ) )
            { template.getScript( ).executeFunction( ScriptFunctionType.onTrapReflexFailed, this, target ); }

            int minDamage = modifyValueByQuality( template.getMinDamage( ) );
            int maxDamage = modifyValueByQuality( template.getMaxDamage( ) );
            int damage = Game.dice.rand( minDamage, maxDamage );

            if ( damage != 0 )
            { target.takeDamage( damage, template.getDamageType( ).getName( ) ); }
        }

        // remove the trap as it has fired
        Game.curCampaign.curArea.removeEntity( this );
    }

    /**
     * Multiplies the specified difficulty by the quality modifier of this trap
     * as a percentage
     *
     * @param difficulty
     * @return the multiplied value
     */

    public int modifyValueByQuality( int difficulty )
    {
        int num = Game.ruleset.getValue( "TrapQualityDifficultyNumerator" );
        int den = Game.ruleset.getValue( "TrapQualityDifficultyDenominator" );

        int qualityBonus = this.getQuality( ).getModifier( );

        int bonus = qualityBonus * num / den;

        return difficulty * ( 100 + bonus ) / 100;
    }
}
