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

package hale.ability;

import de.matthiasmann.twl.Widget;
import hale.Game;
import hale.entity.Creature;
import hale.view.AbilityDetailsWindow;

/**
 * The callback that is run whenever the "View Details" button is clicked
 * on an Ability's right click menu.  When activated, this callback shows
 * an AbilityDetailsWindow for a specified Ability
 *
 * @author Jared Stephen
 */

public class AbilityExamineCallback implements Runnable
{
    private Ability ability;
    private Widget parent;
    private int x, y;

    private Creature owner;

    /**
     * Create a new AbilityExamineCallback.  When activated with the run()
     * method, an AbilityDetailsWindow will appear for the specified Ability
     *
     * @param ability the Ability to view the details of
     * @param parent  the parent Widget creating this Callback
     * @param owner   the owner of the specified ability or null for no owner
     */

    public AbilityExamineCallback(Ability ability, Widget parent, Creature owner)
    {
        this.owner = owner;
        this.ability = ability;
        this.parent = parent;
        this.x = 0;
        this.y = 0;
    }

    /**
     * Specifies the coordinates of the center of the AbilityDetailsWindow
     * when it is created
     *
     * @param x the x Coordinate of the center of the AbilityDetailsWindow
     * @param y the y Coordinate of the center of the AbilityDetailsWindow
     */

    public void setWindowCenter(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    /*
     * The Callback that shows the window (non-Javadoc)
     * @see java.lang.Runnable#run()
     */

    @Override
    public void run()
    {
        if (Game.mainViewer != null) {
            Game.mainViewer.getMenu().hide();
        }


        AbilityDetailsWindow window = new AbilityDetailsWindow(ability, owner, true);
        window.setPosition(x - window.getWidth() / 2, y - window.getHeight() / 2);

        parent.getGUI().getRootPane().add(window);
    }
}
