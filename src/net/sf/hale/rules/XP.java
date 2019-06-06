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

import net.sf.hale.Game;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.PC;

/**
 * Class for handling the experience point table
 *
 * @author Jared
 */

public class XP
{
    /**
     * The maximum level supported by the XP table
     */

    public static int MAX_LEVEL = 100;

    private static int[] pointsForLevel;

    /**
     * Builds the table of experience point values.  Must be called before any
     * other method in this class
     */

    public static void initXPTable( )
    {
        int levelMultFactor = Game.ruleset.getValue( "XPLevelMultFactor" );
        int levelExpBaseInv = Game.ruleset.getValue( "XPLevelExpBaseInv" );
        double levelExpBase = 1.0 + 1.0 / ( ( double ) levelExpBaseInv );

        XP.pointsForLevel = new int[ MAX_LEVEL + 2 ];

        XP.pointsForLevel[ 0 ] = 0;
        XP.pointsForLevel[ 1 ] = 0;

        double curPoints = 0.0;

        for ( int prevLevel = 1; prevLevel <= MAX_LEVEL; prevLevel++ )
        {
            curPoints += prevLevel * levelMultFactor * Math.pow( levelExpBase, prevLevel );

            XP.pointsForLevel[ prevLevel + 1 ] = ( int ) ( ( curPoints + 50 ) / 100 ) * 100;
        }
    }

    /**
     * Returns the number of XP required to reach the specified level
     *
     * @param level the level, must be less than or equal to {@link XP#MAX_LEVEL}
     * @return the number of XP to reach the level
     */

    public static int getPointsForLevel( int level )
    {
        return XP.pointsForLevel[ level ];
    }

    /**
     * Assigns the XP from the given encounter to the player character party, based on the challenge
     * of the encounter and the combat length
     * @param encounter the encounter to assign XP from
     * @param combatLength the length of the combat in rounds
     */

    /**
     * Adds the specified number of experience points directly to the party
     *
     * @param xp the number of experience points to add
     */

    public static void addPartyXP( int xp )
    {
        rewardXP( xp );

        Game.mainViewer.addMessage( "green", "The party has earned " + xp + " experience points." );
    }

    private static void rewardXP( int baseXP )
    {
        int partySize = 0;
        for ( Creature c : Game.curCampaign.party )
        {
            if ( ! c.isDead( ) && ! c.isSummoned( ) ) partySize++;
        }

        int perCharacterXP = baseXP / partySize;

        for ( PC pc : Game.curCampaign.party )
        {
            if ( ! pc.isDead( ) && ! pc.isSummoned( ) )
            {
                pc.addExperiencePoints( perCharacterXP );
            }
        }
    }
}
