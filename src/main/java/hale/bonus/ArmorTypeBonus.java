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

public class ArmorTypeBonus extends IntBonus implements BonusWithSuperType
{
    private final String armorType;

    @Override
    public JSONOrderedObject save()
    {
        JSONOrderedObject data = super.save();

        data.put("armorType", armorType);

        return data;
    }

    public static ArmorTypeBonus load(SimpleJSONObject data)
    {
        int value = data.get("value", 0);
        Bonus.Type type = Type.valueOf(data.get("type", null));
        Bonus.StackType stackType = StackType.valueOf(data.get("stackType", null));
        String armorType = data.get("armorType", null);

        return new ArmorTypeBonus(armorType, type, stackType, value);
    }

    public ArmorTypeBonus(String armorType, Bonus.Type type, Bonus.StackType stackType, int value)
    {
        super(type, stackType, value);

        this.armorType = armorType;
    }

    public String getArmorType()
    {
        return armorType;
    }

    @Override
    public String getSuperType()
    {
        return armorType;
    }

    @Override
    public ArmorTypeBonus cloneWithReduction(int reduction)
    {
        return new ArmorTypeBonus(this.armorType, this.getType(), this.getStackType(), this.getValue() - reduction);
    }

    @Override
    public void appendDescription(StringBuilder sb)
    {
        super.appendDescription(sb);
        sb.append(" for ");
        sb.append("<span style=\"font-family: blue;\">");
        sb.append(armorType);
        sb.append("</span>");
    }
}
