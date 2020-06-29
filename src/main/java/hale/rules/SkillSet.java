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

package hale.rules;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import hale.Game;
import hale.bonus.Stat;
import hale.entity.Creature;
import hale.loading.JSONOrderedObject;
import hale.loading.Saveable;
import hale.util.Logger;
import hale.util.SimpleJSONObject;


/**
 * A Set of Skills and a corresponding integer number of ranks for each Skill.
 *
 * @author Jared Stephen
 */

public class SkillSet implements Saveable
{
    private final Creature parent;
    private final Map<String, Integer> skills;

    @Override
    public JSONOrderedObject save()
    {
        JSONOrderedObject data = new JSONOrderedObject();

        for (String key : skills.keySet()) {
            int value = skills.get(key);
            if (value != 0) {
                data.put(key, value);
            }
        }

        return data;
    }

    public void load(SimpleJSONObject data)
    {
        clear();

        for (String skillID : data.keySet()) {
            int value = data.get(skillID, 0);

            addRanks(skillID, value);
        }
    }

    /**
     * Constructs an empty skill list with no ranks in any skill.
     *
     * @param parent the parent creature
     */

    public SkillSet(Creature parent)
    {
        this.skills = new LinkedHashMap<String, Integer>();
        this.parent = parent;
    }

    /**
     * Constructs a SkillSet containing the same skills and ranks as the
     * supplied SkillSet.
     *
     * @param other  The SkillSet to copy the skills and ranks from.
     * @param parent the new parent for this skillset
     */

    public SkillSet(SkillSet other, Creature parent)
    {
        this(parent);
        for (String skillID : other.skills.keySet()) {
            this.skills.put(skillID, other.skills.get(skillID));
        }
    }

    /**
     * Returns true if and only if the number of ranks for all skills in this
     * skillset equals the number of ranks for all skills in the specified skillset
     *
     * @param other the SkillSet to compare this one to
     * @return true if and only if the SkillSets are equal
     */

    public boolean equals(SkillSet other)
    {
        if (other == null) return false;

        for (String skillID : skills.keySet()) {
            if (!skills.get(skillID).equals(other.skills.get(skillID))) {
                return false;
            }
        }

        for (String skillID : other.skills.keySet()) {
            if (!other.skills.get(skillID).equals(skills.get(skillID))) {
                return false;
            }
        }

        return true;
    }

    /**
     * For each Skill in the supplied SkillSet, the ranks associated with that Skill
     * are added to the Skill in this SkillSet.
     *
     * @param other The SkillSet to add ranks from.
     */

    public void addRanksFromList(SkillSet other)
    {
        if (other == null) return;

        for (String skillID : other.skills.keySet()) {
            addRanks(skillID, other.skills.get(skillID));
        }
    }

    /**
     * Returns the number of skills with ranks in this SkillSet
     *
     * @return the number of skills in this SkillSet
     */

    public int size()
    {
        return skills.size();
    }

    /**
     * Add the specified number of ranks to the specified skill.
     *
     * @param skillID The ID String for the {@link main.java.hale.rules.Skill}
     * @param ranks   The number of ranks to be added.
     */

    public void addRanks(String skillID, int ranks)
    {
        if (Game.ruleset.getSkill(skillID) == null) {
            Logger.appendToErrorLog("Skill ID: " + skillID + " not found");
        } else {
            int curRanks = getRanks(skillID);

            skills.put(skillID, Integer.valueOf(curRanks + ranks));

            parent.updateListeners();
        }
    }

    /**
     * Adds the specified number of ranks to the specified Skill.
     *
     * @param skill The Skill to have ranks added
     * @param ranks The number of ranks to add.
     */

    public void addRanks(Skill skill, int ranks)
    {
        addRanks(skill.getID(), ranks);
    }

    /**
     * Returns true if and only if this SkillSet contains a non zero
     * number of ranks for the specified Skill.
     *
     * @param skill the Skill to check
     * @return true if and only if this SkillSet contains ranks for the
     * specified Skill
     */

    public boolean hasRanks(Skill skill)
    {
        return hasRanks(skill.getID());
    }

    /**
     * Returns true if and only if this SkillSet contains a non zero
     * number of ranks for the Skill with the specified ID.
     *
     * @param skillID the ID of the Skill to check
     * @return true if and only if this SkillSet contains ranks for the
     * specified Skill
     */

    public boolean hasRanks(String skillID)
    {
        Integer ranks = this.skills.get(skillID);
        if (ranks == null) return false;

        return (ranks.intValue() != 0);
    }

    /**
     * Returns the number of ranks for the Skill with the specified ID.  If the
     * Skill has never had ranks added to this SkillSet, returns 0.
     *
     * @param skillID the ID String of the Skill.
     * @return the number of ranks for the Skill with the specified I
     */

    public int getRanks(String skillID)
    {
        Integer ranks = this.skills.get(skillID);
        if (ranks == null) return 0;

        return ranks.intValue();
    }

    /**
     * Returns the number of ranks for the specified SKill.  If the
     * Skill has never had ranks added to this SkillSet, returns 0.
     *
     * @param skill the Skill to get ranks for
     * @return the number of ranks for the Skill with the specified I
     */

    public int getRanks(Skill skill)
    {
        return getRanks(skill.getID());
    }

    /**
     * Set the ranks of all Skills in this Set to 0.
     */

    public void clear()
    {
        skills.clear();
    }

    /**
     * Returns the set of Skill IDs that have been added to this SkillSet
     * via addRanks, setRanks, etc.  It is possible that some of these
     * Skills will have zero ranks.  The set is unmodifiable
     *
     * @return the set of Skill IDs that have been added to this SkillSet.
     */

    public Set<String> getSkills()
    {
        return Collections.unmodifiableSet(skills.keySet());
    }

    /**
     * Returns the parent creature's modifier for the specified skill.  For most
     * skills, a random roll from 1 to 100 is then added to this value for a skill check.
     * Sometimes, however, skill modifiers may be compared directly with no random
     * element
     *
     * @param skill
     * @return the parent creature's skill modifier for the specified skill
     */

    public int getTotalModifier(Skill skill)
    {
        int ranks = getRanks(skill.getID());
        int modifier = parent.stats.getSkillBonus(skill.getID()) + (parent.stats.get(skill.getKeyAttribute()) - 10) * 2;
        if (skill.suffersArmorPenalty()) modifier -= parent.stats.get(Stat.ArmorPenalty);

        return ranks + modifier;
    }

    /**
     * Returns the parent creature's modifier for the specified skill.  For most
     * skills, a random roll from 1 to 100 is then added to this value for a skill check.
     * Sometimes, however, skill modifiers may be compared directly with no random
     * element
     *
     * @param skillID the ID of the skill
     * @return the parent creature's skill modifier for the specified skill
     */

    public int getTotalModifier(String skillID)
    {
        return getTotalModifier(Game.ruleset.getSkill(skillID));
    }

    /**
     * The parent creature performs a skill check with the specified difficulty.
     * The parent's total modifier plus a random roll of 1 to 100 is compared
     * against the difficulty.  Some skills require training, which means the
     * parent must have non zero ranks in order for this check to succeed.  Some
     * skills allow a guaranteed roll of 100 while not in combat.  When using this method
     * no message is appended to the mainviewer regarding the check
     *
     * @param skillID    the skill to check
     * @param difficulty the difficulty of the check.
     * @return true if the check succeeds, false otherwise
     */

    public boolean performCheck(String skillID, int difficulty)
    {
        return getCheck(skillID, difficulty, null) >= difficulty;
    }

    /**
     * Gets a skill check for the specified skill.
     * The parent's total modifier plus a random roll of 1 to 100 is generally used.
     * Some skills require training, which means the
     * parent must have non zero ranks in order for this check to be non-zero.  Some
     * skills allow a guaranteed roll of 100 while not in combat.
     * <p>
     * A message is append to the mainviewer about the results of this check
     *
     * @param skillID    the skill to check
     * @param difficulty the difficulty of the check.  Used in building the message
     *                   describing this check
     * @return the value of the check
     */

    public int getCheck(String skillID, int difficulty)
    {
        StringBuilder sb = new StringBuilder();

        int check = getCheck(skillID, difficulty, sb);

        if (check != 0) {
            Game.mainViewer.addMessage("orange", sb.toString());
        }

        return check;
    }

    /**
     * Gets a check without showing a message
     *
     * @param skillID
     * @param difficulty
     * @param sb         the message to append to, or null for no message
     * @return the skill check
     */

    private int getCheck(String skillID, int difficulty, StringBuilder sb)
    {
        Skill skill = Game.ruleset.getSkill(skillID);

        // if skill requires training, the parent must have ranks
        if (!skill.isUsableUntrained() && getRanks(skill) < 1) {
            return 0;
        }

        int modifier = getTotalModifier(skill);
        int roll = (!Game.isInTurnMode() && skill.alwaysRolls100OutsideOfCombat()) ? 100 : Game.dice.d100();
        int total = modifier + roll;

        if (sb != null) {
            sb.append(skill.getNoun());
            sb.append(" Check: ");

            sb.append(modifier);
            sb.append(" + ");
            sb.append(roll);
            sb.append(" = ");
            sb.append(total);
            sb.append(" vs ");
            sb.append(difficulty);
            sb.append(" : ");

            if (total >= difficulty) {
                sb.append("Success.");
            } else {
                sb.append("Failure.");
            }
        }

        return total;
    }
}
