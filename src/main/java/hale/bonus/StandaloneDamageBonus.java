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

import hale.loading.JSONOrderedObject;
import hale.util.SimpleJSONObject;

public class StandaloneDamageBonus extends IntBonus
{
    private final String damageType;
    private final int minDamage;
    private final int maxDamage;

    @Override
    public JSONOrderedObject save()
    {
        JSONOrderedObject data = new JSONOrderedObject();

        data.put("class", this.getClass().getName());
        data.put("standaloneDamageType", damageType);
        data.put("minDamage", minDamage);
        data.put("maxDamage", maxDamage);

        return data;
    }

    public static StandaloneDamageBonus load(SimpleJSONObject data)
    {
        String damageType = data.get("standaloneDamageType", null);
        int minDamage = data.get("minDamage", 0);
        int maxDamage = data.get("maxDamage", 0);

        return new StandaloneDamageBonus(damageType, minDamage, maxDamage);
    }

    public StandaloneDamageBonus(String damageType, int minDamage, int maxDamage)
    {
        super(Bonus.Type.StandaloneDamage, Bonus.StackType.GenericBonus, (minDamage + maxDamage) / 2);

        this.damageType = damageType;
        this.minDamage = minDamage;
        this.maxDamage = maxDamage;
    }

    public int getMinDamage()
    {
        return minDamage;
    }

    public int getMaxDamage()
    {
        return maxDamage;
    }

    public String getDamageType()
    {
        return damageType;
    }

    @Override
    public StandaloneDamageBonus cloneWithReduction(int reduction)
    {
        return new StandaloneDamageBonus(this.damageType, this.minDamage - reduction, this.maxDamage - reduction);
    }

    @Override
    public void appendDescription(StringBuilder sb)
    {
        sb.append("Extra Damage: ");
        sb.append("<span style=\"font-family: red\">");
        sb.append(minDamage);

        if (minDamage != maxDamage) {
            sb.append("</span> to <span style=\"font-family: red\">");
            sb.append(maxDamage);
        }

        sb.append("</span> ");
        sb.append(damageType);
    }
}
