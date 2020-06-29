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

package main.java.hale.defaultability;

import main.java.hale.Game;
import main.java.hale.entity.Location;
import main.java.hale.entity.PC;

/**
 * A callback that is used to
 * {@link DefaultAbility#activate(PC, Location)}
 * a DefaultAbility.  Can be added as a Callback to a button so that it runs (activating
 * the specified DefaultAbility) when the button is clicked.
 *
 * @author Jared Stephen
 */

public class DefaultAbilityCallback implements Runnable
{
    private DefaultAbility ability;
    private PC parent;
    private Location targetPosition;

    /**
     * Creates a new DefaultAbilityCallback that will activate the
     * specified DefaultAbility when run.
     *
     * @param ability the DefaultAbility to activate
     */

    public DefaultAbilityCallback(DefaultAbility ability)
    {
        this.ability = ability;
    }

    /**
     * Sets the paramaters that will be passed to the DefaultAbility activate
     * method.
     *
     * @param parent         the parent Creature
     * @param targetPosition the targeted grid Point
     */

    public void setActivateParameters(PC parent, Location targetPosition)
    {
        this.parent = parent;
        this.targetPosition = targetPosition;
    }

    /*
     * Activate the DefaultAbility (non-Javadoc)
     * @see java.lang.Runnable#run()
     */

    @Override
    public void run()
    {
        ability.activate(parent, targetPosition);

        Game.mainViewer.getMenu().hide();
    }
}
