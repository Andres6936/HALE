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
import net.sf.hale.icon.Icon;
import net.sf.hale.rules.Race;
import net.sf.hale.rules.Ruleset;
import net.sf.hale.util.SimpleJSONObject;

/**
 * The class containing the immutable parts of a creature
 *
 * @author Jared
 */

public abstract class CreatureTemplate extends EntityTemplate
{

    private final Ruleset.Gender gender;
    private final Race race;
    private final String portrait;
    private final int initiativeTickerHeight;

    /**
     * Creates a new CreatureTemplate with the specified parameters.  All non-specified parameters
     * are set to default (null) values
     *
     * @param id
     * @param name
     * @param icon     the Icon to use for both UI and Area Icons
     * @param gender
     * @param race
     * @param portrait
     */

    public CreatureTemplate( String id, String name, Icon icon, Ruleset.Gender gender, Race race, String portrait )
    {
        super( id, name, icon );

        this.gender = gender;
        this.race = race;
        this.portrait = portrait;
        this.initiativeTickerHeight = Game.TILE_SIZE;
    }

    /**
     * Creates a CreatureTemplate with the specified ID using the data in the
     * specified JSON parser
     *
     * @param id   the entity ID
     * @param data the JSONObject containing the data to be parsed
     */

    public CreatureTemplate( String id, SimpleJSONObject data )
    {
        super( id, data );

        String gender = data.get( "gender", null );

        this.gender = Ruleset.Gender.valueOf( gender );

        String race = data.get( "race", null );

        this.race = Game.ruleset.getRace( race );

        this.portrait = data.get( "portrait", null );

        if ( data.containsKey( "initiativeTickerHeight" ) )
        {
            this.initiativeTickerHeight = data.get( "initiativeTickerHeight", 0 );
        }
        else
        {
            this.initiativeTickerHeight = Game.TILE_SIZE;
        }
    }

    /**
     * Returns the amount of vertical space, in pixels, that should be reserved
     * for showing this creature on the initiative ticker during combat
     *
     * @return the initiative ticker height
     */

    public int getInitiativeTickerHeight( )
    {
        return initiativeTickerHeight;
    }

    /**
     * Returns the gender (sex) of this creature
     *
     * @return the gender of this creature
     */

    public Ruleset.Gender getGender( )
    {
        return gender;
    }

    /**
     * Returns the race of this creature
     *
     * @return the race of this creature
     */

    public Race getRace( )
    {
        return race;
    }

    /**
     * Returns a string representing the resource location of this creature's portrait
     *
     * @return the location of this creature's portrait
     */

    public String getPortrait( )
    {
        return portrait;
    }

}
