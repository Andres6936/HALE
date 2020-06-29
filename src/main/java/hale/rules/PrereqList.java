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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.java.hale.Game;
import main.java.hale.ability.Ability;
import main.java.hale.bonus.Stat;
import main.java.hale.entity.Creature;
import main.java.hale.util.SimpleJSONArray;
import main.java.hale.util.SimpleJSONArrayEntry;
import main.java.hale.util.SimpleJSONObject;

/**
 * A List of Prerequisites that a Creature must meet prior to being able to
 * select a Role, Ability, or anything else with prereqs.  The list can include
 * required skill ranks, stats, proficiencies, roles, and abilities.  This class
 * is immutable.
 *
 * @author Jared Stephen
 */

public class PrereqList
{
    private final Map<String, Integer> skillPrereqs;
    private final List<String> abilityPrereqs;

    private final Map<Stat, Integer> statPrereqs;

    private final List<String> weaponProficiencyPrereqs;
    private final List<String> armorProficiencyPrereqs;

    // unlike all the others these are implemented as an OR condition,
    // meaning the creature must have at least one of the specified roles
    private final List<String> rolePrereqs;
    private final List<Integer> roleLevelPrereqs;

    /**
     * Creates a new, empty PrereqList.  All Creatures meet the
     * requirements of an empty PrereqList
     */

    public PrereqList()
    {
        skillPrereqs = new HashMap<String, Integer>();
        abilityPrereqs = new ArrayList<String>();
        statPrereqs = new HashMap<Stat, Integer>();

        weaponProficiencyPrereqs = new ArrayList<String>();
        armorProficiencyPrereqs = new ArrayList<String>();

        rolePrereqs = new ArrayList<String>();
        roleLevelPrereqs = new ArrayList<Integer>();
    }

    /**
     * Creates a new PrereqList from the specified data.
     * Adds prereqs as specified in the JSON data.  The valid types are "skills",
     * "abilities", "roles", "stats", "weapons", and "armor".
     *
     * @param data the data to parse
     */

    public PrereqList(SimpleJSONObject data)
    {
        this();

        for (PrereqType type : PrereqType.values()) {
            if (data.containsKey(type.name())) {
                type.parse(data.getArray(type.name()), this);
            }
        }
    }

    /**
     * Returns true if and only if the specified creature has at least one level of one of the
     * roles in this prereq list.  The creature need not meet the actual role prereq, or any other
     * prereqs.  This method will return true if this prereq list contains no role prereqs.
     *
     * @param c the creature to check
     * @return whether this creature has at least one level in any role prereq in this list
     */

    public boolean hasRolePrereqs(Creature c)
    {
        if (rolePrereqs.size() == 0) return true;

        for (int i = 0; i < rolePrereqs.size(); i++) {
            Role cc = Game.ruleset.getRole(rolePrereqs.get(i));

            if (cc != null) {
                if (c.roles.getLevel(cc) >= 1) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns true if and only if the specified creature meets the role prereqs of this
     * prereq list.  See {@link #meetsPrereqs(Creature)} for how the role prereqs are defined.
     * Note that if this prereq list contains no role prereqs, then this method will return true
     *
     * @param c the creature to check
     * @return whether the creature meets the role prereqs
     */

    public boolean meetsRolePrereqs(Creature c)
    {
        if (rolePrereqs.size() == 0) return true;

        for (int i = 0; i < rolePrereqs.size(); i++) {
            Role cc = Game.ruleset.getRole(rolePrereqs.get(i));
            int level = roleLevelPrereqs.get(i);

            if (cc != null) {
                if (c.roles.getLevel(cc) >= level) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns true if and only if the specified creature meets none of the prereqs
     * in this prereq list.  See {@link #meetsPrereqs(Creature)}
     *
     * @param c
     * @return true if and only if the creature meets no prereqs
     */

    public boolean meetsRestrictions(Creature c)
    {
        for (Stat stat : statPrereqs.keySet()) {
            if (meetsStatPrereq(c, stat)) return false;
        }

        for (String skillID : skillPrereqs.keySet()) {
            if (meetsSkillPrereq(c, skillID)) return false;
        }

        if (meetsRolePrereqs(c) && rolePrereqs.size() > 0) return false;

        for (String abilityID : abilityPrereqs) {
            if (meetsAbilityPrereq(c, abilityID)) return false;
        }

        for (String baseWeapon : weaponProficiencyPrereqs) {
            if (meetsWeaponProficiencyPrereq(c, baseWeapon)) return false;
        }

        for (String armorType : armorProficiencyPrereqs) {
            if (meetsArmorProficiencyPrereq(c, armorType)) return false;
        }

        return true;
    }

    /**
     * Returns true if and only if the specified Creature meets all prereqs in this
     * prereq list.  This includes having at least the specified ranks in each
     * skill prereq, possessing each required ability, having at least the specified
     * stat in all stat prereqs, having all weapon and armor proficiencies, and
     * having at least the required number of levels in at least one of the role
     * prereqs.
     * <p>
     * Note that while all other prereqs are implemented as an "AND" condition (meaning
     * all individual prereqs must be met), the role prereq is implemented as an "OR"
     * condition, meaning only one of possibly several role prereqs must be met.  All other
     * types of prereqs must still be met in this case.
     *
     * @param c the creature to check prereqs against
     * @return true if and only if the creature meets all prereqs.
     */

    public boolean meetsPrereqs(Creature c)
    {
        for (Stat stat : statPrereqs.keySet()) {
            if (!meetsStatPrereq(c, stat)) return false;
        }

        for (String skillID : skillPrereqs.keySet()) {
            if (!meetsSkillPrereq(c, skillID)) return false;
        }

        if (!meetsRolePrereqs(c)) return false;

        for (String abilityID : abilityPrereqs) {
            if (!meetsAbilityPrereq(c, abilityID)) return false;
        }

        for (String baseWeapon : weaponProficiencyPrereqs) {
            if (!meetsWeaponProficiencyPrereq(c, baseWeapon)) return false;
        }

        for (String armorType : armorProficiencyPrereqs) {
            if (!meetsArmorProficiencyPrereq(c, armorType)) return false;
        }

        return true;
    }

    private boolean meetsStatPrereq(Creature c, Stat stat)
    {
        return c.stats.get(stat) >= statPrereqs.get(stat);
    }

    private boolean meetsSkillPrereq(Creature c, String skillID)
    {
        return c.skills.getRanks(skillID) >= skillPrereqs.get(skillID);
    }

    private boolean meetsAbilityPrereq(Creature c, String abilityID)
    {
        return c.abilities.has(abilityID);
    }

    private boolean meetsWeaponProficiencyPrereq(Creature c, String baseWeapon)
    {
        return c.stats.hasWeaponProficiency(baseWeapon);
    }

    private boolean meetsArmorProficiencyPrereq(Creature c, String armorType)
    {
        return c.stats.hasArmorProficiency(armorType);
    }

    /**
     * Returns true if and only if this PrereqList is empty, meaning
     * that all Creatures will meet the PrereqList and that no
     * Prereqs have been added.
     *
     * @return true if and only if this PrereqList is empty
     */

    public boolean isEmpty()
    {
        if (!weaponProficiencyPrereqs.isEmpty()) return false;
        if (!armorProficiencyPrereqs.isEmpty()) return false;
        if (!abilityPrereqs.isEmpty()) return false;
        if (!rolePrereqs.isEmpty()) return false;
        if (!statPrereqs.isEmpty()) return false;

        return (skillPrereqs.size() == 0);
    }

    private void appendMetOrNotMet(Creature c, boolean isMet, StringBuilder sb)
    {
        sb.append("<tr><td style=\"width: 10ex; text-align: center; vertical-align: middle\">");
        if (c == null) {
            // don't show anything
        } else
            if (isMet) {
                sb.append("<span style=\"font-family: green;\">Met </span>");
            } else {
                sb.append("<span style=\"font-family: red;\">Not Met </span>");
            }
        sb.append("</td><td>");
    }

    /**
     * Appends a String HTML description of this List of Prereqs to the
     * specified StringBuilder.  This will include a mention of all
     * required prereqs for this list to be met by a Creature.
     *
     * @param sb    the StringBuilder to append to
     * @param c     the creature to compare to or null to not show whether prereqs are met
     * @param title the title string to display, i.e. "Prerequisites"
     */

    public void appendDescription(StringBuilder sb, Creature c, String title)
    {
        if (isEmpty()) return;

        sb.append("<div style=\"font-family: medium-bold-blue; margin-top : 1em;\">");
        sb.append(title);
        sb.append("</div>");

        sb.append("<table>");
        for (Stat stat : statPrereqs.keySet()) {
            appendMetOrNotMet(c, c != null && meetsStatPrereq(c, stat), sb);

            sb.append("<span style=\"font-family: purple;\">").append(stat.name);
            sb.append("</span> ").append(statPrereqs.get(stat)).append("</td></tr>");
        }

        for (String skillID : skillPrereqs.keySet()) {
            Skill skill = Game.ruleset.getSkill(skillID);

            if (skill == null) {
                throw new NullPointerException("Skill " + skillID + " not found in prereq list.");
            }

            appendMetOrNotMet(c, c != null && meetsSkillPrereq(c, skillID), sb);

            sb.append(skillPrereqs.get(skillID));
            sb.append(" ranks in <span style=\"font-family: blue;\">");
            sb.append(skill.getNoun()).append("</span></td></tr>");
        }

        if (rolePrereqs.size() > 0) {
            appendMetOrNotMet(c, c != null && meetsRolePrereqs(c), sb);
        }

        for (int i = 0; i < rolePrereqs.size(); i++) {
            Role role = Game.ruleset.getRole(rolePrereqs.get(i));

            if (role == null) {
                throw new NullPointerException("Role " + rolePrereqs.get(i) + " not found in prereq list");
            }

            sb.append("Level <span style=\"font-family: red;\">");
            sb.append(roleLevelPrereqs.get(i)).append("</span> in ");
            sb.append("<span style=\"font-family: blue;\">");
            sb.append(role.getName()).append("</span>");

            if (i != rolePrereqs.size() - 1) {
                sb.append(" OR ");
            } else {
                sb.append("</td></tr>");
            }
        }

        for (String abilityID : abilityPrereqs) {
            Ability ability = Game.ruleset.getAbility(abilityID);

            if (ability == null) {
                throw new NullPointerException("Ability " + abilityID + " not found in prereq list.");
            }

            appendMetOrNotMet(c, c != null && meetsAbilityPrereq(c, abilityID), sb);

            sb.append("Ability: ");
            sb.append("<span style=\"font-family: orange;\">");
            sb.append(ability.getName()).append("</span></td></tr>");
        }

        for (String s : weaponProficiencyPrereqs) {
            appendMetOrNotMet(c, c != null && meetsWeaponProficiencyPrereq(c, s), sb);

            sb.append("Weapon Proficiency: ");
            sb.append("<span style=\"font-family: red;\">").append(s).append("</span></td></tr>");
        }

        for (String s : armorProficiencyPrereqs) {
            appendMetOrNotMet(c, c != null && meetsArmorProficiencyPrereq(c, s), sb);

            sb.append("Armor Proficiency: ");
            sb.append("<span style=\"font-family: green;\">").append(s).append("</span></td></tr>");
        }

        sb.append("</table>");
    }

    private enum PrereqType
    {
        skills(new SkillParser()),
        abilities(new AbilityParser()),
        roles(new RoleParser()),
        stats(new StatParser()),
        weapons(new WeaponParser()),
        armor(new ArmorParser());

        private PrereqParser parser;

        private PrereqType(PrereqParser parser)
        {
            this.parser = parser;
        }

        private void parse(SimpleJSONArray data, PrereqList prereqList)
        {
            for (SimpleJSONArrayEntry entry : data) {
                parser.parse(entry, prereqList);
            }
        }
    }

    private interface PrereqParser
    {
        public void parse(SimpleJSONArrayEntry entry, PrereqList prereqList);
    }

    private static class ArmorParser implements PrereqParser
    {
        @Override
        public void parse(SimpleJSONArrayEntry entry, PrereqList prereqList)
        {
            prereqList.armorProficiencyPrereqs.add(entry.getString());
        }
    }

    private static class WeaponParser implements PrereqParser
    {
        @Override
        public void parse(SimpleJSONArrayEntry entry, PrereqList prereqList)
        {
            prereqList.weaponProficiencyPrereqs.add(entry.getString());
        }
    }

    private static class StatParser implements PrereqParser
    {
        @Override
        public void parse(SimpleJSONArrayEntry entry, PrereqList prereqList)
        {
            SimpleJSONObject in = entry.getObject();

            Stat stat = Stat.valueOf(in.get("type", null));
            prereqList.statPrereqs.put(stat, in.get("value", 0));
        }
    }

    private static class RoleParser implements PrereqParser
    {
        @Override
        public void parse(SimpleJSONArrayEntry entry, PrereqList prereqList)
        {
            SimpleJSONObject in = entry.getObject();

            prereqList.rolePrereqs.add(in.get("id", null));
            prereqList.roleLevelPrereqs.add(in.get("level", 0));
        }
    }

    private static class AbilityParser implements PrereqParser
    {
        @Override
        public void parse(SimpleJSONArrayEntry entry, PrereqList prereqList)
        {
            prereqList.abilityPrereqs.add(entry.getString());
        }
    }

    private static class SkillParser implements PrereqParser
    {
        @Override
        public void parse(SimpleJSONArrayEntry entry, PrereqList prereqList)
        {
            SimpleJSONObject in = entry.getObject();
            prereqList.skillPrereqs.put(in.get("id", null), in.get("ranks", 0));
        }
    }
}
