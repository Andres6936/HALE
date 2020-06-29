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

import java.util.LinkedList;
import java.util.List;

import hale.Game;
import hale.entity.Creature;
import hale.rules.Damage;

public class StandaloneDamageBonusList
{
    private List<StandaloneDamageBonus> bonuses;

    public StandaloneDamageBonusList()
    {
    }

    public StandaloneDamageBonusList(StandaloneDamageBonusList other)
    {

        if (other.bonuses != null) {
            bonuses = new LinkedList<StandaloneDamageBonus>(other.bonuses);
        }
    }

    public void add(Bonus bonus)
    {
        if (bonuses == null) bonuses = new LinkedList<StandaloneDamageBonus>();

        if (bonus.getType() == Bonus.Type.StandaloneDamage) {
            bonuses.add((StandaloneDamageBonus)bonus);
        }
    }

    public void remove(Bonus bonus)
    {
        bonuses.remove(bonus);
    }

    public boolean isEmpty()
    {
        return bonuses != null && bonuses.size() > 0;
    }

    public void clear()
    {
        if (bonuses != null) bonuses.clear();
    }

    public Damage roll(Creature parent)
    {
        Damage damage = new Damage(parent);

        if (bonuses == null) return damage;

        for (StandaloneDamageBonus bonus : bonuses) {
            int randDamage;
            if (bonus.getMinDamage() == bonus.getMaxDamage()) {
                randDamage = bonus.getMinDamage();
            } else {
                randDamage = Game.dice.rand(bonus.getMinDamage(), bonus.getMaxDamage());
            }

            damage.add(Game.ruleset.getDamageType(bonus.getDamageType()), randDamage);
        }

        return damage;
    }
}
