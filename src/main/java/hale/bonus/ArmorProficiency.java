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
import main.java.hale.util.SimpleJSONObject;

public class ArmorProficiency extends Bonus
{
    private final String armorType;

    @Override
    public JSONOrderedObject save()
    {
        JSONOrderedObject data = new JSONOrderedObject();

        data.put("class", this.getClass().getName());
        data.put("armorProficiency", armorType);

        return data;
    }

    public static ArmorProficiency load(SimpleJSONObject data)
    {
        return new ArmorProficiency(data.get("armorProficiency", null));
    }

    public ArmorProficiency(String armorType)
    {
        super(Bonus.Type.ArmorProficiency, Bonus.StackType.GenericBonus);

        this.armorType = armorType;
    }

    public String getArmorType()
    {
        return armorType;
    }

    @Override
    public ArmorProficiency cloneWithReduction(int reduction)
    {
        return new ArmorProficiency(this.armorType);
    }

    @Override
    public void appendDescription(StringBuilder sb)
    {
        sb.append("<span style=\"font-family: blue;\">");
        sb.append(armorType);
        sb.append("</span> ");
        super.appendDescription(sb);
    }
}