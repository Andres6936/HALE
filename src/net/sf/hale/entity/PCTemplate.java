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

import net.sf.hale.icon.Icon;
import net.sf.hale.rules.Race;
import net.sf.hale.rules.Ruleset;
import net.sf.hale.util.SimpleJSONObject;

/**
 * The class containing the immutable parts of a player character creature
 *
 * @author Jared
 */

public class PCTemplate extends CreatureTemplate
{

    // whether this character is one of the defaults in the character directory
    private final boolean isPregenerated;

    /**
     * Creates a new PC template with the specified parameters.  All non-specified parameters
     * are set to default (null or false) values
     *
     * @param id
     * @param name
     * @param icon     the icon to use for both UI and Area icons
     * @param gender
     * @param race
     * @param portrait
     */

    public PCTemplate( String id, String name, Icon icon, Ruleset.Gender gender, Race race, String portrait )
    {
        super( id, name, icon, gender, race, portrait );

        this.isPregenerated = false;
    }

    /**
     * Creates a new Player Character template
     *
     * @param id   the entity ID
     * @param data the JSON data to parse
     */

    public PCTemplate( String id, SimpleJSONObject data )
    {
        super( id, data );

        data.setWarnOnMissingKeys( false );

        this.isPregenerated = data.get( "isPregenerated", false );

        data.setWarnOnMissingKeys( true );
    }

    @Override
    public PC createInstance( )
    {
        throw new IllegalStateException( "Instances of PCs cannot be created directly" );
    }

    /**
     * Returns true if this is one of the default characters in the character directory
     * false otherwise
     *
     * @return whether this is a default character
     */

    public boolean isPregenerated( )
    {
        return isPregenerated;
    }

}
