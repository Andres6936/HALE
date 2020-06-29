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

package main.java.hale.bonus;

import main.java.hale.loading.JSONOrderedObject;
import main.java.hale.loading.Saveable;
import main.java.hale.util.Logger;
import main.java.hale.util.SimpleJSONObject;

public class Bonus implements Saveable
{
    public enum Type
    {
        // non int bonuses (you either have them or you don't)
        DualWieldTraining("Dual Wield Training"), Immobilized, Helpless, Hidden, Blind, Deaf, Silence,
        CriticalHitImmunity("Critical Hit Immunity"), LockPicking("Lock Picking"), TrapHandling("Trap Handling"),
        AoOFromRangedImmunity("AoO From Ranged Immunity"), ImmobilizationImmunity("Immobilization Immunity"),
        UndispellableImmobilized("Immobilized"), UndispellableHelpless("Helpless"),

        // bonuses that apply to an attack with an individual weapon, not to all attacks done by a creature
        StandaloneDamage("Standalone Damage"), WeaponAttack("Attack"), WeaponDamage("Damage"),
        WeaponCriticalChance("Critical Chance"), WeaponCriticalMultiplier("Critical Multiplier"),

        // racial type bonuses
        AttackVsRacialType("Attack"), DamageVsRacialType("Damage"),
        ArmorClassVsRacialType("Defense"),

        // armor type bonuses
        ArmorTypeMovementPenalty("Movement Penalty"), ArmorTypeArmorPenalty("Armor Penalty"),
        ArmorTypeArmorClass("Defense"),

        // base weapon bonuses
        BaseWeaponAttack("Attack"), BaseWeaponDamage("Damage"), BaseWeaponSpeed("Speed"),
        BaseWeaponCriticalChance("Critical Chance"), BaseWeaponCriticalMultiplier("Critical Multiplier"),

        // skill bonuses
        Skill(""),

        // proficiency bonuses
        ArmorProficiency("Proficiency"), WeaponProficiency("Proficiency"),

        // damage bonuses
        DamageImmunity("Damage Immunity"), DamageReduction("Damage Reduction"),
        AttackForWeaponType("Weapon Attack"), DamageForWeaponType("Weapon Damage"),
        DamageForSpellType("Spell Damage"),

        // int bonuses
        BaseStr("Base Strength"), BaseDex("Base Dexterity"), BaseCon("Base Constitution"),
        BaseInt("Base Intelligence"), BaseWis("Base Wisdom"), BaseCha("Base Charisma"),
        Str("Strength"), Dex("Dexterity"), Con("Constitution"),
        Int("Intelligence"), Wis("Wisdom"), Cha("Charisma"),
        Initiative, AttacksOfOpportunity("Attacks of Opportunity"), MentalResistance("Mental Resistance"),
        PhysicalResistance("Physical Resistance"), ReflexResistance("Reflex Resistance"),
        SpellDamage("Spell Damage"), SpellHealing("Spell Healing"), SpellDuration("Spell Duration"),
        SpellResistance("Spell Resistance"), ArmorSpellFailure("Spell Failure due to Armor"),
        VerbalSpellFailure("Verbal Spell Failure"),
        SpellFailure("Spell Success"), MeleeSpellFailure("Spell Success due to Threatening Creatures"),
        SpellCooldown("Spell Cooldown"),
        Attack, Damage, AttackCost("Attack Cost"), ArmorClass("Defense"), ArmorPenalty("Armor Penalty"),
        ShieldAttack("Attack while using a Shield"), CriticalChance, CriticalMultiplier,
        LightMeleeWeaponDamage("Light Melee Weapon Damage"),
        OneHandedMeleeWeaponDamage("One Handed Melee Weapon Damage"),
        TwoHandedMeleeWeaponDamage("Two Handed Melee Weapon Damage"), RangedDamage("Ranged Damage"),
        LightMeleeWeaponAttack("Light Melee Weapon Attack"),
        OneHandedMeleeWeaponAttack("One Handed Melee Weapon Attack"),
        TwoHandedMeleeWeaponAttack("Two Handed Melee Weapon Attack"), RangedAttack("Ranged Attack"),
        MainHandAttack("Main Hand Attack"), OffHandAttack("Off Hand Attack"),
        MainHandDamage("Main Hand Damage"), OffHandDamage("Off Hand Damage"),
        DualWieldAttack("Attack while Dual Wielding"),
        DualWieldStrDamage("Damage Bonus due to Strength while Dual Wielding"),
        DualWieldArmorClass("Defense while Dual Wielding"),
        Concealment, ConcealmentIgnoring("Ignore Concealment on Others"),
        ConcealmentIgnoringRanged("Ignore Concealment on Others with Ranged Weapons"),
        ConcealmentNegation("Concealment Negated"), Movement, RangePenalty("Range Penalty"),
        WeightLimit("Weight Limit"), TemporaryHP("Temporary Hit Points"), ActionPoint("Action Points"),
        ActionPointEquipHands("Action Points to Wield"), FlankingAngle("Flanking Angle");

        private Type()
        {
            this.name = this.toString();
        }

        private Type(String name)
        {
            this.name = name;
        }

        public final String name;
    }

    ;

    public enum StackType
    {
        StackableBonus, StackablePenalty,
        GenericBonus, MoraleBonus, DeflectionBonus, NaturalArmorBonus, ArmorBonus, ShieldBonus,
        GenericPenalty, MoralePenalty, DeflectionPenalty, NaturalArmorPenalty, ArmorPenalty, ShieldPenalty,
        EnhancementBonus, EnhancementPenalty, LuckBonus, LuckPenalty,
    }

    ;

    private final Type type;
    private final StackType stackType;

    @Override
    public JSONOrderedObject save()
    {
        JSONOrderedObject data = new JSONOrderedObject();

        data.put("class", this.getClass().getName());
        data.put("type", type.toString());
        data.put("stackType", stackType.toString());

        return data;
    }

    public static Bonus load(SimpleJSONObject data)
    {
        Bonus.Type type = Type.valueOf(data.get("type", null));
        Bonus.StackType stackType = StackType.valueOf(data.get("stackType", null));

        return new Bonus(type, stackType);
    }

    public Bonus(Type type, StackType stackType)
    {
        this.type = type;
        this.stackType = stackType;
    }

    public boolean hasValue()
    {
        return false;
    }

    public int getValue()
    {
        return 0;
    }

    public Type getType()
    {
        return type;
    }

    public StackType getStackType()
    {
        return stackType;
    }

    public static Type parseType(String typeString)
    {
        Type type = Bonus.Type.valueOf(typeString);
        if (type == null) {
            Logger.appendToErrorLog("Bonus type " + typeString + " not found.");
        }

        return type;
    }

    public static StackType parseStackType(String stackTypeString)
    {
        StackType stackType = Bonus.StackType.valueOf(stackTypeString);
        if (stackType == null) {
            Logger.appendToErrorLog("Bonus stack type " + stackTypeString + " not found.");
            return StackType.GenericBonus;
        }

        return stackType;
    }

    public Bonus cloneWithReduction(int reduction)
    {
        return new Bonus(this.type, this.stackType);
    }

    public void appendDescription(StringBuilder sb)
    {
        sb.append(type.name);
    }
}
