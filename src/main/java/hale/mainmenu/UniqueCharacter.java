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

package main.java.hale.mainmenu;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import main.java.hale.Game;
import main.java.hale.entity.PC;
import main.java.hale.resource.ResourceType;
import main.java.hale.rules.Race;
import main.java.hale.rules.Ruleset;

/**
 * A unique character is a construct used in listing out and selecting a
 * character in the new game window.  Characters that are just different
 * level versions of each other are grouped together
 *
 * @author Jared
 */

public class UniqueCharacter implements Iterable<PC>
{
    private final List<PC> pcs;

    private final String portraitID;
    private final String name;
    private final Ruleset.Gender gender;
    private final Race race;

    private int minLevel;
    private int maxLevel;

    /**
     * Creates a new new UniqueCharacter with the specified creature as the base.
     * Only creatures with matching portrait, name, gender, and race will be able to
     * be added to this UniqueCharacter
     *
     * @param pc the base creature
     */

    public UniqueCharacter(PC pc)
    {
        this.pcs = new ArrayList<PC>();

        pcs.add(pc);

        this.portraitID = pc.getTemplate().getPortrait();
        this.name = pc.getTemplate().getName();
        this.gender = pc.getTemplate().getGender();
        this.race = pc.getTemplate().getRace();

        setMinMaxLevel();
    }

    /**
     * Returns the number of creatures contained in this UniqueCharacter
     *
     * @return the number of creatures
     */

    public int size()
    {
        return pcs.size();
    }

    /**
     * Sets the minimum and maximum level for valid creatures in this unique character based on the current
     * campaign rules
     * Note that this constraint does not prevent creatures from being added.
     */

    public void setMinMaxLevel()
    {
        if (Game.curCampaign.allowLevelUp()) {
            this.minLevel = 1;
        } else {
            this.minLevel = Game.curCampaign.getMinStartingLevel();
        }

        this.maxLevel = Game.curCampaign.getMaxStartingLevel();
    }

    /**
     * Returns true if and only if the specified creature meets the minimum and maximum level
     * constraints for this unique character
     *
     * @param pc the creature to check
     * @return true if the level constraints are met, false otherwise
     */

    public boolean meetsLevelConstraints(PC pc)
    {
        int level = pc.roles.getTotalLevel();

        return level >= this.minLevel && level <= this.maxLevel;
    }

    /**
     * Returns the first creature in this uniqueCharacter
     *
     * @return the first creature in this uniqueCharacter
     */

    public PC getFirstCreature()
    {
        return pcs.get(0);
    }

    /**
     * Returns the highest level creature that meets the minimum level and
     * maximum level criterion from the list of creatures contained in this UniqueCharacter
     * If no creature meets the criterion, returns null
     *
     * @return the highest level PC
     */

    public PC getBestCreature()
    {
        int highestLevel = -1;
        PC maxPC = null;

        for (PC pc : pcs) {
            int level = pc.roles.getTotalLevel();

            if (level > highestLevel && level >= this.minLevel && level <= this.maxLevel) {
                highestLevel = level;
                maxPC = pc;
            }
        }

        return maxPC;
    }

    /**
     * Checks if the specified creature matches and can be added to this unique character.  If
     * the creature matches, it is added to this UniqueCharacter
     *
     * @param pc the creature to check
     * @return true if the creature matches, false otherwise
     */

    public boolean addIfMatches(PC pc)
    {
        if (!this.portraitID.equals(pc.getTemplate().getPortrait())) return false;

        if (!this.name.equals(pc.getTemplate().getName())) return false;

        if (this.gender != pc.getTemplate().getGender()) return false;

        if (this.race != pc.getTemplate().getRace()) return false;

        this.pcs.add(pc);

        return true;
    }

    /**
     * Deletes the file corresponding to the specified creature on disk and removes
     * the creature from this UniqueCharacter.  If the creature is not contained in this
     * unique character, no action is performed
     *
     * @param pc the creature to delete
     */

    public void deleteCreature(PC pc)
    {
        int index = this.pcs.indexOf(pc);

        if (index == -1) return;

        String fileName = Game.plataform.getCharactersDirectory() + pc.getTemplate().getID() +
                ResourceType.JSON.getExtension();

        new File(fileName).delete();

        this.pcs.remove(index);
    }

    @Override
    public Iterator<PC> iterator()
    {
        return pcs.iterator();
    }
}
