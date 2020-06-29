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

import main.java.hale.Game;
import main.java.hale.icon.SimpleIcon;
import main.java.hale.rules.BaseWeapon;
import main.java.hale.rules.DamageType;
import main.java.hale.util.SimpleJSONObject;

/**
 * A template for a weapon, either ranged, thrown, or melee
 *
 * @author Jared
 */

public class WeaponTemplate extends EquippableItemTemplate
{
    private final Type type;
    private final Handed handed;
    private final BaseWeapon baseWeapon;

    // the projectile icon, only valid for thrown weapons
    private final SimpleIcon projectileIcon;

    // whether this weapon can do attacks of opportunity
    private final boolean threatensAoOs;

    private final int minRange, maxRange;

    private final DamageType damageType;
    private final int minDamage, maxDamage;

    // an attack roll (1 to 100) must be higher than this value to score a critical threat
    private final int criticalThreat;
    private final int criticalMultiplier;

    // the cost of performing a single attack with this weapon in AP
    private final int attackCost;

    // the penalty for attacking targets at a distance with this weapon
    // attack is penalized by the rangePenalty times the distance in hexes divided by 100
    // this value is usually only applicable to ranged weapons
    private final int rangePenalty;

    // the maximum / minimum amount of damage bonus allowed for this weapon as a percentage
    private final int maxStrengthBonus, minStrengthBonus;

    private final double averageDamagePerAP;

    /**
     * The basic weapon type - melee, thrown, or ranged
     *
     * @author Jared
     */

    public enum Type
    {
        Melee, Thrown, Ranged;
    }

    /**
     * Whether this weapon is held in one hand as a light weapon, one hand, or two hands
     *
     * @author Jared
     */

    public enum Handed
    {
        Light("Light"), OneHanded("One Handed"), TwoHanded("Two Handed");

        private Handed(String name)
        {
            this.name = name;
        }

        public String name;
    }

    /**
     * Creates a new WeaponTemplate
     *
     * @param id   the entity ID
     * @param data the JSON parser
     */

    public WeaponTemplate(String id, SimpleJSONObject data)
    {
        super(id, data);

        this.type = Type.valueOf(data.get("weaponType", null));
        this.handed = Handed.valueOf(data.get("handed", null));
        this.baseWeapon = Game.ruleset.getBaseWeapon(data.get("baseWeapon", null));

        this.threatensAoOs = data.get("threatensAoOs", false);

        this.minRange = data.get("minRange", 0);
        this.maxRange = data.get("maxRange", 0);

        this.damageType = Game.ruleset.getDamageType(data.get("damageType", null));

        this.minDamage = data.get("minDamage", 0);
        this.maxDamage = data.get("maxDamage", 0);

        this.criticalThreat = data.get("criticalThreat", 0);
        this.criticalMultiplier = data.get("criticalMultiplier", 0);

        this.attackCost = data.get("attackCost", 0);

        this.rangePenalty = data.get("rangePenalty", 0);
        this.maxStrengthBonus = data.get("maxStrengthBonus", 0);
        this.minStrengthBonus = data.get("minStrengthBonus", -100);

        if (data.containsKey("projectileIcon") && type == Type.Thrown) {
            // the projectile icon must be a simple icon for animation purposes
            projectileIcon = new SimpleIcon(data.getObject("projectileIcon"));
        } else {
            projectileIcon = null;
        }

        this.averageDamagePerAP = computeAverageDamagePerAP();
    }

    private WeaponTemplate(String id, WeaponTemplate other, CreatedItem createdItem)
    {
        super(id, other, createdItem);

        this.type = other.type;
        this.handed = other.handed;
        this.baseWeapon = other.baseWeapon;
        this.projectileIcon = other.projectileIcon;
        this.threatensAoOs = other.threatensAoOs;
        this.minRange = other.minRange;
        this.maxRange = other.maxRange;
        this.damageType = other.damageType;
        this.minDamage = other.minDamage;
        this.maxDamage = other.maxDamage;
        this.criticalThreat = other.criticalThreat;
        this.criticalMultiplier = other.criticalMultiplier;
        this.attackCost = other.attackCost;
        this.rangePenalty = other.rangePenalty;
        this.maxStrengthBonus = other.maxStrengthBonus;
        this.minStrengthBonus = other.minStrengthBonus;

        this.averageDamagePerAP = computeAverageDamagePerAP();
    }

    private double computeAverageDamagePerAP()
    {
        double avgDamageBase = (minDamage + maxDamage) / 2.0;
        double critChance = ((100 - criticalThreat) + 1) / 100.0;
        double avgDamage = avgDamageBase * (1 - critChance) + avgDamageBase * criticalMultiplier * critChance;

        return avgDamage / ((double)attackCost);
    }

    @Override
    public WeaponTemplate createModifiedCopy(String id, CreatedItem createdItem)
    {
        return new WeaponTemplate(id, this, createdItem);
    }

    /**
     * Returns the computed average damage per AP, taking into account min and max damage, critical threat range, critical multiplier
     * and AP cost
     *
     * @return the computed average damage per AP
     */

    public double getAverageDamagePerAP()
    {
        return averageDamagePerAP;
    }

    /**
     * Returns the icon that is drawn in the area to represent this weapon as a projectile.
     * This can only be non-null for thrown weapons
     *
     * @return the projectile icon
     */

    public SimpleIcon getProjectileIcon()
    {
        return projectileIcon;
    }

    @Override
    public Weapon createInstance()
    {
        return new Weapon(this);
    }

    /**
     * Returns the WeaponType of this weapon
     *
     * @return the WeaponType
     */

    public Type getWeaponType()
    {
        return type;
    }

    /**
     * Returns whether this weapon is a light, one handed, or two handed weapon
     *
     * @return the handedness
     */

    public Handed getHanded()
    {
        return handed;
    }

    /**
     * Returns the BaseWeapon that this weapon is an example of
     *
     * @return the baseWeapon
     */

    public BaseWeapon getBaseWeapon()
    {
        return baseWeapon;
    }

    /**
     * Whether this weapon is capable of performing attacks of opportunity (AoOs).  Generally,
     * melee weapons threaten AoOs but ranged weapons do not.
     *
     * @return true if this weapon threatens AoOs, false otherwise
     */

    public boolean threatensAoOs()
    {
        return threatensAoOs;
    }

    /**
     * Returns the minimum range (in hexes) for this weapon.  Most weapons have a minimum range
     * of 1, but a few weapons cannot attack adjacent targets and have higher minimum
     * ranges
     *
     * @return the minimum range
     */

    public int getMinRange()
    {
        return minRange;
    }

    /**
     * Returns the maximum range (in hexes) for this weapon.  Most melee weapons have a maximum
     * range of 1, but some have higher.  Most ranged weapons have much higher maximum ranges.
     *
     * @return the maximum range in hexes
     */

    public int getMaxRange()
    {
        return maxRange;
    }

    /**
     * Returns the damageType that this weapon does.  Weapons can do
     * additional damage types via bonuses (see {@link main.java.hale.bonus.StandaloneDamageBonus})
     *
     * @return the damage type of this weapon
     */

    public DamageType getDamageType()
    {
        return damageType;
    }

    /**
     * Returns the base minimum amount of damage that this weapon does.
     * Actual damage is based on this value, which is then modified by many
     * other factors including item quality and the wielder's stats
     *
     * @return the base minimum damage
     */

    public int getMinDamage()
    {
        return minDamage;
    }

    /**
     * Returns the base maximum amount of damage that this weapon does.
     * Actual damage is based on this value, which is then modified by many
     * other factors including item quality and the wielder's stats
     *
     * @return the base maximum damage
     */

    public int getMaxDamage()
    {
        return maxDamage;
    }

    /**
     * Returns the value an attack roll must be greater than or equal to in order to
     * qualify as a critical threat.  Once an attack is a critical threat, a second
     * roll is performed that must hit in order to score a critical hit.
     *
     * @return the minimum critical threat value
     */

    public int getCriticalThreat()
    {
        return criticalThreat;
    }

    /**
     * Returns the critical hit multiplier.  Most weapon damage is multiplied by this
     * value on a successful critical hit.
     *
     * @return the critical hit multiplier.
     */

    public int getCriticalMultiplier()
    {
        return criticalMultiplier;
    }

    /**
     * Returns the cost of performing one standard attack with this weapon, in Action Points
     * (AP).  Note that AP displayed on the interface is actually the AP divided by 100.
     *
     * @return the AP cost of attacking with this weapon
     */

    public int getAttackCost()
    {
        return attackCost;
    }

    /**
     * Returns the penalty to attack that is applied based on the target's distance.  A lower
     * value makes hitting targets at long distances easier.  The attack penalty is calculated as
     * the rangePenalty times the distance in hexes, divided by 100.
     *
     * @return the penalty to attack based on distance
     */

    public int getRangePenalty()
    {
        return rangePenalty;
    }

    /**
     * Returns the maximum amount of bonus damage (as a percentage) from the wielder's strength
     * that can be applied when attacking with this weapon.  Many ranged weapons do not give a
     * bonus for strength; in that case this value is 0.
     *
     * @return the maximum strength bonus damage
     */

    public int getMaxStrengthBonus()
    {
        return maxStrengthBonus;
    }

    /**
     * Returns the minimum amount of bonus damage (as a percentage) from the wielder's strength
     * that can be applied when attacking with this weapon.  In practice stat this allows certain
     * weapons (such as crossbows) to not have their damage penalized for low strength (by setting
     * this to 0)
     *
     * @return the minimum strength bonus damage
     */

    public int getMinStrengthBonus()
    {
        return minStrengthBonus;
    }

    @Override
    public boolean hasPrereqsToEquip(Creature parent)
    {
        return parent.stats.hasWeaponProficiency(baseWeapon.getName());
    }

    /**
     * Returns true if the specified ammo is usable by this weapon, false if the ammo
     * is null or not usable by this weapon
     *
     * @param ammo
     * @return whether the specified ammo can be used by this weapon
     */

    public boolean isAmmoForThisWeapon(Ammo ammo)
    {
        if (ammo == null) return false;

        return ammo.getTemplate().isUsableByBaseWeapon(baseWeapon);
    }
}
