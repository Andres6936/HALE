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

package hale.entity;

import hale.Game;
import hale.area.Area;
import hale.bonus.Stat;
import hale.loading.JSONOrderedObject;
import hale.loading.LoadGameException;
import hale.loading.ReferenceHandler;
import hale.quickbar.Quickbar;
import hale.util.SimpleJSONObject;
import hale.util.SimpleJSONParser;

/**
 * A player character (PC) is a type of creature that the player has direct control
 * over via the interface
 *
 * @author Jared
 */

public final class PC extends Creature
{
    private final PCTemplate template;

    private int experiencePoints;

    private int unspentSkillPoints;

    /**
     * The quickbar, specifying the quick access slots located on the
     * bottom of the interface
     */

    public final Quickbar quickbar;

    @Override
    public void load(SimpleJSONObject data, Area area, ReferenceHandler refHandler) throws LoadGameException
    {
        SimpleJSONObject statsIn = data.getObject("attributes");
        int[] attributes = new int[6];
        attributes[0] = statsIn.get("strength", 0);
        attributes[1] = statsIn.get("dexterity", 0);
        attributes[2] = statsIn.get("constitution", 0);
        attributes[3] = statsIn.get("intelligence", 0);
        attributes[4] = statsIn.get("wisdom", 0);
        attributes[5] = statsIn.get("charisma", 0);
        stats.setAttributes(attributes);

        super.load(data, area, refHandler);

        // template need to already have been loaded at this point, so that data
        // is not looked at here

        experiencePoints = data.get("experiencePoints", 0);
        unspentSkillPoints = data.get("unspentSkillPoints", 0);

        quickbar.load(data.getObject("quickbar"));
    }

    @Override
    public JSONOrderedObject save()
    {
        JSONOrderedObject out = super.save();

        // save data from the template that may not be present if the character file is deleted
        out.put("name", template.getName());
        out.put("gender", template.getGender().name());
        out.put("race", template.getRace().getID());
        out.put("portrait", template.getPortrait());

        if (template.getConversation() != null) {
            out.put("conversation", template.getConversation().getScriptLocation());
        }

        out.put("icon", template.getIcon().save());

        JSONOrderedObject attributes = new JSONOrderedObject();
        attributes.put("strength", stats.get(Stat.BaseStr));
        attributes.put("dexterity", stats.get(Stat.BaseDex));
        attributes.put("constitution", stats.get(Stat.BaseCon));
        attributes.put("intelligence", stats.get(Stat.BaseInt));
        attributes.put("wisdom", stats.get(Stat.BaseWis));
        attributes.put("charisma", stats.get(Stat.BaseCha));
        out.put("attributes", attributes);

        out.put("experiencePoints", experiencePoints);
        out.put("unspentSkillPoints", unspentSkillPoints);
        out.put("quickbar", quickbar.save());

        return out;
    }

    /**
     * Creates a new PC by parsing the specified JSON.  The template
     * has already been defined, and then additional data is read from the JSON
     * to fully define the PC
     *
     * @param template
     * @param parser
     */

    public PC(PCTemplate template, SimpleJSONParser parser)
    {
        super(template, parser);

        this.template = template;

        this.quickbar = new Quickbar(this);
        if (parser.containsKey("quickbar")) {
            quickbar.load(parser.getObject("quickbar"));
        }

        if (parser.containsKey("experiencePoints")) {
            this.experiencePoints = parser.get("experiencePoints", 0);
        }

        if (parser.containsKey("unspentSkillPoints")) {
            this.unspentSkillPoints = parser.get("unspentSkillPoints", 0);
        }
    }

    /**
     * Creates a new player character based on the specified template.  This is used for
     * creating buildable characters in the character editor
     *
     * @param template the template containing the immutable parts of the
     *                 PC definition
     */

    public PC(PCTemplate template)
    {
        super(template);

        this.template = template;

        this.quickbar = new Quickbar(this);

        this.experiencePoints = 0;
        this.unspentSkillPoints = 0;
    }

    /**
     * Creates a new copy of the specified PC.  Permanent creature data such as
     * stats, inventory, roles, skills, abilities, and quickbar are copied.  No other data is copied,
     * however
     *
     * @param other the PC to copy
     */

    public PC(PC other)
    {
        super(other);
        this.template = other.template;
        this.quickbar = new Quickbar(other.quickbar, this);

        this.experiencePoints = other.experiencePoints;
        ;
        this.unspentSkillPoints = other.unspentSkillPoints;
    }

    @Override
    public PCTemplate getTemplate()
    {
        return template;
    }

    /**
     * Gets the total number of experience points that this player character
     * has earned over his or her adventures
     *
     * @return the number of experience points
     */

    public int getExperiencePoints()
    {
        return experiencePoints;
    }

    /**
     * Adds the specified number of experience points to this PC
     *
     * @param amount the number of experience points to add
     */

    public void addExperiencePoints(int amount)
    {
        this.experiencePoints += amount;
    }

    /**
     * Gets the number of skill points this player character has left over from
     * their last level up
     *
     * @return the number of unspent skill points
     */

    public int getUnspentSkillPoints()
    {
        return unspentSkillPoints;
    }

    /**
     * Sets the number of skill points left over from the last level up to the
     * specified value
     *
     * @param value
     */

    public void setUnspentSkillPoints(int value)
    {
        this.unspentSkillPoints = value;
    }

    @Override
    public boolean elapseTime(int numRounds)
    {
        boolean returnValue = super.elapseTime(numRounds);

        if (Game.isInTurnMode()) {
            // take 1 hit point of damage per round while dying
            if (isDying()) {
                takeDamage(numRounds, "Effect");
                Game.mainViewer.addMessage("red", getTemplate().getName() + " is dying with " + getCurrentHitPoints() + " HP.");
            }
        } else
            if (!isDead()) {
                // regenerate HP while not in turn mode
                if (getCurrentHitPoints() < stats.get(Stat.MaxHP)) {
                    healDamage(numRounds * (1 + stats.get(Stat.MaxHP) / Game.ruleset.getValue("OutsideCombatHealingFactor")));
                }
            }

        return returnValue;
    }
}
