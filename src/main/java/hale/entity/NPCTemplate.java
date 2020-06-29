/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2012 Jared Stephen
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

package main.java.hale.entity;

import java.text.ParseException;
import java.util.Iterator;

import main.java.hale.Game;
import main.java.hale.util.SimpleJSONArray;
import main.java.hale.util.SimpleJSONArrayEntry;
import main.java.hale.util.SimpleJSONObject;

/**
 * The class containing the immutable parts of a non-player character creature
 *
 * @author Jared
 */

public class NPCTemplate extends CreatureTemplate
{

    // the currency reward for defeating this NPC
    private int minReward, maxReward;

    // the items dropped by this NPC on death
    private LootList loot;

    // whether this NPC can be killed
    private boolean isImmortal;

    /**
     * Creates a new NPC template
     *
     * @param id   the ID of the entity
     * @param data the JSON data to parse
     * @throws ParseException
     */

    public NPCTemplate(String id, SimpleJSONObject data)
    {
        super(id, data);

        if (data.containsKey("isImmortal")) {
            isImmortal = data.get("isImmortal", false);
        } else {
            isImmortal = false;
        }

        if (data.containsKey("reward")) {
            SimpleJSONArray reward = data.getArray("reward");

            Iterator<SimpleJSONArrayEntry> iter = reward.iterator();

            int first = iter.next().getInt(0);
            int second = iter.next().getInt(0);

            if (first < second) {
                minReward = first;
                maxReward = second;
            } else {
                minReward = second;
                maxReward = first;
            }
        }

        if (data.containsKey("loot")) {
            loot = new LootList(data.getArray("loot"));
        } else {
            loot = null;
        }
    }

    @Override
    public NPC createInstance()
    {
        throw new IllegalStateException("Instances of NPCs cannot be created directly");
    }

    /**
     * Generates a random reward of currency based on the minimum and maximum
     * values defined for this NPC
     *
     * @return a currency reward in copper pieces (CP) over 100
     */

    public int generateReward()
    {
        if (minReward == maxReward) return minReward;

        return Game.dice.rand(minReward, maxReward);
    }

    /**
     * Generates an ItemList with randomly generated loot for this NPC
     *
     * @return the loot that was generated
     */

    public ItemList generateLoot()
    {
        if (loot == null) return new ItemList();

        return loot.generate();
    }

    /**
     * Returns true if this NPC is immortal and cannot be killed, false
     * otherwise
     *
     * @return whether this NPC is immortal
     */

    public boolean isImmortal()
    {
        return isImmortal;
    }
}
