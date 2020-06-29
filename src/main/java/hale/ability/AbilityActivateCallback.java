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

package main.java.hale.ability;

import java.util.ArrayList;

import main.java.hale.Game;

/**
 * This class is created and added as a callback to menu buttons used to activate an Ability or call
 * another function from an Ability's script
 * <p>
 * When the run method is called, the Ability is activated or the function is called
 *
 * @author Jared Stephen
 */

public class AbilityActivateCallback implements Runnable
{
    private AbilitySlot slot;

    private String function;
    private ScriptFunctionType type;
    private ArrayList<Object> arguments;

    /**
     * Creates a new AbilityActivateCallback for the specified AbilitySlot
     *
     * @param slot     the AbilitySlot to callback
     * @param function the function to be called on activation
     */

    public AbilityActivateCallback(AbilitySlot slot, String function)
    {
        this.slot = slot;
        this.function = function;
        this.arguments = new ArrayList<Object>();
        this.arguments.add(slot);
    }

    /**
     * Creates a new AbilityActivateCallback for the specified AbilitySlot
     *
     * @param slot the AbilitySlot to callback
     * @param type the script function type to be called on activation
     */

    public AbilityActivateCallback(AbilitySlot slot, ScriptFunctionType type)
    {
        this.slot = slot;
        this.type = type;
        this.arguments = new ArrayList<Object>();
        this.arguments.add(slot);
    }

    /**
     * Adds the specified Object as an argument that will passed to the Script
     * function
     *
     * @param argument the argument to add
     */

    public void addArgument(Object argument)
    {
        this.arguments.add(argument);
    }

    /*
     * The callback that activates the ability (non-Javadoc)
     * @see java.lang.Runnable#run()
     */

    @Override
    public void run()
    {
        Game.mainViewer.getMenu().hide();

        if (type != null) {
            slot.getAbility().executeFunction(type, arguments.toArray());
        } else
            if (function != null) {
                slot.getAbility().executeFunction(function, arguments.toArray());
            }

        Game.mainViewer.updateInterface();
    }
}
