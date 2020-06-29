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

package hale.rules;

import java.util.ArrayList;
import java.util.List;

import hale.DifficultyManager;
import hale.Game;
import hale.entity.Creature;

/**
 * This class represents an amount of damage (of one or more types)
 * that is applied to a given creature
 *
 * @author Jared
 */

public class Damage
{
    private Creature parent;
    private int totalDamage;
    private String message;

    private List<Entry> entries;


    /**
     * Creates a new Damage object applied against the specified parent.  The damage object
     * is initially empty (total damage of 0)
     *
     * @param parent
     */

    public Damage(Creature parent)
    {
        this.parent = parent;
        this.totalDamage = -1;

        this.entries = new ArrayList<Entry>();
    }

    /**
     * Creates a new Damage object with the specified initial type and amount of damage
     *
     * @param parent the parent creature
     * @param type
     * @param damage
     */

    public Damage(Creature parent, DamageType type, int damage)
    {
        this(parent);
        add(type, damage);
    }

    /*
     * Finds the entry with the specified type or creates it if it does not
     * exist
     */

    private Entry getEntry(DamageType type)
    {
        for (Entry entry : entries) {
            if (entry.type == type) return entry;
        }

        Entry entry = new Entry(type);
        entries.add(entry);

        return entry;
    }

    /**
     * Adds the specified amount and type of damage to this damage
     *
     * @param type   the type of the damage
     * @param damage the amount of the damage
     */

    public void add(DamageType type, int damage)
    {
        Entry entry = getEntry(type);
        entry.damage += damage;
    }

    private void add(Entry entry)
    {
        add(entry.type, entry.damage);
    }

    /**
     * Adds all of the damage types and amounts from the specified damage to this damage
     *
     * @param other
     */

    public void add(Damage other)
    {
        for (Entry entry : other.entries) {
            add(entry);
        }
    }

    /**
     * Computes the total amount of damage that should be applied to the owner creature
     * from all the damage amounts and types that have been added to this damage
     *
     * @return the total amount of damage to apply
     */

    public int computeAppliedDamage()
    {
        DifficultyManager diffManager = Game.ruleset.getDifficultyManager();

        this.totalDamage = 0;

        StringBuilder str = new StringBuilder();

        for (Entry entry : entries) {
            int baseDamageOfType = entry.damage;
            if (parent.getFaction() == Game.ruleset.getFaction(Game.ruleset.getString("PlayerFaction"))) {
                // apply difficulty settings to PCs
                baseDamageOfType = baseDamageOfType * diffManager.getDamageFactorOnPCs() / 100;
            }

            int damage = parent.stats.getAppliedDamage(baseDamageOfType, entry.type);

            totalDamage += damage;

            if (entry.type != null) {
                int dr = parent.stats.getDamageReduction(entry.type);
                int percent = parent.stats.getDamageImmunity(entry.type);

                str.append(" (" + baseDamageOfType + " " + entry.type.getName());
                if (dr > 0) str.append(", " + dr + " Damage Reduction");

                if (percent > 0) {
                    str.append(", " + percent + "% Immune");
                } else
                    if (percent < 0) str.append(", " + (-percent) + "% Vulnerable");
                str.append(")");
            }
        }

        str.append(".");
        str.insert(0, parent.getTemplate().getName() + " takes " + totalDamage + " damage");

        message = str.toString();

        return totalDamage;
    }

    /**
     * Gets the descriptive message showing the amount of damage applied by this damage
     * object
     *
     * @return the descriptive message
     */

    public String getMessage()
    {
        return message;
    }

    /**
     * Returns the total amount of computed applied damage to the designated target
     *
     * @return the total amount of applied damage
     */

    public int getTotalAppliedDamage()
    {
        if (totalDamage == -1) {
            throw new IllegalStateException("computeAppliedDamage must be called prior to getTotalAppliedDamage");
        }

        return totalDamage;
    }

    /**
     * Returns true if and only if this damage has a total (preapplied damage) of at least 1
     *
     * @return whether this damage has a preapplied damage greater than 0
     */

    public boolean causesDamage()
    {
        for (Entry entry : entries) {
            if (entry.damage > 0) return true;
        }

        return false;
    }

    /**
     * A single entry in a damage list
     *
     * @author Jared
     */

    private class Entry
    {
        private final DamageType type;
        private int damage;

        private Entry(DamageType type)
        {
            this.type = type;
        }
    }
}
