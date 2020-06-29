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

package main.java.hale.rules;

import main.java.hale.Game;
import main.java.hale.bonus.Stat;
import main.java.hale.entity.Creature;
import main.java.hale.icon.Icon;
import main.java.hale.icon.IconFactory;
import main.java.hale.resource.ResourceManager;
import main.java.hale.resource.ResourceType;
import main.java.hale.util.SimpleJSONObject;
import main.java.hale.util.SimpleJSONParser;

/**
 * A skill represents a specific type of action that creatures can take, generally
 * not directly related to combat.  This includes things like picking a lock, setting
 * a trap, or crafting an item
 *
 * @author Jared
 */

public class Skill implements Comparable<Skill>
{
    private final String id;

    private final String noun;
    private final String presentTenseVerb, pastTenseVerb;

    private final Icon icon;

    private final String restrictToRole;
    private final boolean isUsableUntrained;
    private final Stat keyAttribute;
    private final boolean suffersArmorPenalty;
    private final boolean isCraft;
    private final boolean alwaysRolls100OutsideOfCombat;

    public Skill(String id)
    {
        this.id = id;

        SimpleJSONParser parser = new SimpleJSONParser("skills/" + id + ResourceType.JSON.getExtension());

        SimpleJSONObject nameIn = parser.getObject("name");

        noun = nameIn.get("noun", null);
        presentTenseVerb = nameIn.get("presentTenseVerb", null);
        pastTenseVerb = nameIn.get("pastTenseVerb", null);

        if (parser.containsKey("icon")) {
            icon = IconFactory.createIcon(parser.getObject("icon"));
        } else {
            icon = IconFactory.emptyIcon;
        }

        isUsableUntrained = parser.get("isUsableUntrained", false);
        suffersArmorPenalty = parser.get("suffersArmorPenalty", false);
        keyAttribute = Stat.valueOf(parser.get("keyAttribute", null));
        isCraft = parser.get("isCraft", false);

        if (parser.containsKey("restrictToRole")) {
            restrictToRole = parser.get("restrictToRole", null);
        } else {
            restrictToRole = null;
        }

        if (parser.containsKey("alwaysRolls100OutsideOfCombat")) {
            alwaysRolls100OutsideOfCombat = parser.get("alwaysRolls100OutsideOfCombat", false);
        } else {
            alwaysRolls100OutsideOfCombat = false;
        }

        parser.warnOnUnusedKeys();
    }

    /**
     * Returns true if this skill will automatically roll the highest possible value (100)
     * while outside of combat on all checks
     *
     * @return whether this skill will automatically roll 100 while outside combat
     */

    public boolean alwaysRolls100OutsideOfCombat()
    {
        return alwaysRolls100OutsideOfCombat;
    }

    /**
     * Returns true if the specified role can use this skill, false otherwise.  All roles
     * can use all skills, except for skills that are restricted to a specific role.  This
     * method assumes the creature owning the role has training in this skill, as needed.
     *
     * @param role
     * @return whether the specified role can use this skill
     */

    public boolean canUse(Role role)
    {
        if (restrictToRole == null) return true;

        return role.getID().equals(restrictToRole);
    }

    /**
     * Returns true if the specified creature can use this skill, false otherwise.  All creatures
     * can use all skills, except for skills that are restricted to a specific role.  Note that this
     * method assumes the creature has training in this skill, as needed.
     *
     * @param creature
     * @return whether the creature can use this skill
     */

    public boolean canUse(Creature creature)
    {
        if (restrictToRole == null) return true;

        Role role = Game.ruleset.getRole(restrictToRole);

        return (creature.roles.contains(role));
    }

    /**
     * Returns true if this skill is restricted to only being usable by a specific role,
     * false otherwise
     *
     * @return whether this skill is restricted to a specific role
     */

    public boolean isRestrictedToARole()
    {
        return restrictToRole != null;
    }

    /**
     * Returns the ID of the role that this skill is restricted to, or null if this
     * skill is usable by all roles
     *
     * @return the ID of the restricted role
     */

    public String getRestrictToRole()
    {
        return restrictToRole;
    }

    /**
     * Returns the HTML description for this skill, defined in its description file
     *
     * @return the HTML description
     */

    public String getDescription()
    {
        return ResourceManager.getResourceAsString("descriptions/skills/" + noun +
                ResourceType.HTML.getExtension());
    }

    /**
     * Returns the id of this skill
     *
     * @return the ID
     */

    public String getID()
    {
        return id;
    }


    /**
     * Returns the icon for this skill.  This may be the empty icon
     * if this skill does not define an icon
     *
     * @return the icon for this skill
     */

    public Icon getIcon()
    {
        return icon;
    }

    /**
     * Returns the past tense verb for using this skill
     *
     * @return the past tense verb
     */

    public String getPastTenseVerb()
    {
        return pastTenseVerb;
    }

    /**
     * Returns the present tense verb for using this skill
     *
     * @return the present tense verb
     */

    public String getPresentTenseVerb()
    {
        return presentTenseVerb;
    }

    /**
     * Returns the noun for describing this skill
     *
     * @return the noun
     */

    public String getNoun()
    {
        return noun;
    }

    /**
     * Returns true if this skill is usable by a creature with zero ranks in this skill,
     * false if creatures must have at least one rank
     *
     * @return whether this skill is usable untrained
     */

    public boolean isUsableUntrained()
    {
        return isUsableUntrained;
    }

    /**
     * Returns the key attribute for this skill, used in computing the skill check bonus
     *
     * @return the key attribute
     */

    public Stat getKeyAttribute()
    {
        return keyAttribute;
    }

    /**
     * Returns true if checks made for this skill suffer from the armor penalty of the parent
     * creature, false otherwise
     *
     * @return whether checks for this skill suffer an armor penalty
     */

    public boolean suffersArmorPenalty()
    {
        return suffersArmorPenalty;
    }

    /**
     * Returns true if this is a craft skill, meaning one that can be used with recipes to create items,
     * false otherwise
     *
     * @return whether this is a craft skill
     */

    public boolean isCraft()
    {
        return isCraft;
    }

    @Override
    public int compareTo(Skill other)
    {
        return this.id.compareTo(other.id);
    }
}
