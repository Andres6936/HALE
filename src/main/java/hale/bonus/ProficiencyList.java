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

import java.util.LinkedHashSet;
import java.util.Set;

public class ProficiencyList
{
    private Set<String> proficiencies;

    public ProficiencyList()
    {
        proficiencies = new LinkedHashSet<String>();
    }

    public ProficiencyList(ProficiencyList other)
    {
        proficiencies = new LinkedHashSet<String>(other.proficiencies);
    }

    public void add(String proficiency)
    {
        proficiencies.add(proficiency);
    }

    public void remove(String proficiency)
    {
        proficiencies.remove(proficiency);
    }

    public void clear()
    {
        proficiencies.clear();
    }

    public boolean hasProficiency(String proficiency)
    {
        return proficiencies.contains(proficiency);
    }
}
