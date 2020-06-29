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

import hale.Game;
import hale.entity.Item;
import hale.entity.Location;
import hale.entity.PC;
import hale.entity.Trap;
import hale.view.ItemDetailsWindow;

/**
 * Default Ability for examining an item in the area.
 *
 * @author Jared Stephen
 */

public class ExamineItem implements DefaultAbility
{

    private Item target;

    @Override
    public String getActionName()
    {
        return "Examine " + target.getTemplate().getName();
    }

    @Override
    public boolean canActivate(PC parent, Location targetPosition)
    {
        if (!parent.hasVisibility(targetPosition)) return false;

        Trap trap = targetPosition.getTrap();

        if (trap == null || !trap.isSpotted()) return false;

        target = trap;

        return true;
    }

    @Override
    public void activate(PC parent, Location targetPosition)
    {
        ItemDetailsWindow window = new ItemDetailsWindow(target);
        window.setPosition(Game.areaListener.getMouseGUIX() - window.getWidth() / 2,
                Game.areaListener.getMouseGUIY() - window.getHeight() / 2);
        Game.mainViewer.add(window);

        Game.areaListener.computeMouseState();
    }

    @Override
    public DefaultAbility getInstance()
    {
        return new ExamineItem();
    }

}
