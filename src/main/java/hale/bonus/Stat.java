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

package hale.bonus;

public enum Stat
{
    CreatureLevel("Level"), CasterLevel("Caster Level"), LevelAttackBonus("Level Attack Bonus"),
    LevelDamageBonus("Level Damage Bonus"), MaxHP("Maximum Hit Points"),
    BaseStr("Strength"), BaseDex("Dexterity"), BaseCon("Constitution"), BaseInt("Intelligence"), BaseWis("Wisdom"), BaseCha("Charisma"),
    Str("Strength"), Dex("Dexterity"), Con("Constitution"), Int("Intelligence"), Wis("Wisdom"), Cha("Charisma"),
    MentalResistance("Mental Resistance"), PhysicalResistance("Physical Resistance"), ReflexResistance("Reflex Resistance"),
    WeightLimit("Weight Limit"),
    AttacksOfOpportunity("Attacks of Opportunity"),
    ArmorClass("Defense"), TouchArmorClass("Defense to Touch"),
    ShieldAttackPenalty("Shield Attack Penalty"), ArmorPenalty("Armor Penalty"),
    MovementBonus("Movement Bonus"), MovementCost("Movement Cost"),
    InitiativeBonus("Initiative Bonus"),
    AttackCost("Attack Cost"), MainHandAttackBonus("Main Hand Attack Bonus"), MainHandDamageBonus("Main Hand Damage Bonus"),
    OffHandAttackBonus("Off Hand Attack Bonus"), OffHandDamageBonus("Off Hand Damage Bonus"),
    TouchAttackBonus("Touch Attack Bonus");

    private Stat()
    {
        name = toString();
    }

    private Stat(String name)
    {
        this.name = name;
    }

    public final String name;
};
