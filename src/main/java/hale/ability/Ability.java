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

package main.java.hale.ability;

import java.util.ArrayList;
import java.util.List;

import de.matthiasmann.twl.Color;

import main.java.hale.Game;
import main.java.hale.entity.Creature;
import main.java.hale.icon.Icon;
import main.java.hale.icon.IconFactory;
import main.java.hale.resource.ResourceManager;
import main.java.hale.rules.PrereqList;
import main.java.hale.util.Logger;
import main.java.hale.util.SimpleJSONArrayEntry;
import main.java.hale.util.SimpleJSONObject;
import main.java.hale.util.SimpleJSONParser;

/**
 * An Ability is a special power that a particular Creature possesses.  Abilities can
 * provide constant benefits or be activated.  Abilities can represent special combat
 * abilities, spells, or any other special trait or talent.
 * <p>
 * An ability will typically apply one or more {@link Effect}s onto a parent or target creature.
 * <p>
 * An Ability object is immutable.  There should only be one copy of each Ability in existance
 * at any time.
 *
 * @author Jared Stephen
 */

public class Ability extends Scriptable
{

    /**
     * The general type of action this Ability performs.  This is helpful
     * for the AI in categorizing Abilities.
     *
     * @author Jared Stephen
     */
    public enum ActionType
    {
        /**
         * Improves the stats of a friendly creature or creatures, and / or the ability activator
         */
        Buff,

        /**
         * Restores hit points to a friendly creature or creatures, and / or the ability activator
         */
        Heal,

        /**
         * Removes defenses or lowers stats of a hostile creature or creatures
         */
        Debuff,

        /**
         * Reduces hit points of one or more hostile creatures
         */
        Damage,

        /**
         * Creates an allied creature to fight for the caster
         */
        Summon,

        /**
         * Useful in a specific set of circumstances that doesn't fall into the any of the above categories.
         * Often, provides some benefit or drawback to hostiles and friendlies alike, or a combination of
         * benefits and drawbacks to a single target.
         **/
        Tactical;
    }

    /**
     * The number of targets affected by this Ability.  Useful for the AI in categorizing
     * Abilities.  Note that this is not the number of Creatures that are targetted by the
     * Ability, but the number of Creatures typically affected.  Many Abilities target
     * one central point but can affect multiple targets.
     *
     * @author Jared Stephen
     */
    public enum GroupType
    {
        /**
         * Affects only a single Creature
         */
        Single,

        /**
         * Affects one or potentially more Creatures
         */
        Multiple;
    }

    /**
     * The range (distance) to targets selectable by this Ability.  Useful for the AI in
     * categorizing Abilities.
     *
     * @author Jared Stephen
     */
    public enum RangeType
    {
        /**
         * only targetable on the AbilityActivator using this Ability
         */
        Personal,

        /**
         * targetable on Creatures 1 hex away
         */
        Touch,

        /**
         * targetable on Creatures a short distance away, 2 to 4 hexes
         */
        Short,

        /**
         * targetable on Creatures up to a long distance away, usually any visible Creature
         */
        Long;
    }

    private final String id;
    private final String name;
    private final String type;
    private final Icon icon;

    private final boolean isFixed;
    private final boolean isActivateable;
    private final boolean isMode;
    private final boolean isCancelable;
    private final boolean canActivateOutsideCombat;

    private final int cooldown;
    private final int actionPointCost;
    private final String actionPointCostDescription;

    private final PrereqList prereqs, restrictions;

    private final ActionType actionType;
    private final GroupType groupType;
    private final RangeType rangeType;
    private final int aiPower;
    private final int aiPriority;

    private final String description;
    private List<AbilityUpgrade> upgrades;

    private final String quickbarGroup;

    // stored here so generic ability activators can get it for abilities
    // that may or may not be spells
    private final int spellLevel;

    /**
     * Ability factory method.  This is the preferred method for creating new abilities.
     * Creates a new Ability using the specified id String and the Resource
     * at the specified location.  Will determine whether the Object created should
     * be an Ability or a Spell.
     *
     * @param id           the ID String of this Ability
     * @param resourcePath the location of the Resource to read in creating
     *                     this Ability
     * @return a new Ability created from the specified resource
     */

    public static Ability createAbilityFromResource(String id, String resourcePath) throws IllegalArgumentException
    {
        SimpleJSONParser parser = new SimpleJSONParser(resourcePath);

        boolean inline;
        String script;
        String scriptLocation;

        boolean isSpell;
        if (parser.containsKey("isSpell")) {
            isSpell = parser.get("isSpell", false);
        } else {
            isSpell = false;
        }

        if (!parser.containsKey("externalScript")) {
            if (isSpell) {
                Logger.appendToWarningLog("Warning.  Script not specified for Spell " + id +
                        " and inline scripts unsupported for spells.");
            }

            if (parser.containsKey("inlineScript")) {
                script = parser.get("inlineScript", null);
                inline = true;
            } else {
                script = null;
                inline = false;
            }

            // set the script location to something sensible so error messages from the
            // superclass methods are useful
            scriptLocation = resourcePath;
        } else {
            scriptLocation = parser.get("externalScript", null);
            script = ResourceManager.getScriptResourceAsString(scriptLocation);
            inline = false;
        }

        try {
            Ability ability;

            if (isSpell) {
                ability = new Spell(id, script, scriptLocation, parser.getObject());
            } else {
                ability = new Ability(id, script, scriptLocation, parser.getObject(), inline);
            }

            parser.warnOnUnusedKeys();

            return ability;

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Error creating ability " + id + " from resource " + resourcePath, e);
        }
    }

    /**
     * Create a new Ability with the specified parameters
     *
     * @param id             the ID String of the Ability
     * @param script         the script contents of the Ability
     * @param scriptLocation the resource location of the script for the Ability
     * @param map            the JSON to parse for this ability
     * @param inline         See @link Scriptable#Scriptable(String, String, boolean).  Whether
     *                       or not this script is treated as inline.
     */

    protected Ability(String id, String script, String scriptLocation, SimpleJSONObject map, boolean inline)
    {
        super(script, scriptLocation, inline);
        this.id = id;

        this.name = map.get("name", id);
        this.type = map.get("type", this.getClass().getSimpleName());

        if (map.containsKey("icon")) {
            icon = IconFactory.createIcon(map.getObject("icon"));
        } else {
            icon = IconFactory.emptyIcon;
        }

        this.description = map.get("description", null);

        upgrades = new ArrayList<AbilityUpgrade>();
        for (SimpleJSONArrayEntry entry : map.getArray("upgrades")) {
            SimpleJSONObject entryObj = entry.getObject();

            AbilityUpgrade upgrade = new AbilityUpgrade(entryObj);
            upgrades.add(upgrade);
        }

        if (map.containsKey("canActivateOutsideCombat")) {
            this.canActivateOutsideCombat = map.get("canActivateOutsideCombat", false);
        } else {
            this.canActivateOutsideCombat = false;
        }

        if (map.containsKey("isActivateable")) {
            this.isActivateable = map.get("isActivateable", false);
        } else {
            this.isActivateable = false;
        }

        if (map.containsKey("isFixed")) {
            this.isFixed = map.get("isFixed", false);
        } else {
            this.isFixed = false;
        }

        if (map.containsKey("isMode")) {
            this.isMode = map.get("isMode", false);
        } else {
            this.isMode = false;
        }

        if (map.containsKey("isCancelable")) {
            this.isCancelable = map.get("isCancelable", false);
        } else {
            this.isCancelable = false;
        }

        if (map.containsKey("spellLevel")) {
            this.spellLevel = map.get("spellLevel", 0);
        } else {
            this.spellLevel = 0;
        }

        if (map.containsKey("quickbarGroup")) {
            this.quickbarGroup = map.get("quickbarGroup", null);
        } else {
            this.quickbarGroup = null;
        }

        if (!isActivateable && isMode) {
            Logger.appendToWarningLog("Ability at " + map.getObjectID() +
                    " is not activateable.  Mode=true flag will have no effect.");
        }

        if ((!isActivateable || !isMode) && isCancelable) {
            Logger.appendToWarningLog("Ability at " + map.getObjectID() +
                    " is not an activateable mode.  Cancelable=true flag will have no effect.");
        }

        if (!isActivateable && isFixed) {
            Logger.appendToWarningLog("Ability at " + map.getObjectID() +
                    " is not activateable.  Fixed=true flag will have no effect.");
        }

        if (!isActivateable && quickbarGroup != null) {
            Logger.appendToWarningLog("Ability at " + map.getObjectID() +
                    "is not activateable.  Quickbar group will have no affect.");
        }


        if (map.containsKey("cooldown")) {
            this.cooldown = map.get("cooldown", 0);
        } else {
            this.cooldown = 0;
        }

        if (map.isInteger("actionPointCost")) {
            this.actionPointCost = map.get("actionPointCost", 0);
            this.actionPointCostDescription = Integer.toString(this.actionPointCost / 100);
        } else
            if (map.isString("actionPointCost")) {
                this.actionPointCost = 0;
                this.actionPointCostDescription = map.get("actionPointCost", null);
            } else {
                this.actionPointCost = 0;
                this.actionPointCostDescription = Integer.toString(this.actionPointCost / 100);
            }

        int aiPower = 0;
        int aiPriority = 1;
        String actionType = null;
        String groupType = null;
        String rangeType = null;
        if (map.containsKey("ai")) {
            SimpleJSONObject aiIn = map.getObject("ai");

            if (aiIn.containsKey("power")) {
                aiPower = aiIn.get("power", 0);
            }

            if (aiIn.containsKey("priority")) {
                aiPriority = aiIn.get("priority", 1);
            }

            if (aiIn.containsKey("actionType")) {
                actionType = aiIn.get("actionType", null);
                groupType = aiIn.get("groupType", null);
                rangeType = aiIn.get("rangeType", null);
            }
        }
        this.actionType = actionType != null ? ActionType.valueOf(actionType) : null;
        this.groupType = groupType != null ? GroupType.valueOf(groupType) : null;
        this.rangeType = rangeType != null ? RangeType.valueOf(rangeType) : null;
        this.aiPower = aiPower;
        this.aiPriority = aiPriority;

        if (map.containsKey("prereqs")) {
            prereqs = new PrereqList(map.getObject("prereqs"));
        } else {
            prereqs = new PrereqList();
        }

        if (map.containsKey("restrictions")) {
            restrictions = new PrereqList(map.getObject("restrictions"));
        } else {
            restrictions = new PrereqList();
        }
    }

    public boolean canActivateOutsideCombat()
    {
        return canActivateOutsideCombat;
    }

    /**
     * Returns the spell level of this ability, not modified by any upgrades.
     * <p>
     * Most uses should use {@link #getSpellLevel(Creature)} instead
     *
     * @return the spell level of this ability
     */

    public int getSpellLevel()
    {
        return this.spellLevel;
    }

    /**
     * Returns the Spell Level of this Ability.  If this is not a Spell, returns 0.
     * This value is important in determining various interactions such as chance of
     * spell failure and spell resistance.  Upgraded abilities may increase the value
     * of this Spell's spell level.
     *
     * @param parent the owner of this ability, used to check for upgrades.  If the parent
     *               is null, then this method will simply return the spell level of this ability unupgraded
     * @return the Spell Level of this spell, or 0 if this is not a spell
     */

    public int getSpellLevel(Creature parent)
    {
        int level = this.spellLevel;

        if (parent == null) return this.spellLevel;

        for (AbilityUpgrade upgrade : upgrades) {
            if (parent.abilities.has(upgrade.id)) {
                level = Math.max(level, Game.ruleset.getAbility(upgrade.id).spellLevel);
            }
        }

        return level;
    }

    /**
     * Returns the quickbar group ID associated with this ability or null if no group exists
     *
     * @return the quickbar group ID
     */

    public String getQuickbarGroup()
    {
        return quickbarGroup;
    }

    /**
     * Returns the ID String for this Ability.  This ID must be
     * unique among all the defined Abilities
     *
     * @return the ID String for this Ability
     */

    public String getID()
    {
        return id;
    }

    /**
     * Returns the name String for this Ability.
     *
     * @return the name String for this Ability
     */

    public String getName()
    {
        return name;
    }

    /**
     * Returns the type String for this Ability.
     *
     * @return the type String for this Ability
     */

    public String getType()
    {
        return type;
    }

    /**
     * Returns the description String for this Ability.
     *
     * @return the description String for this Ability
     */

    public String getDescription()
    {
        return description;
    }

    /**
     * Returns the Icon referencing the {@link main.java.hale.resource.Sprite} for this Ability's icon.
     *
     * @return the Icon referencing this Ability's icon Sprite.
     */

    public Icon getIcon()
    {
        return icon;
    }

    /**
     * Returns whether this Ability is fixed.  A fixed ability is an activateable ability
     * that is always readied in one and only one AbilitySlot.  This AbilitySlot will
     * automatically be added to the parent Creature when adding the Ability.
     *
     * @return true if and only if this Ability is fixed
     */

    public boolean isFixed()
    {
        return isFixed;
    }

    /**
     * Returns whether or not this Ability is user activateable.
     * Some Abilities have constant effects and cannot be activated, others
     * can be enabled at will or only when certain conditions are met.
     *
     * @return true if this Ability can be activated, false otherwise
     */

    public boolean isActivateable()
    {
        return isActivateable;
    }

    /**
     * Returns whether this Ability is a mode.  This is only meaningful for
     * activateable Abilities.  Mode abilities remain in an active state
     * once activated until their duration runs out, they are canceled, or
     * some other event occurs.
     *
     * @return true if this Ability is a mode, false otherwise
     */

    public boolean isMode()
    {
        return isMode;
    }

    /**
     * Returns whether this Ability can be canceled once active.  This is only
     * meaningful for activateable mode Abilities.  Some Abilities last for a specific
     * duration of time or until a certain condition is met.  Others can be canceled
     * at will.
     *
     * @return true if this Ability can be canceled, false otherwise
     */

    public boolean isCancelable()
    {
        return isCancelable;
    }

    /**
     * Returns the cooldown for this Ability in rounds.  This is the length
     * of time the user must wait between successive uses of the Ability.
     * 0 is a valid value.
     *
     * @param parent the cooldown duration can potentially be modified by bonuses on this parent creature
     * @return the cooldown for this Ability in rounds
     */

    public int getCooldown(Creature parent)
    {
        return cooldown;
    }

    /**
     * Returns the Action Point (AP) cost of activating this Ability.
     * If this Ability is not activateable, then this value has no effect.
     * Some Abilities have a variable AP cost.  In this case, this value
     * should be left at zero and the APCostDescription should be set
     * to explain how the AP is calculated.  This Ability's script will
     * then need to take care of handling the AP usage of this Ability.
     *
     * @return the AP cost of activating this Ability.
     */

    public int getAPCost()
    {
        return actionPointCost;
    }

    /**
     * Returns a String description of the AP cost of this Ability.  By default,
     * this will be a String representation of the integer AP cost.  However,
     * this can be a text description for some Abilities.
     *
     * @return a String description of the AP cost of this Ability.
     */

    public String getAPCostDescription()
    {
        return actionPointCostDescription;
    }

    /**
     * Returns the ActionType for this Ability, which describes the types of targets
     * and the action this Ability will perform on them
     *
     * @return the ActionType for this Ability
     */

    public ActionType getActionType()
    {
        return actionType;
    }

    /**
     * Returns the RangeType for this Ability, which describes the distance targets
     * must be from the activator
     *
     * @return the RangeType for this Ability
     */

    public RangeType getRangeType()
    {
        return rangeType;
    }

    /**
     * Gets the range type of this ability, or if the parent has any upgrades to this ability,
     * the highest range type of this ability or any of the upgrades, using the natural ordering
     * of the Ability.RangeType enum
     *
     * @param parent the owner of this ability
     * @return the max range type
     */

    public RangeType getUpgradedRangeType(Creature parent)
    {
        RangeType rangeType = this.rangeType;

        for (AbilityUpgrade upgrade : upgrades) {
            String abilityID = upgrade.id;
            if (parent.abilities.has(abilityID)) {
                Ability ability = Game.ruleset.getAbility(abilityID);

                RangeType otherRangeType = ability.getRangeType();
                if (otherRangeType.ordinal() > rangeType.ordinal()) {
                    rangeType = otherRangeType;
                }
            }
        }

        return rangeType;
    }

    /**
     * Returns the GroupType for this Ability, which describes the number of targets
     * this Ability affects.  Note that it does not describe the number of targets
     * that must be selected, which is usually one even for Abilities affecting
     * groups.
     *
     * @return the GroupType for this Ability
     */

    public GroupType getGroupType()
    {
        return groupType;
    }

    /**
     * Gets the group type of this ability, or if the parent has any upgrades to this ability,
     * the highest group type of this ability or any of the upgrades, using the natural ordering
     * of the Ability.GroupType enum
     *
     * @param parent the owner of this ability
     * @return the max group type
     */

    public GroupType getUpgradedGroupType(Creature parent)
    {
        GroupType groupType = this.groupType;

        for (AbilityUpgrade upgrade : upgrades) {
            String abilityID = upgrade.id;
            if (parent.abilities.has(abilityID)) {
                Ability ability = Game.ruleset.getAbility(abilityID);

                GroupType otherGroupType = ability.getGroupType();

                if (otherGroupType.ordinal() > groupType.ordinal()) {
                    groupType = otherGroupType;
                }
            }
        }

        return groupType;
    }

    /**
     * Returns the priority that the AI should attempt to use this ability at.  Higher priority abilities
     * should be used sooner in combat
     *
     * @return the AI priority
     */

    public int getAIPriority()
    {
        return aiPriority;
    }

    /**
     * Returns the approximate relative power or effectiveness of this ability, relative
     * to other similar abilities.  This is used in sorting abilities in the ai abilities set
     *
     * @return the approximate relative effectiveness of this ability
     */

    public int getAIPower()
    {
        return aiPower;
    }

    /**
     * Gets the AI power of this ability, or if the parent has any upgrades to this ability,
     * the highest AI power of this ability or any of the upgrades
     *
     * @param parent the owner of this ability
     * @return the max AI power of this ability or any of its upgrades owned by the parent
     */

    public int getUpgradedAIPower(Creature parent)
    {
        int aiPower = this.aiPower;

        for (AbilityUpgrade upgrade : upgrades) {
            String abilityID = upgrade.id;
            if (parent.abilities.has(abilityID)) {
                Ability ability = Game.ruleset.getAbility(abilityID);
                aiPower = Math.max(aiPower, ability.getAIPower());
            }
        }

        return aiPower;
    }

    /**
     * Performs several miscellaneous actions that are part of activating an ability.
     * <p>
     * This includes running "onAbilityActivate" scripts on the parent, and adding
     * basic interface feedback including a message and a fade away indicating the
     * ability was used.  The AP cost of this Ability will also be deducted at this
     * time.
     *
     * @param parent the parent Activator / Creature that is activating this Ability
     */

    public void activate(Creature parent)
    {
        parent.getEffects().executeOnAll(ScriptFunctionType.onAbilityActivated, this, parent);

        // spells create their own detailed message
        if (!(this instanceof Spell)) {
            Game.mainViewer.addMessage(parent.getTemplate().getName() + " uses " + getName() + ".");
        }

        Game.mainViewer.addFadeAway(getName(), parent.getLocation().getX(), parent.getLocation().getY(), new Color(0xFF00FF00));

        parent.timer.performAction(this.actionPointCost);
    }

    /**
     * Returns true if and only if the specified Creature meets all the
     * prereqs specified in this Abilities definition text file, and none
     * of the restrictions.
     * Note that Abilities can be added to Creatures that do not satisfy
     * the prereqs.  However, when selecting new Abilities at level up,
     * players will only be able to choose Abilities where they meet the
     * prereqs.
     *
     * @param parent the Creature to check the prereqs against
     * @return true if and only if the specified Creature meets all prereqs
     */

    public boolean meetsPrereqs(Creature parent)
    {
        return prereqs.meetsPrereqs(parent) && restrictions.meetsRestrictions(parent);
    }

    private void appendUpgradesList(StringBuilder sb, Creature parent, boolean upgraded)
    {
        if (parent == null) return;

        if (upgraded) {
            sb.append("<div style=\"font-family: medium;\">");
            sb.append("Base Ability ");
            sb.append("<span style=\"font-family: medium-red\">");
            sb.append(this.getName());
            sb.append("</span>");
            sb.append("</div>");
        }

        int upgradesCount = 0;
        for (AbilityUpgrade upgrade : upgrades) {
            String abilityID = upgrade.id;
            Ability ability = Game.ruleset.getAbility(abilityID);

            if (parent.abilities.has(ability)) {
                if (upgradesCount == 0) {
                    sb.append("<div style=\"font-family: medium; margin-bottom: 1em;\">");
                    sb.append("Upgrades ");
                    upgradesCount++;
                } else {
                    sb.append(", ");
                }

                sb.append("<span style=\"font-family: medium-red\">");
                sb.append(ability.getName());
                sb.append("</span>");
            }
        }

        if (upgradesCount > 0) {
            sb.append("</div>");
        }
    }

    /**
     * Appends the String representation of the properties of this Ability
     * to the specified StringBuilder.  This is used by AbilityDetailsWindow
     * when showing the detailed view of this Ability.
     *
     * @param sb      the StringBuilder to append to
     * @param parent  the owner of this ability
     * @param upgrade true if we are looking at the details of the upgraded version, false
     *                for the base version
     */

    public void appendDetails(StringBuilder sb, Creature parent, boolean upgrade)
    {
        // whether the ability is active or passive
        sb.append("<div style=\"font-family: medium; margin-bottom: 1em;\">");
        appendBaseType(sb);
        sb.append("</div>");

        // append section on upgrades if this ability has any
        appendUpgradesList(sb, parent, upgrade);

        // create table with slot type, AP cost, and cooldown
        sb.append("<table style=\"font-family: medium; vertical-align: middle;\">");
        sb.append("<tr><td style=\"width: 10ex;\">Slot Type</td><td style=\"font-family: medium-green\">");
        sb.append(type).append("</td></tr>");

        if (isActivateable) {
            sb.append("<tr><td style=\"width: 10ex;\">AP Cost</td><td style=\"font-family: medium-blue\">");
            sb.append(actionPointCostDescription).append("</td></tr>");

            if (cooldown > 0) {
                sb.append("<tr><td style=\"width: 10ex;\">Cooldown</td><td style=\"font-family: medium-red\">");
                sb.append(cooldown).append(" Rounds</td></tr>");
            }
        }
        sb.append("</table>");

        this.prereqs.appendDescription(sb, parent, "Prerequisites");

        this.restrictions.appendDescription(sb, parent, "Restrictions");
    }

    /**
     * Appends the description of all currently owned upgrades to the specified StringBuilder
     *
     * @param sb     the StringBuilder to append
     * @param parent the creature who will be checked for the upgrade abilities, or null to check
     *               no owner
     */

    public void appendUpgradesDescription(StringBuilder sb, Creature parent)
    {
        if (parent == null) return;

        List<String> upgradesToShow = new ArrayList<String>();

        for (AbilityUpgrade upgrade : upgrades) {
            String abilityID = upgrade.id;
            if (parent.abilities.has(abilityID)) {
                upgradesToShow.add(upgrade.description);
            }
        }

        for (String upgrade : upgradesToShow) {
            sb.append("<div style=\"margin-top: 1em\">");
            sb.append(upgrade);
            sb.append("</div>");
        }
    }

    /**
     * Returns the name associated with the highest priority upgrade available to the parent
     *
     * @param parent
     * @return the upgraded name
     */

    public String getUpgradedName(Creature parent)
    {
        for (int i = upgrades.size() - 1; i >= 0; i--) {
            AbilityUpgrade upgrade = upgrades.get(i);

            if (upgrade.override && parent.abilities.has(upgrade.id)) {
                return Game.ruleset.getAbility(upgrade.id).name;
            }
        }

        return this.name;
    }

    /**
     * Returns the icon associated with the highest priority upgrade available to the parent
     *
     * @param parent
     * @return the upgraded icon
     */

    public Icon getUpgradedIcon(Creature parent)
    {
        for (int i = upgrades.size() - 1; i >= 0; i--) {
            AbilityUpgrade upgrade = upgrades.get(i);

            if (upgrade.override && parent.abilities.has(upgrade.id)) {
                return Game.ruleset.getAbility(upgrade.id).icon;
            }
        }

        return this.icon;
    }

    @Override
    public String toString()
    {
        return this.name;
    }

    /*
     * Appends the basic type information for this Ability to the given StringBuilder.
     * This includes whether the ability is active, cancelable, or a mode.
     */

    private void appendBaseType(StringBuilder sb)
    {
        if (isActivateable) {
            sb.append("<p><span style=\"font-family: medium-italic-red\">Active</span> Ability</p>");
            if (isMode) {
                sb.append("<p>");
                if (isCancelable) {
                    sb.append("<span style=\"font-family: medium-italic-blue\">Cancelable</span> ");
                }

                sb.append("Mode</p>");
            }
        } else {
            sb.append("<p><span style=\"font-family: medium-italic-red\">Passive</span> Ability</p>");
        }
    }

    /**
     * Shortens the duration of the effect if this Ability is a Spell with spell resistance
     * applying and the target of the effect has spell resistance.  Also applies any spell
     * duration bonuses.
     * <p>
     * For abilities that are not spells, no action is taken.  Also
     * <p>
     * See {@link #setSpellDuration(Effect, Creature)}
     *
     * @param effect the effect to shorten duration
     * @param parent the activator for this ability
     */

    public void setSpellDuration(Effect effect, Creature parent)
    {
    }

    private class AbilityUpgrade
    {
        private final String id;
        private final String description;
        private final boolean override;

        private AbilityUpgrade(SimpleJSONObject data)
        {
            id = data.get("ability", null);
            description = data.get("description", null);
            override = data.get("override", false);

        }
    }
}
