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

import java.util.LinkedList;
import java.util.List;

public class BonusStackTypeList
{
    private int currentTotal;
    private List<Bonus> bonuses;

    public BonusStackTypeList()
    {
        bonuses = new LinkedList<Bonus>();
        currentTotal = 0;
    }

    public BonusStackTypeList(BonusStackTypeList other)
    {
        this.bonuses = new LinkedList<Bonus>(other.bonuses);
        this.currentTotal = other.currentTotal;
    }

    public void add(Bonus bonus)
    {
        bonuses.add(bonus);

        if (!bonus.hasValue()) return;

        switch (bonus.getStackType()) {
            case StackableBonus:
            case StackablePenalty:
                currentTotal += bonus.getValue();
                break;
            default:
                if (Math.abs(bonus.getValue()) > Math.abs(currentTotal)) currentTotal = bonus.getValue();
        }
    }

    public void remove(Bonus bonus)
    {
        bonuses.remove(bonus);

        if (!bonus.hasValue()) return;

        currentTotal = 0;

        // we need to recompute the total for this stack type from scratch
        switch (bonus.getStackType()) {
            case StackableBonus:
            case StackablePenalty:
                for (Bonus b : bonuses) {
                    currentTotal += b.getValue();
                }
                break;
            default:
                for (Bonus b : bonuses) {
                    if (Math.abs(b.getValue()) > Math.abs(currentTotal)) currentTotal = b.getValue();
                }
        }
    }

    public int getCurrentTotal()
    {
        return currentTotal;
    }

    public boolean isEmpty()
    {
        return bonuses.size() == 0;
    }
}
