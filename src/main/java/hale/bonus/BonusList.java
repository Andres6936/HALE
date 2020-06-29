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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import main.java.hale.loading.JSONOrderedObject;
import main.java.hale.loading.Saveable;

public class BonusList implements Iterable<Bonus>, Saveable
{
    private List<Bonus> bonuses;

    @Override
    public Object save()
    {
        Object[] data = new JSONOrderedObject[bonuses.size()];

        int i = 0;
        for (Bonus bonus : bonuses) {
            data[i] = bonus.save();
            i++;
        }

        return data;
    }

    public BonusList()
    {
        this.bonuses = new ArrayList<Bonus>(3);
    }

    public BonusList(BonusList other)
    {
        this.bonuses = new ArrayList<Bonus>(other.bonuses);
    }

    public void remove(Bonus bonus)
    {
        this.bonuses.remove(bonus);
    }

    public void addAll(BonusList other)
    {
        for (Bonus bonus : other.bonuses) {
            this.bonuses.add(bonus);
        }
    }

    public void add(Bonus bonus)
    {
        bonuses.add(bonus);
    }

    public void addStandaloneDamageBonus(String damageType, int damageMin, int damageMax)
    {
        addStandaloneDamageBonus(damageType, Bonus.StackType.GenericBonus, damageMin, damageMax);
    }

    private void addStandaloneDamageBonus(String damageType, Bonus.StackType stackType, int damageMin, int damageMax)
    {
        bonuses.add(new StandaloneDamageBonus(damageType, damageMin, damageMax));
    }

    public void addSpellDamagePenalty(String damageType, int multiple)
    {
        addSpellDamageBonus(damageType, Bonus.StackType.GenericPenalty, multiple);
    }

    public void addSpellDamagePenalty(String damageType, String stackType, int multiple)
    {
        addSpellDamageBonus(damageType, Bonus.parseStackType(stackType + "Penalty"), multiple);
    }

    public void addSpellDamageBonus(String damageType, int multiple)
    {
        addSpellDamageBonus(damageType, Bonus.StackType.GenericBonus, multiple);
    }

    public void addSpellDamageBonus(String damageType, String stackType, int multiple)
    {
        addSpellDamageBonus(damageType, Bonus.parseStackType(stackType + "Bonus"), multiple);
    }

    private void addSpellDamageBonus(String damageType, Bonus.StackType stackType, int multiple)
    {
        bonuses.add(new DamageBonus(Bonus.Type.DamageForSpellType, stackType, damageType, multiple));
    }

    public void addAttackBonusVsRacialType(String racialType, int value)
    {
        addBonusVsRacialType(racialType, Bonus.Type.AttackVsRacialType, Bonus.StackType.GenericBonus, value);
    }

    public void addDamageBonusVsRacialType(String racialType, int value)
    {
        addBonusVsRacialType(racialType, Bonus.Type.DamageVsRacialType, Bonus.StackType.GenericBonus, value);
    }

    public void addArmorClassBonusVsRacialType(String racialType, int value)
    {
        addBonusVsRacialType(racialType, Bonus.Type.ArmorClassVsRacialType, Bonus.StackType.GenericBonus, value);
    }

    private void addBonusVsRacialType(String racialType, Bonus.Type type, Bonus.StackType stackType, int value)
    {
        bonuses.add(new RacialTypeBonus(type, stackType, racialType, value));
    }

    public void addWeaponDamageBonus(String damageType, String stackType, int multiple)
    {
        addWeaponDamageBonus(damageType, Bonus.parseStackType(stackType + "Bonus"), multiple);
    }

    public void addWeaponDamageBonus(String damageType, int multiple)
    {
        addWeaponDamageBonus(damageType, Bonus.StackType.GenericBonus, multiple);
    }

    private void addWeaponDamageBonus(String damageType, Bonus.StackType stackType, int multiple)
    {
        bonuses.add(new DamageBonus(Bonus.Type.DamageForWeaponType, stackType, damageType, multiple));
    }

    public void addWeaponAttackBonus(String damageType, String stackType, int value)
    {
        addWeaponAttackBonus(damageType, Bonus.parseStackType(stackType + "Bonus"), value);
    }

    public void addWeaponAttackBonus(String damageType, int value)
    {
        addWeaponAttackBonus(damageType, Bonus.StackType.GenericBonus, value);
    }

    private void addWeaponAttackBonus(String damageType, Bonus.StackType stackType, int value)
    {
        bonuses.add(new DamageBonus(Bonus.Type.AttackForWeaponType, stackType, damageType, value));
    }

    private void addDamageResistanceBonus(String damageType, Bonus.Type type, Bonus.StackType stackType, int value)
    {
        bonuses.add(new DamageBonus(type, stackType, damageType, value));
    }

    public void addDamageReduction(String damageType, int value)
    {
        addDamageResistanceBonus(damageType, Bonus.Type.DamageReduction, Bonus.StackType.GenericBonus, value);
    }

    public void addDamageReduction(String damageType, String stackType, int value)
    {
        addDamageResistanceBonus(damageType, Bonus.Type.DamageReduction, Bonus.parseStackType(stackType + "Bonus"), value);
    }

    public void addDamageImmunity(String damageType, int value)
    {
        addDamageResistanceBonus(damageType, Bonus.Type.DamageImmunity, Bonus.StackType.GenericBonus, value);
    }

    public void addDamageImmunity(String damageType, String stackType, int value)
    {
        addDamageResistanceBonus(damageType, Bonus.Type.DamageImmunity, Bonus.parseStackType(stackType + "Bonus"), value);
    }

    public void addDamageVulnerability(String damageType, int value)
    {
        addDamageResistanceBonus(damageType, Bonus.Type.DamageImmunity, Bonus.StackType.GenericPenalty, value);
    }

    public void addDamageVulnerability(String damageType, String stackType, int value)
    {
        addDamageResistanceBonus(damageType, Bonus.Type.DamageImmunity, Bonus.parseStackType(stackType + "Bonus"), value);
    }

    public void addWeaponProficiency(String baseWeapon)
    {
        bonuses.add(new WeaponProficiency(baseWeapon));
    }

    public void addBaseWeaponBonus(String baseWeapon, String bonusType, String stackType, int bonus)
    {
        addBaseWeaponBonus(baseWeapon, bonusType, Bonus.parseStackType(stackType + "Bonus"), bonus);
    }

    public void addBaseWeaponBonus(String baseWeapon, String bonusType, int bonus)
    {
        addBaseWeaponBonus(baseWeapon, bonusType, Bonus.StackType.GenericBonus, bonus);
    }

    public void addBaseWeaponPenalty(String baseWeapon, String bonusType, String stackType, int bonus)
    {
        addBaseWeaponBonus(baseWeapon, bonusType, Bonus.parseStackType(stackType + "Penalty"), bonus);
    }

    public void addBaseWeaponPenalty(String baseWeapon, String bonusType, int bonus)
    {
        addBaseWeaponBonus(baseWeapon, bonusType, Bonus.StackType.GenericPenalty, bonus);
    }

    private void addBaseWeaponBonus(String baseWeapon, String bonusType, Bonus.StackType stackType, int bonus)
    {
        bonuses.add(new BaseWeaponBonus(baseWeapon, Bonus.parseType("BaseWeapon" + bonusType), stackType, bonus));
    }

    public void addArmorProficiency(String armorType)
    {
        bonuses.add(new ArmorProficiency(armorType));
    }

    public void addArmorBonus(String armorType, String bonusType, String stackType, int bonus)
    {
        addArmorBonus(armorType, bonusType, Bonus.parseStackType(stackType + "Bonus"), bonus);
    }

    public void addArmorBonus(String armorType, String bonusType, int bonus)
    {
        addArmorBonus(armorType, bonusType, Bonus.StackType.GenericBonus, bonus);
    }

    private void addArmorBonus(String armorType, String bonusType, Bonus.StackType stackType, int bonus)
    {
        bonuses.add(new ArmorTypeBonus(armorType, Bonus.parseType("ArmorType" + bonusType), stackType, bonus));
    }

    private void addSkillBonus(String skillID, Bonus.StackType stackType, int ranks)
    {
        bonuses.add(new SkillBonus(skillID, stackType, ranks));
    }

    public void addSkillPenalty(String skillID, int ranks)
    {
        addSkillBonus(skillID, Bonus.StackType.GenericPenalty, ranks);
    }

    public void addSkillPenalty(String skillID, String stackType, int ranks)
    {
        addSkillBonus(skillID, Bonus.parseStackType(stackType + "Penalty"), ranks);
    }

    public void addSkillBonus(String skillID, String stackType, int ranks)
    {
        addSkillBonus(skillID, Bonus.parseStackType(stackType + "Bonus"), ranks);
    }

    public void addSkillBonus(String skillID, int ranks)
    {
        addSkillBonus(skillID, Bonus.StackType.GenericBonus, ranks);
    }

    public void add(String typeString)
    {
        bonuses.add(new Bonus(Bonus.parseType(typeString), Bonus.StackType.GenericBonus));
    }

    private void add(Bonus.Type type, Bonus.StackType stackType, int value)
    {
        bonuses.add(new IntBonus(type, stackType, value));
    }

    public void addBonus(String typeString, String stackTypeString, int value)
    {
        add(Bonus.parseType(typeString), Bonus.parseStackType(stackTypeString + "Bonus"), value);
    }

    public void addBonus(String typeString, int value)
    {
        add(Bonus.parseType(typeString), Bonus.StackType.GenericBonus, value);
    }

    public void addPenalty(String typeString, int value)
    {
        add(Bonus.parseType(typeString), Bonus.StackType.GenericPenalty, value);
    }

    public void addPenalty(String typeString, String stackTypeString, int value)
    {
        add(Bonus.parseType(typeString), Bonus.parseStackType(stackTypeString + "Penalty"), value);
    }

    public boolean hasBonus(Bonus bonus)
    {
        return bonuses.contains(bonus);
    }

    public boolean hasBonusOfType(String type)
    {
        return hasBonusOfType(Bonus.Type.valueOf(type));
    }

    public boolean hasBonusOfType(Bonus.Type type)
    {
        for (Bonus bonus : bonuses) {
            if (bonus.getType() == type) return true;
        }

        return false;
    }

    public Bonus getBonusOfType(String type)
    {
        return getBonusOfType(Bonus.Type.valueOf(type));
    }

    public Bonus getBonusOfType(Bonus.Type type)
    {
        for (Bonus bonus : bonuses) {
            if (bonus.getType() == type) return bonus;
        }

        return null;
    }

    /**
     * Returns a list of all bonuses in this list of any of the specified types
     *
     * @param types
     * @return a list of bonuses, or an empty list if no such bonuses exist
     */

    public List<Bonus> getBonusesOfType(Bonus.Type... types)
    {
        List<Bonus> bonuses = new ArrayList<Bonus>();

        for (Bonus bonus : this.bonuses) {
            for (Bonus.Type type : types) {
                if (bonus.getType() == type) {
                    bonuses.add(bonus);
                }
            }
        }

        return bonuses;
    }

    public String getDescription()
    {
        StringBuilder str = new StringBuilder();

        for (Bonus bonus : bonuses) {
            str.append("<p>");
            bonus.appendDescription(str);
            str.append("</p>");
        }

        return str.toString();
    }

    public int size()
    {
        return bonuses.size();
    }

    @Override
    public Iterator<Bonus> iterator()
    {
        return new BonusIterator();
    }

    private class BonusIterator implements Iterator<Bonus>
    {
        private int numLeft;

        private BonusIterator()
        {
            this.numLeft = bonuses.size();
        }

        @Override
        public boolean hasNext()
        {
            return (numLeft > 0);
        }

        @Override
        public Bonus next()
        {
            try {
                int index = bonuses.size() - numLeft;
                Bonus result = bonuses.get(index);
                numLeft--;
                return result;

            } catch (IndexOutOfBoundsException e) {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

    }
}
