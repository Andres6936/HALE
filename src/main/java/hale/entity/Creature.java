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

package main.java.hale.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.matthiasmann.twl.Color;

import main.java.hale.Game;
import main.java.hale.ability.AbilitySlot;
import main.java.hale.ability.CreatureAbilitySet;
import main.java.hale.ability.Effect;
import main.java.hale.ability.ScriptFunctionType;
import main.java.hale.area.Area;
import main.java.hale.bonus.Stat;
import main.java.hale.bonus.StatManager;
import main.java.hale.icon.ComposedCreatureIcon;
import main.java.hale.icon.Icon;
import main.java.hale.icon.IconFactory;
import main.java.hale.icon.IconRenderer;
import main.java.hale.icon.SimpleIcon;
import main.java.hale.icon.SubIcon;
import main.java.hale.icon.SubIconRenderer;
import main.java.hale.interfacelock.EntityOffsetAnimation;
import main.java.hale.loading.JSONOrderedObject;
import main.java.hale.loading.LoadGameException;
import main.java.hale.loading.ReferenceHandler;
import main.java.hale.rules.Attack;
import main.java.hale.rules.Damage;
import main.java.hale.rules.RoleSet;
import main.java.hale.rules.SkillSet;
import main.java.hale.util.AreaUtil;
import main.java.hale.util.Logger;
import main.java.hale.util.Point;
import main.java.hale.util.SimpleJSONArray;
import main.java.hale.util.SimpleJSONArrayEntry;
import main.java.hale.util.SimpleJSONObject;
import main.java.hale.util.SimpleJSONParser;

/**
 * A creature is an entity that moves and interacts with the world around.
 * Creatures include player characters and non player characters
 *
 * @author Jared
 */

public abstract class Creature extends Entity
{

    private final CreatureTemplate template;

    private final IconRenderer renderer;

    // animation state of the creature in the area relative to its
    // normal position
    private Point animatingOffset;
    private EntityOffsetAnimation offsetAnimation;

    // the encounter is the group containing this creature; it is also used
    // for group aspects of the AI
    private Encounter encounter;

    private boolean[][] visibility;

    private int currentHitPoints, temporaryHitPoints;

    // the round number at which this creature will be unsummoned
    // for creatures which are not summoned creatures, this value will be equal to -1
    private int summonExpiration;

    // number of attacks of opportunity this creature has left for this round
    private int attacksOfOpportunityAvailable;

    private volatile boolean isCurrentlyMoving;

    private boolean alreadySearchedForHiddenCreatures;

    // the list of targets that this creature has already taken a movement caused
    // AoO against this round.  This will extremely rarely hold more than 1 or 2 members,
    // so List is better for Set, even though we will be searching it
    private final List<Creature> moveAoOsThisRound;

    private final List<String> unarmedWeapons;
    private Weapon unarmedWeapon;

    /**
     * Controls all primary and secondary statistics for this creature
     */

    public final StatManager stats;

    /**
     * Holds all equipped and unequipped items
     */

    public final Inventory inventory;

    /**
     * The set of roles currently held by this creature
     */

    public final RoleSet roles;

    /**
     * The skills and associated skill points for this creature
     */

    public final SkillSet skills;

    /**
     * The set of abilities currently owned by this creature
     */

    public final CreatureAbilitySet abilities;

    /**
     * The timer keeping track of this creature's Action Points (AP)
     */

    public final RoundTimer timer;

    @Override
    public void load(SimpleJSONObject data, Area area, ReferenceHandler refHandler) throws LoadGameException
    {
        super.load(data, area, refHandler);

        this.currentHitPoints = data.get("currentHitPoints", 0);

        if (data.containsKey("temporaryHitPoints")) {
            this.temporaryHitPoints = data.get("temporaryHitPoints", 0);
        } else {
            this.temporaryHitPoints = 0;
        }

        if (data.containsKey("summonExpiration")) {
            this.summonExpiration = data.get("summonExpiration", 0);
        } else {
            this.summonExpiration = -1;
        }

        this.skills.load(data.getObject("skills"));
        this.roles.load(data.getObject("roles"), false);
        this.abilities.load(data.getObject("abilities"), refHandler);
        this.inventory.load(data.getObject("inventory"), refHandler);

        stats.recomputeAllStats();
        timer.reset();
    }

    @Override
    public JSONOrderedObject save()
    {
        JSONOrderedObject out = super.save();

        out.put("currentHitPoints", currentHitPoints);

        if (temporaryHitPoints > 0) {
            out.put("temporaryHitPoints", temporaryHitPoints);
        }

        if (summonExpiration > 0) {
            out.put("summonExpiration", summonExpiration);
        }

        out.put("inventory", inventory.save());
        out.put("roles", roles.save());
        out.put("skills", skills.save());
        out.put("abilities", abilities.save());

        return out;
    }

    /**
     * Creates a new Creature by parsing the specified JSON.  The template
     * has already been defined, and then additional data is read from the JSON
     * to fully define the creature
     *
     * @param template
     * @param parser
     */

    protected Creature(CreatureTemplate template, SimpleJSONParser parser)
    {
        super(template);

        this.template = template;
        this.renderer = createIconRenderer();

        // parse attributes
        stats = new StatManager(this);
        SimpleJSONObject obj = parser.getObject("attributes");
        int[] attributes = new int[6];
        attributes[0] = obj.get("strength", 0);
        attributes[1] = obj.get("dexterity", 0);
        attributes[2] = obj.get("constitution", 0);
        attributes[3] = obj.get("intelligence", 0);
        attributes[4] = obj.get("wisdom", 0);
        attributes[5] = obj.get("charisma", 0);
        stats.setAttributes(attributes);

        unarmedWeapons = new ArrayList<String>();
        unarmedWeapons.add(template.getRace().getDefaultWeaponTemplate().getID());

        // parse skills
        skills = new SkillSet(this);
        if (parser.containsKey("skills")) {
            skills.load(parser.getObject("skills"));
        }

        // parse abilities
        abilities = new CreatureAbilitySet(this);
        if (parser.containsKey("abilities")) {
            SimpleJSONArray abilitiesArray = parser.getArray("abilities");
            for (SimpleJSONArrayEntry entry : abilitiesArray) {
                obj = entry.getObject();

                String id = obj.get("id", null);
                int level = obj.get("levelObtained", 0);

                try {
                    abilities.add(Game.ruleset.getAbility(id), level);
                } catch (Exception e) {
                    Logger.appendToErrorLog("Error loading ability " + id, e);
                }
            }
        }

        // parse roles
        roles = new RoleSet(this);
        roles.load(parser.getObject("roles"), true);

        timer = new RoundTimer(this);

        summonExpiration = -1;

        moveAoOsThisRound = new ArrayList<Creature>(2);

        animatingOffset = new Point();

        // 1 hit point prevents this creature from starting out dead
        currentHitPoints = 1;

        // parse inventory Note that Inventory MUST be initialized last
        inventory = new Inventory(this);
        try {
            inventory.load(parser.getObject("inventory"), null);
        } catch (LoadGameException e) {
            Logger.appendToErrorLog("Error loading inventory for " + template.getID(), e);
        }
    }

    /**
     * Creates a new creature from the specified template.  This is used for
     * creating buildable characters in the character editor, and for loading PCs
     *
     * @param template the creature template
     */

    protected Creature(CreatureTemplate template)
    {
        super(template);

        this.template = template;

        // only initialize the icon renderer if race and gender are defined
        // this is only applicable for the character builder, where they might
        // not be defined yet
        if (template.getRace() != null && template.getGender() != null) {
            this.renderer = createIconRenderer();
        } else {
            this.renderer = IconFactory.emptyIcon;
        }

        stats = new StatManager(this);

        unarmedWeapons = new ArrayList<String>();
        if (template.getRace() != null) {
            unarmedWeapons.add(template.getRace().getDefaultWeaponTemplate().getID());
        }

        roles = new RoleSet(this);
        skills = new SkillSet(this);
        abilities = new CreatureAbilitySet(this);
        timer = new RoundTimer(this);

        summonExpiration = -1;

        moveAoOsThisRound = new ArrayList<Creature>(2);

        animatingOffset = new Point();

        // 1 hit point prevents this creature from starting out dead
        currentHitPoints = 1;

        // Note that Inventory MUST be initialized last
        inventory = new Inventory(this);
    }

    /**
     * Creates a new copy of the specified creature.  Permanent creature data such as
     * stats, inventory, roles, skills, and abilities are copied.  No other data is copied,
     * however
     *
     * @param other the creature to copy
     */

    protected Creature(Creature other)
    {
        super(other);

        this.template = other.template;
        this.renderer = createIconRenderer();

        stats = new StatManager(other.stats, this);
        roles = new RoleSet(other.roles, this);
        skills = new SkillSet(other.skills, this);
        abilities = new CreatureAbilitySet(other.abilities, this);
        timer = new RoundTimer(this);

        summonExpiration = -1;

        moveAoOsThisRound = new ArrayList<Creature>(2);

        animatingOffset = new Point();

        // 1 hit point prevents this creature from starting out dead
        currentHitPoints = 1;

        // Note that Inventory MUST be initialized last
        inventory = new Inventory(other.inventory, this);

        unarmedWeapons = new ArrayList<String>();
        for (String templateID : other.unarmedWeapons) {
            unarmedWeapons.add(templateID);
        }
    }

    private IconRenderer createIconRenderer()
    {
        Icon icon = template.getIcon();
        if (icon instanceof ComposedCreatureIcon) {
            return new SubIconRenderer((ComposedCreatureIcon)icon, template.getRace(), template.getGender());
        } else {
            return icon;
        }
    }

    /**
     * Sets the animation which controls the offset point
     *
     * @param animation the offset animation, or null to cancel any
     *                  current offset animation
     */

    public void setOffsetAnimation(EntityOffsetAnimation animation)
    {
        if (offsetAnimation != null) {
            offsetAnimation.cancel();
        }

        offsetAnimation = animation;

        if (offsetAnimation != null) {
            offsetAnimation.setAnimatingPoint(this.animatingOffset);
        }
    }

    /**
     * Flags this creature as currently moving, or not
     *
     * @param isMoving
     */

    public void setCurrentlyMoving(boolean isMoving)
    {
        this.isCurrentlyMoving = isMoving;
    }

    public boolean isCurrentlyMoving()
    {
        return isCurrentlyMoving;
    }

    /**
     * Sets the current value of the animating offset point.  If there
     * is an animation playing, this will be overridden on the next
     * animation update
     *
     * @param x
     * @param y
     */

    public void setOffsetPoint(int x, int y)
    {
        animatingOffset.x = x;
        animatingOffset.y = y;
    }

    /**
     * Returns the x coordinate of this creature's animating offset point
     *
     * @return the x coordinate
     */

    public int getAnimatingOffsetX()
    {
        return animatingOffset.x;
    }

    /**
     * Returns the y coordinate of this creature's animating offset point
     *
     * @return the y coordinate
     */

    public int getAnimatingOffsetY()
    {
        return animatingOffset.y;
    }

    /**
     * Gets the template that this creature is based on
     *
     * @return the template
     */

    @Override
    public CreatureTemplate getTemplate()
    {
        return template;
    }

    /**
     * Returns true if this is a summoned creature which will eventually be unsummoned,
     * disappearing from the area, or false if it is a normal creature
     *
     * @return whether this is a summoned creature
     */

    public boolean isSummoned()
    {
        return summonExpiration != -1;
    }

    /**
     * Sets this creature as a summoned creature which will expire after the specified
     * number of rounds have passed
     *
     * @param duration
     */

    public void setSummoned(int duration)
    {
        this.summonExpiration = Game.curCampaign.getDate().getTotalRoundsElapsed() + duration;
    }

    /**
     * Returns the current number of hits points that this creature has.  This
     * represents the amount of damage the creature can take before falling combat
     *
     * @return the current hit points
     */

    public int getCurrentHitPoints()
    {
        return currentHitPoints + temporaryHitPoints;
    }

    /**
     * Returns the encounter associated with this creature, or null if no encounter
     * has been set
     *
     * @return the encounter for this Creature
     */

    public Encounter getEncounter()
    {
        return this.encounter;
    }

    /**
     * Sets the Encounter for this Creature
     *
     * @param encounter the encounter for this creature.  The encounter is the group
     *                  of creatures with like faction and can share some AI
     */

    public void setEncounter(Encounter encounter)
    {
        this.encounter = encounter;
    }

    /*
     * Set the location and then recompute visibility (non-Javadoc)
     * @see main.java.hale.entity.Entity#setLocation(main.java.hale.entity.Location)
     */

    @Override
    public boolean setLocation(Location newLocation)
    {
        Location oldLocation = this.getLocation();
        Point oldScreen = this.getLocation().getScreenPoint();
        Point newScreen = newLocation.getScreenPoint();

        super.setLocation(newLocation);

        // offset animation positions for inventory
        for (Inventory.Slot slot : Inventory.Slot.values()) {
            if (inventory.getEquippedItem(slot) == null) continue;

            inventory.getEquippedItem(slot).getEffects().offsetAnimationPositions(newScreen.x - oldScreen.x, newScreen.y - oldScreen.y);
        }

        // run onExit and onEnter scripts for effects
        List<Effect> oldEffects;
        if (oldLocation.getArea() == null) {
            oldEffects = Collections.emptyList();
        } else {
            oldEffects = oldLocation.getEffects();
        }

        List<Effect> newEffects;
        if (newLocation.getArea() == null) {
            newEffects = Collections.emptyList();
        } else {
            newEffects = newLocation.getEffects();
        }

        for (Effect effect : oldEffects) {
            // dont run onExit scripts if this creature is the direct target (i.e. if the effect is an aura)
            if (effect.getTarget() != this && !newEffects.contains(effect)) {
                effect.executeFunction(ScriptFunctionType.onTargetExit, this, effect);
            }
        }

        for (Effect effect : newEffects) {
            // dont run onEnter scripts if this creature is the direct target (i.e. if the effect is an aura)
            if (effect.getTarget() != this && !oldEffects.contains(effect)) {
                effect.executeFunction(ScriptFunctionType.onTargetEnter, this, effect);
            }
        }

        computeVisibility();

        // check for interruptions
        boolean interrupted = false;

        Trap trapAtNewLocation = newLocation.getTrap();
        if (trapAtNewLocation != null) {
            if (trapAtNewLocation.checkSpringTrap(this)) {
                interrupted = true;
            }
        }

        if (isPlayerFaction()) {
            // fire any scripts for triggers
            newLocation.getArea().checkPlayerMoved(this);

            // do any search checks for traps
            for (Entity entity : newLocation.getArea().getEntities()) {
                if (!(entity instanceof Trap)) {
                    continue;
                }

                if (!hasVisibilityInCurrentArea(entity.getLocation().getX(), entity.getLocation().getY())) {
                    continue;
                }

                if (((Trap)entity).attemptSearch(this)) {
                    interrupted = true;
                }
            }
        }

        setOffsetPoint(0, 0);

        return interrupted;
    }

    /**
     * Causes this creature to perform search checks against any hidden creatures in its
     * line of sight
     */

    public void searchForHidingCreatures()
    {
        // we only allow searching once per round per creature
        // this in theory is reset every round.  Currently, you can shuffle round timer orders
        // using CombatRunner.activateCreatureWait and get around this
        if (alreadySearchedForHiddenCreatures) return;

        alreadySearchedForHiddenCreatures = true;

        for (Creature creature : getVisibleCreatures()) {
            if (!creature.stats.isHidden()) continue;

            // only search for hostiles
            if (creature.getFaction().isHostile(this)) {
                performSearchCheck(creature, 0);
            }
        }
    }

    /**
     * Causes this creature to perform a search check for the specified target, with the specified
     * extra penalty added to the difficulty of the check.  The difficulty is also affected by the
     * distance between this creature and the target, concealment, and the target's hide skill
     *
     * @param target
     * @param penalty
     * @return true if the search check succeeded, false otherwise
     */

    public boolean performSearchCheck(Creature target, int penalty)
    {
        int baseDifficulty = Game.ruleset.getValue("SearchCheckCreatureBaseDifficulty");
        int distanceMultiplier = Game.ruleset.getValue("SearchCheckDistanceMultiplier");

        int checkPenalty = distanceMultiplier * getLocation().getDistance(target.getLocation()) + baseDifficulty;
        int concealment = Math.min(100, Game.curCampaign.curArea.getConcealment(this, target));

        int difficulty = target.skills.getTotalModifier("Hide") + checkPenalty + concealment - penalty;

        int check = skills.getCheck("Search", difficulty);

        if (check >= difficulty) {
            // deactivate hide mode
            for (AbilitySlot slot : target.abilities.getSlotsWithReadiedAbility("Hide")) {
                slot.deactivate();
            }

            Game.areaListener.getCombatRunner().checkAIActivation();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns true if and only if this creature has an encounter defined and that encounter
     * is set to AI active, or if this creature is a summoned party member
     *
     * @return whether this creature is ai active
     */

    public boolean isAIActive()
    {
        if (isPlayerFaction() && isSummoned()) return true;

        if (encounter == null) return false;

        return encounter.isAIActive();
    }

    /**
     * Returns true if this creature has visibility for the specified point
     * in the area it is currently within, false otherwise
     *
     * @param x the x grid coordinate
     * @param y the y grid coordinate
     * @return whether this creature has visibility at the specified coordinates
     */

    public final boolean hasVisibilityInCurrentArea(int x, int y)
    {
        return visibility[x][y];
    }

    /**
     * Returns true if this creature currently has visibility at the specified location,
     * false otherwise
     *
     * @param location
     * @return whether this creature has visibility at the specified location
     */

    public final boolean hasVisibility(Location location)
    {
        if (location.getArea() != this.getLocation().getArea()) {
            return false;
        }

        return visibility[location.getX()][location.getY()];
    }

    /**
     * Computes the tiles that are currently visible for this creature and saves
     * that information in this creature's visibility matrix.  This method will
     * automatically be called whenever the creature's location changes
     */

    public void computeVisibility()
    {
        Location location = getLocation();

        int width = location.getArea().getWidth();
        int height = location.getArea().getHeight();

        // recreate the matrix if it is not of the correct size
        if (visibility == null || visibility.length != width || visibility[0].length != height) {
            visibility = new boolean[width][height];
        }

        location.getArea().getUtil().setVisibilityWithRespectToPosition(visibility,
                location.getX(), location.getY());
    }

    /**
     * Adds the visibility of this creature to the specified matrix.  Points that are already
     * visible (true) in the matrix are not affected.  Points that are false will be set to true
     * if this Creature has visibility on that point.
     * <p>
     * Note that passing a matrix that is not of the correct size for this creature location's
     * area will result in an exception.  This creature must have a set location before calling
     * this function
     *
     * @param matrix the matrix to set the visibility of
     */

    public void addVisibilityToMatrix(boolean[][] matrix)
    {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] = visibility[i][j] || matrix[i][j];
            }
        }
    }

    /**
     * Returns true if this creature can potentially take a movement caused
     * Attack of Opportunity (AoO) against the target this round, false otherwise.
     * This means that this creature has at least one AoO available, is hostile
     * to the target, and has not taken any move AoOs against the target previously
     * this round.  It does not check the current position of this creature or the
     * target
     *
     * @param target the target Creature
     * @return whether a move AoO is possible against the target
     */

    public boolean canTakeMoveAoOIgnoringLocation(Creature target)
    {
        if (!this.getFaction().isHostile(target)) return false;

        if (this.attacksOfOpportunityAvailable < 1) return false;

        if (this.moveAoOsThisRound.contains(target)) return false;

        return true;
    }

    /**
     * The specified target is added to the list of creatures that this
     * creature has taken a move based attack of opportunity (AoO) against this round.
     * Creatures may only make one move based AoO per target per round
     *
     * @param target
     */

    public void takeMoveAoO(Creature target)
    {
        moveAoOsThisRound.add(target);
    }

    /**
     * Decrements the count of available attacks of opportuntity for this creature. If the
     * count reaches zero, this creature will no longer threaten locations and may not take
     * any further attacks of opportunity until the next round.
     */

    public void takeAttackOfOpportunity()
    {
        this.attacksOfOpportunityAvailable--;
    }

    /**
     * Returns the current off hand weapon wielded by this creature, or null if no
     * weapon is being wielded off hand
     *
     * @return the off hand weapon
     */

    public Weapon getOffHandWeapon()
    {
        EquippableItem item = inventory.getEquippedItem(Inventory.Slot.OffHand);

        if (item instanceof Weapon) {
            return (Weapon)item;
        } else {
            return null;
        }
    }

    /**
     * Returns the current main hand weapon for this creature.  This is either the
     * weapon equipped in this creature's main hand, or if no weapon is equipped,
     * the racial default weapon for this creature
     *
     * @return the current main hand weapon
     */

    public Weapon getMainHandWeapon()
    {
        EquippableItem item = this.inventory.getEquippedItem(Inventory.Slot.MainHand);

        if (item == null) {
            return getDefaultWeapon();
        } else {
            return (Weapon)item;
        }
    }

    /**
     * Returns the current default weapon of this creature.  This is normally the racial default weapon
     *
     * @return the current default weapon
     */

    public Weapon getDefaultWeapon()
    {
        if (unarmedWeapon == null) {
            WeaponTemplate bestWeapon = (WeaponTemplate)EntityManager.getItemTemplate(unarmedWeapons.get(0));
            ;
            for (int i = 1; i < unarmedWeapons.size(); i++) {
                WeaponTemplate curWeapon = (WeaponTemplate)EntityManager.getItemTemplate(unarmedWeapons.get(i));

                if (curWeapon.getAverageDamagePerAP() > bestWeapon.getAverageDamagePerAP()) {
                    bestWeapon = curWeapon;
                }
            }

            unarmedWeapon = (Weapon)EntityManager.getItem(bestWeapon.getID(), bestWeapon.getDefaultQuality());
        }

        return unarmedWeapon;
    }

    /**
     * Adds the specified weapon template as a possible default weapon for this creature.  the actual
     * default weapon will then be the strongest (most average damage per action point) weapon of the available default weapons
     *
     * @param templateID the weapon template to add
     */

    public void addDefaultWeapon(String templateID)
    {
        unarmedWeapons.add(templateID);

        unarmedWeapon = null; // recompute the best unarmed weapon next time we ask for it
    }

    /**
     * Returns true if this creature's weapon currently threatens the specified location,
     * meaning that creatures provoking an attack of opportunity from that location will
     * suffer an attack of opportunity from this creature.
     *
     * @param location the location to verify
     * @return whether or not this creature threatens the specified location with attacks
     * of opportunity
     */

    public boolean threatensLocation(Location location)
    {
        // can only threaten creatures in the same area
        if (location.getArea() != this.getLocation().getArea()) return false;

        return threatensPointInCurrentArea(location.getX(), location.getY());
    }

    /**
     * Returns true if this creature currently threatens the specified point in the area
     * that this creature is located in currently.  See {@link #threatensLocation(Location)}
     *
     * @param x
     * @param y
     * @return whether or not this creature threatens the specified point with attacks
     * of opportunity
     */

    public boolean threatensPointInCurrentArea(int x, int y)
    {
        if (this.isDying() || this.isDead()) return false;

        if (this.attacksOfOpportunityAvailable < 1) return false;

        if (this.stats.isHelpless()) return false;

        // must be able to see the tile
        if (!this.visibility[x][y]) return false;

        Weapon weapon = getMainHandWeapon();
        if (!weapon.getTemplate().threatensAoOs()) return false;

        int thisX = getLocation().getX();
        int thisY = getLocation().getY();

        int distance = AreaUtil.distance(thisX, thisY, x, y);
        if (distance > weapon.getTemplate().getMaxRange() || distance < weapon.getTemplate().getMinRange()) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if this Creature is currently capable of executing a standard attack against
     * the specified position.  This creature's weapon must be capable of reaching the location,
     * and this creature must have enough Action Points to perform the attack.  If this creature's
     * weapon requires ammo, it must be equipped
     *
     * @param location
     * @return whether this creature can attack the location
     */

    public boolean canAttack(Location location)
    {
        if (!timer.canAttack()) return false;

        // can't attack across areas
        if (location.getArea() != this.getLocation().getArea()) return false;

        if (stats.isHelpless()) return false;

        if (!visibility[location.getX()][location.getY()]) return false;

        // can only attack on the same elevation
        if (getLocation().getElevation() != location.getElevation()) return false;

        int distance = location.getDistance(this);

        Weapon weapon = getMainHandWeapon();

        // check the range requirement
        if (distance > weapon.getTemplate().getMaxRange() || distance < weapon.getTemplate().getMinRange()) {
            return false;
        }

        // ranged weapons have ammo
        if (weapon.getTemplate().getWeaponType() == WeaponTemplate.Type.Ranged) {
            Ammo quiver = inventory.getEquippedQuiver();

            if (!weapon.getTemplate().isAmmoForThisWeapon(quiver)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Gives this Creature the specified number of temporary hit points.
     * Temporary hit points act as normal hit points, but only last for a
     * limited time, at which point any unused points are removed.  When taking
     * damage, temporary hit points are lost first.
     *
     * @param amount
     */

    public void addTemporaryHitPoints(int amount)
    {
        // can't heal dead creatures
        if (isDead()) return;

        this.temporaryHitPoints += amount;

        this.updateListeners();
    }

    /**
     * Removes up to the specified quantity of temporary hit points.  If this
     * Creature has fewer temporary hit points than what is specified, the Creature's
     * temporary hit points are set to zero.  This method will not affect the Creature's
     * ordinary hit point pool.
     * <p>
     * See {@link #addTemporaryHitPoints(int)}
     *
     * @param amount
     */

    public void removeTemporaryHitPoints(int amount)
    {
        if (this.temporaryHitPoints > amount) {
            takeDamage(amount, "Effect");
        } else {
            takeDamage(this.temporaryHitPoints, "Effect");
        }
    }

    /**
     * Applies the specified amount of damage of the specified type to this Creature.
     * The damage is modified by this Creature's resistances.  Temporary hit points
     * are lost before ordinary hit points.
     *
     * @param amount
     * @param type
     */

    public void takeDamage(int amount, String type)
    {
        Damage damage = new Damage(this, Game.ruleset.getDamageType(type), amount);
        damage.computeAppliedDamage();
        takeDamage(damage);
    }

    /**
     * Applies the specified damage to this creature.  The damage will be modified
     * by this Creature's resistances.  Temporary hit points are lost before ordinary
     * hit points.
     *
     * @param damage
     */

    public void takeDamage(Damage damage)
    {
        // dead creatures can't take damage
        if (isDead()) return;

        getEffects().executeOnAll(ScriptFunctionType.onDamaged, damage);

        int damageLeftToApply = damage.getTotalAppliedDamage();

        Game.mainViewer.addFadeAway(Integer.toString(damageLeftToApply), getLocation().getX(),
                getLocation().getY(), new Color(0xFFFF2200));

        if (temporaryHitPoints > damageLeftToApply) {
            temporaryHitPoints -= damageLeftToApply;
            damageLeftToApply = 0;
        } else {
            damageLeftToApply -= temporaryHitPoints;
            temporaryHitPoints = 0;
        }

        currentHitPoints -= damageLeftToApply;

        // show a message about the applied damage
        Game.mainViewer.addMessage("red", damage.getMessage());

        if (isDead()) {
            Game.mainViewer.updateEntity(this);
        }

        this.updateListeners();
    }

    /**
     * Heals the specified number of hit points
     *
     * @param amount the number of hit points to heal this creature
     */

    public void healDamage(int amount)
    {
        // can't heal dead creatures
        if (isDead()) return;

        amount = Math.max(0, Math.min(amount, stats.getMaxHP() - currentHitPoints));

        currentHitPoints += amount;

        Game.mainViewer.addMessage("blue", getTemplate().getName() + " was healed for " + amount + " hit points.");

        Game.mainViewer.addFadeAway(Integer.toString(amount), getLocation().getX(),
                getLocation().getY(), new Color(0xFF33CCFF));

        this.updateListeners();
    }

    /**
     * If this creature is dead, raises it.  Its hit points are set to 1.  Note that
     * this method is the only way of healing a creature whose hit points have reached
     * -20.
     */

    public void raiseFromDead()
    {
        if (!isDead()) return;

        this.temporaryHitPoints = 0;
        this.currentHitPoints = 1;

        Game.mainViewer.addFadeAway("Raised", getLocation().getX(),
                getLocation().getY(), new Color(0xFF33CCFF));
        Game.mainViewer.addMessage("blue", getTemplate().getName() + " was raised.");
    }

    /**
     * Returns true if this creature is dying, meaning it is unable to take
     * any actions and is losing hit points.  Creatures become dying when they
     * reach 0 hit points.  If a dying creature is not healed, it will eventually
     * reach -20 hit points and die.
     *
     * @return whether this creature is dying
     */

    public boolean isDying()
    {
        return getCurrentHitPoints() < 1 && getCurrentHitPoints() > -20;
    }

    /**
     * Returns true if and only if this creature has reached -20 or fewer hit points
     * and is dead.
     *
     * @return whether this creature is dead
     */

    public boolean isDead()
    {
        if (!isPlayerFaction() || isSummoned()) {
            return getCurrentHitPoints() <= 0;
        } else {
            return getCurrentHitPoints() <= -20;
        }
    }

    /**
     * Finds a path from this creature's location to a point the specified distance away from the target
     * location.  If the distance away is zero, finds a path to exactly the target location.
     * If no path can be found, returns null
     *
     * @param target
     * @param distanceAway
     * @return a path from this location to the new location
     */

    public Path findPathTo(Location target, int distanceAway)
    {
        return getLocation().getArea().getUtil().findShortestPath(this, target.toPoint(), distanceAway);
    }

    /**
     * Starts the process of performing an attack against the specified creature.
     * AP is deducted from this creature's timer and the attack is created.
     *
     * @param target
     * @return the newly created attack
     */

    public Attack performMainHandAttack(Creature target)
    {
        timer.performAttack();

        return new Attack(this, target, Inventory.Slot.MainHand);
    }

    /**
     * Starts the process of performing an off hand attack against the specified creature.
     * AP is not deducted; it is assumed that the amount is deducted separately when
     * performing the main hand attack
     *
     * @param target
     * @return the newly created attack
     */

    public Attack performOffHandAttack(Creature target)
    {
        return new Attack(this, target, Inventory.Slot.OffHand);
    }

    /**
     * Starts the process of performing a single attack against the specified creature.  The single
     * attack does not cost any Action Points.  This method is most frequently used for attacks of
     * opportunity but can also be used for any other attack that should not cost action points
     *
     * @param target
     * @return the newly created attack
     */

    public Attack performSingleAttack(Creature target, Inventory.Slot slot)
    {
        return new Attack(this, target, slot);
    }

    @Override
    public void areaDraw(int x, int y)
    {
        renderer.drawCentered(x + animatingOffset.x, y + animatingOffset.y, Game.TILE_SIZE, Game.TILE_SIZE);
    }

    @Override
    public void uiDraw(int x, int y)
    {
        renderer.draw(x, y);
    }

    /**
     * Returns the renderer for this creature's icon.  This can be the icon itself or
     * an external renderer
     *
     * @return the icon renderer for this creature
     */

    public IconRenderer getIconRenderer()
    {
        return renderer;
    }

    /**
     * Returns true if the icon renderer for this creature is a SubIconRenderer,
     * false otherwise
     *
     * @return whether this creature is rendered with subIcons
     */

    public boolean drawsWithSubIcons()
    {
        return renderer instanceof SubIconRenderer;
    }

    /**
     * Returns the screen position for sub icons of the specified type
     *
     * @param subIconType
     * @return the screen position
     */

    public Point getSubIconScreenPosition(String subIconType)
    {
        Point screen = getLocation().getScreenPoint();

        SubIcon subIcon = ((SubIconRenderer)renderer).getSubIcon(subIconType);
        Point offset = subIcon.getOffset();

        screen.x += offset.x + subIcon.getWidth() / 2;
        screen.y += offset.y + subIcon.getHeight() / 2;

        return screen;
    }

    /**
     * Called whenever an item is equipped, to add the subIcon associated
     * with that item
     *
     * @param item
     * @param slot the inventory slot being equipped into
     */

    protected void addSubIcon(EquippableItem item, Inventory.Slot slot)
    {
        if (!(renderer instanceof SubIconRenderer)) return;

        SimpleIcon icon = item.getTemplate().getSubIcon();
        if (icon == null) return;

        // first check if this item has a sub icon override
        SubIcon.Type type = item.getTemplate().getSubIconTypeOverride();
        if (type == null) {
            // if it does not, use the default setting from the inventory slot
            type = slot.getSubIconType(item);
        }

        if (type == null) return;

        if (!template.getRace().drawsSubIconType(type)) return;

        SubIcon.Factory factory = new SubIcon.Factory(type, template.getRace(), template.getGender());
        factory.setPrimaryIcon(icon.getSpriteID(), icon.getColor());
        factory.setSecondaryIcon(null, ((ComposedCreatureIcon)template.getIcon()).getClothingColor());
        factory.setCoversBeard(item.getTemplate().coversBeard());
        factory.setCoversHair(item.getTemplate().coversHair());

        ((SubIconRenderer)renderer).add(factory.createSubIcon());
    }

    /**
     * Called whenever an item is unequipped, to remove the subIcon
     * associated with that item
     *
     * @param item
     * @param slot the inventory slot being unequipped from
     */

    protected void removeSubIcon(EquippableItem item, Inventory.Slot slot)
    {
        if (!(renderer instanceof SubIconRenderer)) return;

        // first check if this item has a sub icon override
        SubIcon.Type type = item.getTemplate().getSubIconTypeOverride();

        if (type == null) {
            // if it does not, use the default setting from the inventory slot
            type = slot.getSubIconType(item);
        }

        if (type == null) return;

        ((SubIconRenderer)renderer).remove(type);
    }

    @Override
    public boolean elapseTime(int numRounds)
    {
        super.elapseTime(numRounds);

        moveAoOsThisRound.clear();

        inventory.elapseTime(numRounds);

        timer.reset();

        this.attacksOfOpportunityAvailable = stats.getAttacksOfOpportunity();

        if (isSummoned() && summonExpiration <= Game.curCampaign.getDate().getTotalRoundsElapsed()) {
            this.currentHitPoints = -20;
            this.temporaryHitPoints = 0;
            Game.mainViewer.updateEntity(this);
        }

        this.alreadySearchedForHiddenCreatures = false;

        boolean returnValue = abilities.elapseTime(numRounds);

        updateListeners();

        return returnValue;
    }

    /**
     * Resets time for this Creature, meaning hit points are fully restored
     */

    public void resetTime()
    {
        stats.recomputeAllStats();
        timer.reset();

        if (!isDead()) {
            temporaryHitPoints = 0;
            currentHitPoints = stats.get(Stat.MaxHP);
        }

        elapseTime(1);
    }

    /**
     * Causes all animations on this creature and all held items to end
     */

    public void endAllAnimations()
    {
        inventory.endAllAnimations();

        getEffects().endAllAnimations();
    }

    /**
     * Gets the list of creatures that are visible to this creature
     *
     * @return the list of creatures
     */

    public List<Creature> getVisibleCreatures()
    {
        return getLocation().getArea().getEntities().getVisibleCreatures(this);
    }

    @Override
    protected void applyEffectBonuses(Effect effect)
    {
        if (inventory == null) {
            // inventory being null indicates that we are in the constructor and
            // this object is not fully initialized
            // in which case, we cannot yet compute our stats
            stats.addAllNoRecompute(effect.getBonuses());
        } else {
            stats.addAll(effect.getBonuses());
        }
    }

    @Override
    protected void removeEffectBonuses(Effect effect)
    {
        stats.removeAll(effect.getBonuses());
    }

    @Override
    public int compareTo(Entity other)
    {
        if (other instanceof Creature) return super.compareTo(other);

        return 1;
    }
}
