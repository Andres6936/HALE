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

package main.java.hale.characterbuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.matthiasmann.twl.Color;

import main.java.hale.Game;
import main.java.hale.ability.Ability;
import main.java.hale.entity.EntityManager;
import main.java.hale.entity.EquippableItem;
import main.java.hale.entity.Item;
import main.java.hale.entity.PC;
import main.java.hale.entity.PCTemplate;
import main.java.hale.icon.ComposedCreatureIcon;
import main.java.hale.icon.SubIcon;
import main.java.hale.rules.Race;
import main.java.hale.rules.Role;
import main.java.hale.rules.Ruleset;
import main.java.hale.rules.Skill;
import main.java.hale.rules.SkillSet;

/**
 * A class for containing a Creature and managing the valid selections
 * for that Creature when being edited by the CharacterBuilder.
 *
 * @author Jared Stephen
 */

public class Buildable
{
    private PC creature;

    private Race selectedRace;

    private Role selectedRole;

    private int[] selectedAttributes;

    private SkillSet selectedSkills;
    private int selectedUnspentSkillPoints;

    private List<Ability> selectedAbilities;

    private String selectedName;
    private Ruleset.Gender selectedGender;
    private String selectedHairIcon;
    private Color selectedHairColor;
    private String selectedPortrait;
    private String selectedBeardIcon;
    private Color selectedBeardColor;
    private Color selectedSkinColor;
    private Color selectedClothingColor;

    private List<Race> selectableRaces;

    private final boolean newCharacter;

    /**
     * Creates a new Buildable with an empty, new creature.  This is used
     * when creating a new character from scratch.
     */

    public Buildable()
    {
        PCTemplate template = new PCTemplate("temp", "temp", null, Ruleset.Gender.Male, null, null);
        creature = new PC(template);

        selectableRaces = new ArrayList<Race>();
        for (Race race : Game.ruleset.getAllRaces()) {
            if (race.isPlayerSelectable()) selectableRaces.add(race);
        }

        selectedAbilities = new ArrayList<Ability>();

        newCharacter = true;
    }

    /**
     * Creates a new Buildable with the specified creature to edit.  This is
     * used when leveling up a creature.
     *
     * @param creature the creature to edit
     */

    public Buildable(PC creature)
    {
        this.creature = creature;

        newCharacter = false;

        selectableRaces = new ArrayList<Race>();
        selectableRaces.add(creature.getTemplate().getRace());

        selectedAbilities = new ArrayList<Ability>();
    }

    /**
     * Returns true if this Buildable contains a new character, false if it
     * contains a character that already has at least one level
     *
     * @return true if and only if this Buildable contains a new character
     */

    public boolean isNewCharacter()
    {
        return newCharacter;
    }

    /**
     * Returns the currently selected race for this Buildable, or null if no
     * Race has been selected yet
     *
     * @return the selected race for this Buildable
     */

    public Race getSelectedRace()
    {
        return selectedRace;
    }

    /**
     * Sets the selected race for this Buildable to the specified race.  If this
     * race is not the currently selected race, then all other selectables which
     * depend on race (role, attributes, skills, abilities, cosmetic) will be cleared.
     *
     * @param race the race to select for this Buildable
     */

    public void setSelectedRace(Race race)
    {
        if (race != selectedRace) {
            selectedRole = null;
            selectedAttributes = null;
            selectedSkills = null;
            selectedUnspentSkillPoints = 0;
            selectedAbilities.clear();
            selectedName = null;
            selectedGender = null;
            selectedHairIcon = null;
            selectedHairColor = null;
            selectedPortrait = null;
            selectedBeardIcon = null;
            selectedBeardColor = null;
            selectedSkinColor = null;
            selectedClothingColor = null;
        }

        this.selectedRace = race;
    }

    /**
     * Sets the selected set of skill points for this Buildable to the
     * ranks in the specified set.  If the set of skills or the number of points
     * unspent is changed from the previously set selected skills, then all
     * selectables dependant on skills (abilities and cosmetic) will be cleared.
     *
     * @param skills        the set of skill ranks that have been added to this Buildable
     * @param pointsUnspent the number of skill points left over that will be
     *                      added at the next level up
     */

    public void setSelectedSkills(SkillSet skills, int pointsUnspent)
    {
        if (selectedUnspentSkillPoints != pointsUnspent || !skills.equals(selectedSkills)) {
            selectedAbilities.clear();
            selectedName = null;
            selectedGender = null;
            selectedHairIcon = null;
            selectedHairColor = null;
            selectedPortrait = null;
            selectedBeardIcon = null;
            selectedBeardColor = null;
            selectedSkinColor = null;
            selectedClothingColor = null;
        }

        this.selectedSkills = skills;
        this.selectedUnspentSkillPoints = pointsUnspent;
    }

    /**
     * Returns the set of selected skill ranks for this Buildable
     *
     * @return the set of selected skill ranks
     */

    public SkillSet getSelectedSkills()
    {
        return this.selectedSkills;
    }

    /**
     * Returns the number of skill points that were left unspent when
     * selecting skills via the CharacterBuilder
     *
     * @return the number of skill points left unspent
     */

    public int getSelectedUnspentSkillPoints()
    {
        return this.selectedUnspentSkillPoints;
    }

    /**
     * Sets the attributes (Str, Dex...) selected for this Buildable to
     * the specified array of attributes.  The array length must be 6 or
     * the method will throw an IllegalArgumentException.  If the specified
     * list of Attributes does not equal the current selected attributes, then
     * all dependant selectables (skills, abilities, cosmetic) will be cleared.
     *
     * @param attributes the attributes to select
     */

    public void setSelectedAttributes(int[] attributes)
    {
        if (attributes == null || attributes.length != 6) {
            throw new IllegalArgumentException("Attributes must be an array of length 6.");
        }

        boolean attributesChanged = false;
        if (selectedAttributes == null) {
            attributesChanged = true;
        } else {
            for (int i = 0; i < attributes.length; i++) {
                if (attributes[i] != selectedAttributes[i]) {
                    attributesChanged = true;
                    break;
                }
            }
        }

        if (attributesChanged) {
            selectedSkills = null;
            selectedUnspentSkillPoints = 0;
            selectedAbilities.clear();
            selectedName = null;
            selectedGender = null;
            selectedHairIcon = null;
            selectedHairColor = null;
            selectedPortrait = null;
            selectedBeardIcon = null;
            selectedBeardColor = null;
            selectedSkinColor = null;
            selectedClothingColor = null;
        }

        this.selectedAttributes = attributes;
    }

    /**
     * Returns an array of length 6 specifying the attributes (Str, Dex...) currently
     * selected for this Buildable character
     *
     * @return the array of attributes
     */

    public int[] getSelectedAttributes()
    {
        return selectedAttributes;
    }

    /**
     * Returns the selected Role for this Buildable
     *
     * @return the selected Role for this Buildable
     */

    public Role getSelectedRole()
    {
        return selectedRole;
    }

    /**
     * Sets the selected role for this Buildable to the specified Role.  If this
     * Role is different than the currently selected role, all dependent selectables
     * (attributes, skills, abilities, cosmetic) will be cleared
     *
     * @param role the role to select
     */

    public void setSelectedRole(Role role)
    {
        if (role != selectedRole) {
            selectedAttributes = null;
            selectedSkills = null;
            selectedUnspentSkillPoints = 0;
            selectedAbilities.clear();
            selectedName = null;
            selectedGender = null;
            selectedHairIcon = null;
            selectedHairColor = null;
            selectedPortrait = null;
            selectedBeardIcon = null;
            selectedBeardColor = null;
            selectedSkinColor = null;
            selectedClothingColor = null;
        }

        this.selectedRole = role;
    }

    /**
     * Adds the specified ability to the list of Abilities being selected at this level.
     * Once being added, this Ability will count towards prereqs if additional Abilities
     * are being selected at this level.
     *
     * @param ability the ability to select
     */

    public void addSelectedAbility(Ability ability)
    {
        this.selectedAbilities.add(ability);
    }

    /**
     * Removes all Abilities from the list of Abilities selected this level.  Also
     * clears dependent selectables (cosmetics)
     */

    public void clearSelectedAbilities()
    {
        this.selectedAbilities.clear();

        selectedName = null;
        selectedGender = null;
        selectedHairIcon = null;
        selectedHairColor = null;
        selectedPortrait = null;
        selectedBeardIcon = null;
        selectedBeardColor = null;
        selectedSkinColor = null;
        selectedClothingColor = null;
    }

    /**
     * Returns the list of all Abilities that have been selected for this Buildable
     * character.  Returns an empty list in the event that no abilities have been
     * selected.
     *
     * @return the list of selected Abilities
     */

    public List<Ability> getSelectedAbilities()
    {
        return selectedAbilities;
    }

    /**
     * Sets the selected name for this Buildable character.
     *
     * @param selectedName the selected name for this Buildable
     */

    public void setSelectedName(String selectedName)
    {
        this.selectedName = selectedName;
    }

    /**
     * Returns the name that has been selected for this Buildable character.  Returns
     * null if no name has been selected
     *
     * @return the name selected for this Buildable
     */

    public String getSelectedName()
    {
        return selectedName;
    }

    /**
     * Sets the selected gender for this Buildable character.  Dependant selectables
     * (hair color, hair icon, portrait) will be cleared
     *
     * @param gender the gender for this Buildable character
     */

    public void setSelectedGender(Ruleset.Gender gender)
    {
        this.selectedGender = gender;
    }

    /**
     * Returns the gender that has been selected for this Buildable character.  Returns
     * null if no gender has been selected.
     *
     * @return the gender selected for this Buildable
     */

    public Ruleset.Gender getSelectedGender()
    {
        return selectedGender;
    }

    /**
     * Sets the selected hair icon for this buildable character
     *
     * @param hairIcon the selected hair icon
     */

    public void setSelectedHairIcon(String hairIcon)
    {
        this.selectedHairIcon = hairIcon;
    }

    /**
     * Returns the hair icon that has been selected for this Buildable character.  Returns
     * null if no hair icon has been selected
     *
     * @return the hair icon selected for this Buildable
     */

    public String getSelectedHairIcon()
    {
        return selectedHairIcon;
    }

    /**
     * Sets the selected hair color for this Buildable character
     *
     * @param hairColor the selected hair color
     */

    public void setSelectedHairColor(Color hairColor)
    {
        this.selectedHairColor = hairColor;
    }

    /**
     * Returns the hair color that has been selected for this Buildable character.  Returns
     * null if no hair color has been selected
     *
     * @return the hair color selected for this Buildable
     */

    public Color getSelectedHairColor()
    {
        return selectedHairColor;
    }

    /**
     * Sets the selected beard icon for this buildable character
     *
     * @param beardIcon the selected beard icon
     */

    public void setSelectedBeardIcon(String beardIcon)
    {
        this.selectedBeardIcon = beardIcon;
    }

    /**
     * Returns the beard icon that has been selected for this Buildable character.  Returns
     * null if no beard icon has been selected
     *
     * @return the beard icon selected for this Buildable
     */

    public String getSelectedBeardIcon()
    {
        return selectedBeardIcon;
    }

    /**
     * Sets the selected beard color for this Buildable character
     *
     * @param beardColor the selected beard color
     */

    public void setSelectedBeardColor(Color beardColor)
    {
        this.selectedBeardColor = beardColor;
    }

    /**
     * Returns the beard color that has been selected for this Buildable character.  Returns
     * null if no beard color has been selected
     *
     * @return the beard color selected for this Buildable
     */

    public Color getSelectedBeardColor()
    {
        return selectedBeardColor;
    }

    /**
     * Sets the selected skin color for this buildable character
     *
     * @param color the skin color to set
     */

    public void setSelectedSkinColor(Color color)
    {
        this.selectedSkinColor = color;
    }

    /**
     * Returns the skin color that has been selected for this Buildable character, or null
     * if no skin color has been selected
     *
     * @return the skin color
     */

    public Color getSelectedSkinColor()
    {
        return selectedSkinColor;
    }

    /**
     * Sets the selected clothing color
     *
     * @param color the color to be set to
     */

    public void setSelectedClothingColor(Color color)
    {
        this.selectedClothingColor = color;
    }

    /**
     * Returns the clothing color that has been selected for this character, or null if
     * no clothing color has been selected
     *
     * @return the clothing color
     */

    public Color getSelectedClothingColor()
    {
        return selectedClothingColor;
    }

    /**
     * Sets the selected portrait for this Buildable character
     *
     * @param portrait the selected portrait
     */

    public void setSelectedPortrait(String portrait)
    {
        this.selectedPortrait = portrait;
    }

    /**
     * Returns the portrait that has been selected for this Buildable character.  Returns
     * null if no portrait has been selected.
     *
     * @return the portrait selected for this Buildable
     */

    public String getSelectedPortrait()
    {
        return selectedPortrait;
    }

    /**
     * All current selections will be applied to the base creature being edited
     * by this Buildable.  This method is called when finishing editing an
     * existing character.  It is not called when saving a new character.  In that
     * case, a completed working copy is created and saved.
     */

    protected void applySelectionsToCreature()
    {
        if (selectedRole != null) {
            creature.roles.addLevels(selectedRole, 1);
        }

        if (selectedSkills != null) {
            creature.skills.addRanksFromList(selectedSkills);
            creature.setUnspentSkillPoints(selectedUnspentSkillPoints);
        }

        int level = getCreatureLevel();
        for (Ability ability : selectedAbilities) {
            creature.abilities.add(ability, level - 1);
        }
    }

    /**
     * Returns a Creature that is a copy of the base creature being edited by this Buildable,
     * with all current selections added to the Creature.  This is useful for determining
     * Ability prereqs, for example
     *
     * @return a working copy of the Creature being edited
     */

    public PC getWorkingCopy()
    {

        PCTemplate template;
        if (newCharacter) {
            List<SubIcon> subIcons = new ArrayList<SubIcon>();

            // set up the character's icon
            if (selectedHairIcon != null) {
                SubIcon.Factory factory = new SubIcon.Factory(SubIcon.Type.Hair, selectedRace, selectedGender);
                factory.setPrimaryIcon(selectedHairIcon, selectedHairColor);
                subIcons.add(factory.createSubIcon());
            }

            if (selectedBeardIcon != null) {
                SubIcon.Factory factory = new SubIcon.Factory(SubIcon.Type.Beard, selectedRace, selectedGender);
                factory.setPrimaryIcon(selectedBeardIcon, selectedBeardColor);
                subIcons.add(factory.createSubIcon());
            }

            ComposedCreatureIcon icon = new ComposedCreatureIcon(selectedSkinColor, selectedClothingColor, subIcons);

            // for a new character, create a new template
            template = new PCTemplate(selectedName, selectedName, icon, selectedGender, selectedRace, selectedPortrait);
        } else {
            // for an existing character, use the existing template
            template = creature.getTemplate();
        }

        PC workingCopy = new PC(template);
        // give the PC action points to equip the armor if their gender is set
        workingCopy.timer.reset();

        if (newCharacter) {
            // add the default set of clothing
            if (selectedGender != null) {
                Item clothes = EntityManager.getItem(Game.ruleset.getString("DefaultClothes"));
                workingCopy.inventory.addAndEquip((EquippableItem)clothes);
            }

            if (selectedAttributes != null) {
                workingCopy.stats.setAttributes(selectedAttributes);
            }
        } else {
            // add base roles, skills, etc from creature being worked on
            workingCopy.roles.addLevels(creature.roles);

            workingCopy.skills.addRanksFromList(creature.skills);

            workingCopy.stats.setAttributes(creature.stats.getAttributes());

            workingCopy.abilities.addAll(creature.abilities);
        }

        if (selectedRole != null) {
            workingCopy.roles.addLevels(selectedRole, 1);
        }

        if (selectedSkills != null) {
            workingCopy.skills.addRanksFromList(selectedSkills);
            workingCopy.setUnspentSkillPoints(selectedUnspentSkillPoints);
        }

        int level = getCreatureLevel();
        for (Ability ability : selectedAbilities) {
            workingCopy.abilities.add(ability, level);
        }

        if (newCharacter && selectedRace != null) {
            // can't recompute if race is not yet specified as some stats can't be computed
            workingCopy.resetTime();
        }

        return workingCopy;
    }

    /**
     * Returns a List of all Skills that are currently selectable (ranks can be added) for this
     * Buildable.  This may depend on the currently selected Role.
     *
     * @return a List of all selectable Skills
     */

    public List<Skill> getSelectableSkills()
    {
        List<Skill> skills = new ArrayList<Skill>();

        for (Skill skill : Game.ruleset.getAllSkills()) {
            if (skill.canUse(creature)) {
                skills.add(skill);
                continue;
            }

            if (selectedRole != null && skill.canUse(selectedRole)) {
                skills.add(skill);
                continue;
            }
        }

        // sort skills; restricted to this character's role first, then alphabetically by name
        Collections.sort(skills, new Comparator<Skill>()
        {
            @Override
            public int compare(Skill s1, Skill s2)
            {
                if (s1.isRestrictedToARole() && !s2.isRestrictedToARole()) {
                    return -1;
                } else
                    if (s2.isRestrictedToARole() && !s1.isRestrictedToARole()) {
                        return 1;
                    } else {
                        return s1.getNoun().compareTo(s2.getNoun());
                    }
            }
        });

        return skills;
    }

    /**
     * Returns a List of all selectable Roles for this Buildable.  If this Buildable
     * is wrapping a new Creature, then the List will contain all base player selectable
     * roles.
     *
     * @return a List of all selectable Roles for this Buildable.
     */

    public List<Role> getSelectableRoles()
    {
        List<Role> roles = new ArrayList<Role>();

        for (Role role : Game.ruleset.getAllRoles()) {
            if (!role.isPlayer()) continue;

            if (!role.creatureCanSelect(creature)) continue;

            roles.add(role);
        }

        return roles;
    }

    /**
     * Returns a list of roles which this creature may be able to select in the
     * future, based on its base role, or roles which the creature was previously able
     * to select but can no longer, due to max level.  These roles cannot currently be selected.
     *
     * @return a list of all roles that may be selected in the future based on
     * this creature's base role
     */

    public List<Role> getFutureOrPastSelectableRoles()
    {
        List<Role> roles = new ArrayList<Role>();

        for (Role role : Game.ruleset.getAllRoles()) {
            if (!role.isPlayer()) continue;

            if (role.creatureCanSelect(creature)) continue;

            if (!role.creatureHasRolePrereqs(creature)) continue;

            roles.add(role);
        }

        return roles;
    }

    /**
     * Returns a List of all Races that can be selected for this Buildable.  If the
     * Buildable is wrapping a new Creature, then this will be all player selectable races.
     * Otherwise, it will be only the selected race.
     *
     * @return the List of all Races selectable for this Buildable
     */

    public List<Race> getSelectableRaces()
    {
        return selectableRaces;
    }

    /**
     * Returns the number of levels that the character currently being built
     * possesses in the specified Role.
     *
     * @param role the Role to get the number of levels for
     * @return the number of levels in the specified Role
     */

    public int getLevel(Role role)
    {
        return creature.roles.getLevel(role);
    }

    /**
     * Returns the total number of levels possessed by the character being
     * built.  This will be the number of levels of the base creature plus
     * one.
     *
     * @return the total number of levels of the character being built.
     */

    public int getCreatureLevel()
    {
        return creature.stats.getCreatureLevel() + 1;
    }

    /**
     * Returns the number of unspent skill points that the base character for this
     * Buildable possesses.  This value is not dependant on the number of points
     * set via {@link #setSelectedSkills(SkillSet, int)}.
     *
     * @return the number of unspent skill points for the base character
     */

    public int getUnspentSkillPoints()
    {
        return creature.getUnspentSkillPoints();
    }

    /**
     * Returns the skill set possessed by the base creature being edited.  This is
     * not affected by any selections made via {@link #setSelectedSkills(SkillSet, int)}
     *
     * @return the skill set for the base creature being edited
     */

    public SkillSet getSkillSet()
    {
        return creature.skills;
    }

    /**
     * If this is a new character, returns the selected intelligence attribute. Otherwise,
     * returns the base intelligence stat for the edited creature.
     *
     * @return the intelligence for the character being edited
     */

    public int getCurrentIntelligence()
    {
        if (newCharacter) {
            return selectedAttributes[3];
        } else {
            return creature.stats.getBaseInt();
        }
    }

    /**
     * Returns the name of this character.  For new characters, this is the selected name.
     * For existing characters, this is the creature name.
     *
     * @return the name of this character.
     */

    public String getName()
    {
        if (newCharacter) {
            return selectedName;
        } else {
            return creature.getTemplate().getName();
        }
    }

    /**
     * Returns the base role of this character.  For new characters, this is the selected role.
     * For existing characters, this is the base role for that creature's roleset
     *
     * @return the base role of this character
     */

    public Role getBaseRole()
    {
        if (newCharacter) {
            return selectedRole;
        } else {
            return creature.roles.getBaseRole();
        }
    }
}
