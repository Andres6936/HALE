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

package hale.defaultability;

import hale.entity.Location;
import hale.entity.PC;

/**
 * An interface for DefaultAbilities.  These are abilities that can be activated
 * directly through the basic interface via left clicking or right clicking
 * on the Area and selecting the corresponding entry in a list.  This is in
 * contrast to other Abilities which call scripts in order to perform their
 * actions, create effects, etc.
 *
 * @author Jared Stephen
 */

public interface DefaultAbility
{
    /**
     * Returns the descriptive String of the action that this DefaultAbility
     * accomplishes.
     *
     * @return the String name of the action for this DefaultAbility
     */

    public String getActionName();

    /**
     * Determines whether this can be activated with the given Creature
     * as parent and the mouse over the given position.
     *
     * @param parent         the Creature that is checking whether it can activate
     *                       this DefaultAbility
     * @param targetPosition the grid position of the mouse; where this
     *                       DefaultAbility is being activated
     * @return true if this DefaultAbility can be activated with the given
     * parameters, false otherwise
     */

    public boolean canActivate(PC parent, Location targetPosition);

    /**
     * Activates this DefaultAbility for the given Creature at the
     * specified position.  Some action will be taken based on the ability,
     * for example moving to a position or opening a container.
     * <p>
     * Note that the {@link #canActivate(PC, Location)} method must
     * be called prior to calling this method.
     *
     * @param parent         the Creature that is activating this DefaultAbility
     * @param targetPosition the grid position where this DefaultAbility is
     *                       being activated
     */

    public void activate(PC parent, Location targetPosition);

    /**
     * Returns a copy of this DefaultAbility with an empty internal state.
     *
     * @return a copy of this DefaultAbility with an empty internal state.
     */

    public DefaultAbility getInstance();
}
