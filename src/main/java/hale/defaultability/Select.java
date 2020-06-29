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
import main.java.hale.entity.Creature;
import main.java.hale.entity.Location;
import main.java.hale.entity.PC;

/**
 * A DefaultAbility for selecting a specified Creature
 *
 * @author Jared Stephen
 */

public class Select implements DefaultAbility
{

    @Override
    public String getActionName()
    {
        return "Select";
    }

    @Override
    public boolean canActivate(PC parent, Location targetPosition)
    {
        Creature target = targetPosition.getCreature();

        // note that we purposely allow you to select the already selected creature
        // this makes the interface a little nicer; showing cancel above the selected
        // creature looks strange

        return (target != null && target instanceof PC && target.isPlayerFaction());
    }

    @Override
    public void activate(PC parent, Location targetPosition)
    {
        Creature target = targetPosition.getCreature();

        Select.selectCreature(target);
        Game.areaViewer.addDelayedScrollToCreature(target);

        Game.areaListener.computeMouseState();
    }

    /**
     * Selects the specified Creature.  The party will now have the
     * specified Creature as the selected Creature and the interface
     * will change to indicate the new selection.
     *
     * @param creature the Creature to be selected
     */

    public static void selectCreature(Creature creature)
    {
        if (!(creature instanceof PC)) return;

        Game.curCampaign.party.setSelected(creature);
        Game.selectedEntity = Game.curCampaign.party.getSelected();
        Game.mainViewer.containerWindow.setVisible(false);
        Game.mainViewer.updateInterface();
    }

    @Override
    public DefaultAbility getInstance()
    {
        // this Object has no internal state, so no need to make a copy
        return this;
    }

}
