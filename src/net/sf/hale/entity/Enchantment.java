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

import javax.script.ScriptException;

import net.sf.hale.Game;
import net.sf.hale.bonus.BonusList;
import net.sf.hale.util.JSEngine;
import net.sf.hale.util.Logger;

/**
 * Enchantments are short, one line scripts that give simple bonuses to items
 *
 * @author Jared
 */

public class Enchantment
{
    private final String script;
    private final boolean isUser;
    private final BonusList bonuses;

    /**
     * Creates a new enchantment with the specified script.
     *
     * @param script the script for this enchantment.  This script is evaulated by the JavaScript
     *               engine to produce the list of bonuses
     * @param user   whether this enchantment is user generated
     */

    public Enchantment( String script, boolean user )
    {
        this.script = script;
        this.isUser = user;

        this.bonuses = new BonusList( );

        JSEngine engine = Game.scriptEngineManager.getEngine( );

        engine.put( "entity", bonuses );
        try
        {
            engine.eval( script );
            engine.release( );
        }
        catch ( ScriptException e )
        {
            Logger.appendToErrorLog( "Error running enchantment " + script );
        }
    }

    /**
     * Creates a new enchantment with the specified script.  This enchantment will not
     * be a user enchantment.
     *
     * @param script the script for this enchantment.  The script is evaluated by the JavaScript
     *               engine to produce the list of bonuses
     */

    public Enchantment( String script )
    {
        this( script, false );
    }

    /**
     * Returns the script that generates this enchantment
     *
     * @return the enchantment script
     */

    public String getScript( )
    {
        return script;
    }

    /**
     * Returns whether this enchantment is user generated (through crafting), or specified
     * in an item datafile
     *
     * @return true if this enchantment is user generated, false otherwise
     */

    public boolean isUser( )
    {
        return isUser;
    }

    /**
     * Returns the list of bonuses in this enchantment
     *
     * @return the list of bonuses
     */

    public BonusList getBonuses( )
    {
        return bonuses;
    }
}
