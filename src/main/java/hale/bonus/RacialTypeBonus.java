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

public class RacialTypeBonus extends IntBonus implements BonusWithSuperType
{
    private final String racialType;

    @Override
    public JSONOrderedObject save()
    {
        JSONOrderedObject data = super.save();

        data.put("racialType", racialType);

        return data;
    }

    public static RacialTypeBonus load(SimpleJSONObject data)
    {
        int value = data.get("value", 0);
        Bonus.Type type = Type.valueOf(data.get("type", null));
        Bonus.StackType stackType = StackType.valueOf(data.get("stackType", null));
        String racialType = data.get("racialType", null);

        return new RacialTypeBonus(type, stackType, racialType, value);
    }

    public RacialTypeBonus(Bonus.Type type, Bonus.StackType stackType, String racialType, int value)
    {
        super(type, stackType, value);

        this.racialType = racialType;
    }

    public String getRacialType()
    {
        return racialType;
    }

    @Override
    public String getSuperType()
    {
        return racialType;
    }

    @Override
    public RacialTypeBonus cloneWithReduction(int reduction)
    {
        return new RacialTypeBonus(this.getType(), this.getStackType(), this.racialType, this.getValue() - reduction);
    }

    @Override
    public void appendDescription(StringBuilder sb)
    {
        super.appendDescription(sb);
        sb.append(" vs Racial Type ");
        sb.append("<span style=\"font-family: blue;\">");
        sb.append(racialType);
        sb.append("</span>");
    }
}