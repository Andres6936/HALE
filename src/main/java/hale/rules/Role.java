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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hale.Game;
import hale.ability.Ability;
import hale.ability.AbilitySelectionList;
import hale.bonus.Stat;
import hale.entity.Creature;
import hale.icon.Icon;
import hale.icon.IconFactory;
import hale.resource.ResourceManager;
import hale.resource.ResourceType;
import hale.util.SimpleJSONArray;
import hale.util.SimpleJSONArrayEntry;
import hale.util.SimpleJSONObject;

/**
 * A class representing the character archetype or class of a creature.  This defines
 * many of the basic stats, such as attack, damage, hit points, and skills.
 *
 * @author Jared
 */

public class Role
{
    private final String id;
    private final String name;

    private final Icon icon;

    private final int skillPointsPerLevel;
    private final int hpPerLevel;
    private final int hpAtLevelOne;
    private final int attackBonusPerLevel;
    private final int damageBonusPerLevel;

    private final int maximumLevel;

    private final Stat spellCastingAttribute;
    private final int spellFailureBase;
    private final int spellFailureSpellLevelFactor;
    private final int spellFailureAbilityScoreFactor;
    private final int spellFailureCasterLevelFactor;

    private final int maximumSpellLevel;

    private final boolean isPlayer;
    private final boolean isBase;

    private final Map<Stat, Integer> defaultPlayerAttributes;
    private final List<String> defaultPlayerSkillSelections;

    private final PrereqList prereqs;

    private final Map<Integer, LevelUpList> levelUpLists;

    /**
     * Creates a new Role by parsing the specified JSON data
     *
     * @param id
     * @param data
     */

    public Role(String id, SimpleJSONObject data)
    {
        this.id = id;

        name = data.get("name", id);

        if (data.containsKey("icon")) {
            icon = IconFactory.createIcon(data.getObject("icon"));
        } else {
            icon = IconFactory.emptyIcon;
        }

        isPlayer = data.get("isPlayer", false);
        isBase = data.get("isBase", false);

        SimpleJSONObject statsIn = data.getObject("stats");

        damageBonusPerLevel = statsIn.get("damageBonusPerLevel", 0);
        attackBonusPerLevel = statsIn.get("attackBonusPerLevel", 0);
        skillPointsPerLevel = statsIn.get("skillPointsPerLevel", 0);
        hpPerLevel = statsIn.get("hpPerLevel", 0);

        if (statsIn.containsKey("hpAtLevelOne")) {
            hpAtLevelOne = statsIn.get("hpAtLevelOne", 0);
        } else {
            hpAtLevelOne = hpPerLevel;
        }

        if (statsIn.containsKey("maximumLevel")) {
            maximumLevel = statsIn.get("maximumLevel", 0);
        } else {
            maximumLevel = Integer.MAX_VALUE;
        }

        if (statsIn.containsKey("maximumSpellLevel")) {
            maximumSpellLevel = statsIn.get("maximumSpellLevel", 0);
        } else {
            maximumSpellLevel = 0;
        }

        if (statsIn.containsKey("spellCastingAttribute")) {
            spellCastingAttribute = Stat.valueOf(statsIn.get("spellCastingAttribute", null));
            spellFailureBase = statsIn.get("spellFailureBase", 0);
            spellFailureSpellLevelFactor = statsIn.get("spellFailureSpellLevelFactor", 0);
            spellFailureAbilityScoreFactor = statsIn.get("spellFailureAbilityScoreFactor", 0);
            spellFailureCasterLevelFactor = statsIn.get("spellFailureCasterLevelFactor", 0);
        } else {
            spellCastingAttribute = null;
            spellFailureBase = 0;
            spellFailureSpellLevelFactor = 0;
            spellFailureAbilityScoreFactor = 0;
            spellFailureCasterLevelFactor = 0;
        }

        defaultPlayerAttributes = new HashMap<Stat, Integer>();
        defaultPlayerSkillSelections = new ArrayList<String>(5);
        if (data.containsKey("defaultSelections")) {
            SimpleJSONObject selectionsIn = data.getObject("defaultSelections");

            for (String key : selectionsIn.keySet()) {
                if (key.equals("skills")) {
                    SimpleJSONArray skillsIn = selectionsIn.getArray(key);
                    for (SimpleJSONArrayEntry entry : skillsIn) {
                        defaultPlayerSkillSelections.add(entry.getString());
                    }
                } else {
                    Stat stat = Stat.valueOf(key);
                    int value = selectionsIn.get(key, 0);

                    defaultPlayerAttributes.put(stat, value);
                }
            }
        }

        if (data.containsKey("prereqs")) {
            prereqs = new PrereqList(data.getObject("prereqs"));
        } else {
            prereqs = new PrereqList();
        }

        levelUpLists = new HashMap<Integer, LevelUpList>();

        if (data.containsKey("casterLevelsAdded")) {
            SimpleJSONArray casterLevelsIn = data.getArray("casterLevelsAdded");

            for (SimpleJSONArrayEntry entry : casterLevelsIn) {
                int level = entry.getInt(0);

                LevelUpList levelUpList = getLevelUpList(level);
                levelUpList.casterLevelAdded++;
            }
        }

        if (data.containsKey("abilitiesAdded")) {
            SimpleJSONObject abilitiesIn = data.getObject("abilitiesAdded");

            for (String abilityID : abilitiesIn.keySet()) {
                int level = abilitiesIn.get(abilityID, 0);

                // verify the ability exists
                Ability ability = Game.ruleset.getAbility(abilityID);
                if (ability == null) {
                    throw new IllegalArgumentException("Ability ID \"" + abilityID +
                            "\" not found while reading " + id);
                }

                LevelUpList levelUpList = getLevelUpList(level);
                levelUpList.abilities.add(abilityID);
            }
        }

        if (data.containsKey("abilitySelectionsFromList")) {
            SimpleJSONObject selectionsIn = data.getObject("abilitySelectionsFromList");

            for (String listID : selectionsIn.keySet()) {
                AbilitySelectionList list = Game.ruleset.getAbilitySelectionList(listID);
                if (list == null) {
                    throw new IllegalArgumentException("Ability Selection List \"" + listID +
                            "\" not found while reading " + id);
                }

                SimpleJSONArray listLevelsIn = selectionsIn.getArray(listID);
                for (SimpleJSONArrayEntry entry : listLevelsIn) {
                    LevelUpList levelUpList = getLevelUpList(entry.getInt(0));
                    levelUpList.abilitySelectionLists.add(listID);
                }
            }
        }
    }

    /**
     * Used when parsing the input data
     *
     * @param level the level for the list
     * @return the levelUpList for the specified level.  If the list does not exist, it is created and added
     * to the level up lists map
     */

    private LevelUpList getLevelUpList(int level)
    {
        LevelUpList list = levelUpLists.get(level);
        if (list == null) {
            list = new LevelUpList();
            levelUpLists.put(level, list);
        }

        return list;
    }

    /**
     * Gets the list of default player skill selections, in order.
     * Note that this list is unmodifiable
     *
     * @return the list of default player skill selections
     */

    public List<String> getDefaultPlayerSkillSelections()
    {
        return Collections.unmodifiableList(defaultPlayerSkillSelections);
    }

    /**
     * Returns the default player point selection for the specified attribute
     *
     * @param stat the attribute (strength, dexterity, constitution, intelligence, wisdom, or charisma
     * @return the default player point selection
     */

    public int getDefaultPlayerAttributeSelection(Stat stat)
    {
        Integer value = this.defaultPlayerAttributes.get(stat);

        if (value == null) {
            return 0;
        } else {
            return value.intValue();
        }
    }

    /**
     * Returns the number of hit points that this role adds at level one
     *
     * @return the level one hit points
     */

    public int getHPAtLevelOne()
    {
        return hpAtLevelOne;
    }

    /**
     * Gets the HTML description for this role
     *
     * @return the HTML description
     */

    public String getDescription()
    {
        return ResourceManager.getResourceAsString("descriptions/roles/" +
                id + ResourceType.HTML.getExtension());
    }

    /**
     * Returns the spell casting attribute, one of {@link Stat#Str}, {@link Stat#Dex}, {@link Stat#Con},
     * {@link Stat#Int}, {@link Stat#Wis}, or {@link Stat#Cha}, or null if this role does not
     * cast spells
     *
     * @return the spell casting attribute
     */

    public Stat getSpellCastingAttribute()
    {
        return spellCastingAttribute;
    }

    /**
     * Gets the base spell failure value, used in computing spell failure
     *
     * @return the base spell failure value
     */

    public int getSpellFailureBase()
    {
        return spellFailureBase;
    }

    /**
     * Gets the factor that the spell level is multiplied by when computing spell failure
     *
     * @return the spell level spell failure factor
     */

    public int getSpellFailureSpellLevelFactor()
    {
        return spellFailureSpellLevelFactor;
    }

    /**
     * Gets the factor that the ability score is multiplied by when computing spell failure
     *
     * @return the factor that the ability score is multiplied by
     */

    public int getSpellFailureAbilityScoreFactor()
    {
        return spellFailureAbilityScoreFactor;
    }

    /**
     * Gets the factor that the caster level is multiplied by when computing spell failure
     *
     * @return the caster level factor
     */

    public int getSpellFailureCasterLevelFactor()
    {
        return spellFailureCasterLevelFactor;
    }

    /**
     * Returns the maximum level of spells that this role can cast.  For roles that do not
     * cast spells or roles that are not a base role (see {@link #isBase()}, this method
     * should return zero.
     *
     * @return the maximum spell level for this role
     */

    public int getMaximumSpellLevel()
    {
        return maximumSpellLevel;
    }

    /**
     * Returns true if this is a base role (one that can be added at level one), or false otherwise
     *
     * @return whether this is a base role
     */

    public boolean isBase()
    {
        return isBase;
    }

    /**
     * Returns true if this is a role that players can select when leveling up or creating a character,
     * false otherwise
     *
     * @return whether this is a player selectable role
     */

    public boolean isPlayer()
    {
        return isPlayer;
    }

    /**
     * Returns the unique ID string for this role
     *
     * @return the unique ID
     */

    public String getID()
    {
        return id;
    }

    /**
     * Returns the name of this role
     *
     * @return the name
     */

    public String getName()
    {
        return name;
    }

    /**
     * Returns the icon that is used to represent this role.  This will always be non-null,
     * but may be an empty icon
     *
     * @return the icon for this role
     */

    public Icon getIcon()
    {
        return icon;
    }

    /**
     * Returns the number of skill points that creatures adding this role receive per level,
     * in addition to any other bonuses (such as from high intelligence)
     *
     * @return the number of skill points per level
     */

    public int getSkillPointsPerLevel()
    {
        return skillPointsPerLevel;
    }

    /**
     * Returns the number of hit points that creatures adding this role receive per level,
     * in addition to other bonuses (such as from high constitution).  Note that the
     * hp received at level one may be different, see {@link #getHPAtLevelOne()}
     *
     * @return the number of hit points per level
     */

    public int getHPPerLevel()
    {
        return hpPerLevel;
    }

    /**
     * Returns the attack bonus added to the owning creature per level.  This is a flat
     * value that will be added to a 1 - 100 random number
     *
     * @return the attack bonus per level
     */

    public int getAttackBonusPerLevel()
    {
        return attackBonusPerLevel;
    }

    /**
     * Returns the damage bonus added to the owning creature per level.  This is a percentage
     * bonus.
     *
     * @return the damage bonus per level
     */

    public int getDamageBonusPerLevel()
    {
        return damageBonusPerLevel;
    }

    /**
     * A detailed description of this role is appended to the specified StringBuilder
     *
     * @param sb
     * @param c  the creature used for determining if prereqs are met or not, or null to not show
     *           this information
     */

    public void appendDescription(StringBuilder sb, Creature c)
    {
        sb.append("<div style=\"font-family: large-red;\">");
        sb.append(name).append("</div>");

        if (!isBase) {
            sb.append("<div style=\"font-family: medium\">");
            sb.append("Specialization");
            sb.append("</div>");
        }

        if (maximumLevel != Integer.MAX_VALUE) {
            sb.append("<div>");
            sb.append("<span style=\"font-family: red\">");
            sb.append(maximumLevel);
            sb.append("</span>");
            sb.append(" Level Maximum");
            sb.append("</div>");
        }

        prereqs.appendDescription(sb, c, "Prerequisites");

        sb.append("<div style=\"font-family: medium-bold-blue; margin-top: 1em;\">");
        sb.append("Stats");
        sb.append("</div>");
        sb.append("<table>");

        if (isBase) {
            sb.append("<tr><td style=\"text-align:right; width:4ex;\"><span style=\"font-family: blue;\">");
            sb.append(hpAtLevelOne);
            sb.append("</span></td><td style=\"margin-left: 1ex;\"> Hit Points at first level</td></tr>");
        }

        sb.append("<tr style=\"margin-bottom: 1em;\">");
        sb.append("<td style=\"text-align:right; width:4ex;\">+<span style=\"font-family: blue;\">");
        sb.append(hpPerLevel);
        sb.append("</span></td><td style=\"margin-left: 1ex;\">Hit Points at each level</td></tr>");

        sb.append("<tr><td style=\"text-align:right; width:4ex;\">+<span style=\"font-family: blue;\">");
        sb.append(attackBonusPerLevel);
        sb.append("</span></td><td style=\"margin-left: 1ex;\">Attack Bonus per level</td></tr>");

        sb.append("<tr style=\"margin-bottom: 1em\">");
        sb.append("<td style=\"text-align:right; width: 4ex;\">+<span style=\"font-family: blue;\">");
        sb.append(damageBonusPerLevel);
        sb.append("</span></td><td style=\"margin-left: 1 ex;\">Damage Bonus per level</td></tr>");

        sb.append("<tr><td style=\"text-align:right; width:4ex;\">+<span style=\"font-family: blue;\">");
        sb.append(skillPointsPerLevel);
        sb.append("</span></td><td style=\"margin-left: 1ex;\"> Skill Points per level (before Intelligence modifier)</td></tr>");

        sb.append("</table>");

        sb.append(getDescription());
    }

    /**
     * Returns true if and only if the specified creature meets all prereqs for this
     * role and is capable of adding one level of this role
     *
     * @param creature
     * @return whether the creature can add this role
     */

    public boolean creatureCanSelect(Creature creature)
    {
        if (creature == null) return false;

        if (!prereqs.meetsPrereqs(creature)) return false;

        if (isBase && creature.roles.getBaseRole() != null) {
            if (creature.roles.getBaseRole() != this) return false;
        }

        if (creature.roles.getLevel(this) >= maximumLevel) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if this role is a specialization and the specified creature
     * meets role requirements (but not neccessarily the needed role level, or any other requirements)
     * for this role
     *
     * @param creature the creature to test against
     * @return whether this is a specialization for the specified base role
     */

    public boolean creatureHasRolePrereqs(Creature creature)
    {
        if (this.isBase) return false;

        return prereqs.hasRolePrereqs(creature);
    }

    /**
     * Gets the number of caster levels added for this role at the specified role level
     *
     * @param level the role level
     * @return the number of caster levels
     */

    public int getCasterLevelAddedAtLevel(int level)
    {
        LevelUpList list = levelUpLists.get(level);
        if (list == null) return 0;

        return list.casterLevelAdded;
    }

    /**
     * Returns the List of all Abilities that should be added to a Creature
     * upon gaining the specified level of this Role.  If their are no Abilities
     * to be added, the List will be empty.
     *
     * @param level the level that has been gained
     * @return the List of Abilities to add
     */

    public List<Ability> getAbilitiesAddedAtLevel(int level)
    {
        LevelUpList list = levelUpLists.get(level);
        if (list == null) return Collections.emptyList();

        List<String> abilityIDs = list.abilities;

        List<Ability> abilities = new ArrayList<Ability>(abilityIDs.size());
        for (String id : abilityIDs) {
            abilities.add(Game.ruleset.getAbility(id));
        }

        return abilities;
    }

    /**
     * For each AbilitySelectionList in the returned List, the Creature gaining
     * the level specified of this Role should pick one Ability from that
     * AbilitySelectionList.  If their are no selections to be made, the List
     * will be empty.
     *
     * @param level the role level that has been gained
     * @return the List of AbilitySelectionLists to choose abilities from
     */

    public List<AbilitySelectionList> getAbilitySelectionsAddedAtLevel(int level)
    {
        LevelUpList list = levelUpLists.get(level);
        if (list == null) return Collections.emptyList();

        List<String> listIDs = list.abilitySelectionLists;

        List<AbilitySelectionList> lists = new ArrayList<AbilitySelectionList>(listIDs.size());
        for (String id : listIDs) {
            lists.add(Game.ruleset.getAbilitySelectionList(id));
        }

        return lists;
    }

    /**
     * Returns a set containing all AbilitySelectionLists that are referenced at any
     * level within this Role
     *
     * @return the set of AbilitySelectionLists
     */

    public Set<AbilitySelectionList> getAllReferencedAbilitySelectionLists()
    {
        Set<AbilitySelectionList> lists = new LinkedHashSet<AbilitySelectionList>();

        for (LevelUpList levelUpList : levelUpLists.values()) {
            for (String id : levelUpList.abilitySelectionLists) {
                AbilitySelectionList list = Game.ruleset.getAbilitySelectionList(id);
                lists.add(list);
            }
        }

        return lists;
    }

    /**
     * A list containing new abilities, ability slots, and bonuses gained when
     * reaching a given level in this Role
     *
     * @author Jared Stephen
     */

    private class LevelUpList
    {
        private List<String> abilitySelectionLists;
        private List<String> abilities;
        private int casterLevelAdded;

        private LevelUpList()
        {
            this.abilitySelectionLists = new ArrayList<String>(0);
            this.abilities = new ArrayList<String>(0);
            this.casterLevelAdded = 0;
        }
    }
}